"use strict";

/**
 * M1-B institutional licensing SERVER-ONLY staging functions.
 * Not connected to the legacy Android code, not deployed to Firebase.
 *
 * Each seat uses an unpredictable, single-use code that is stored only as a
 * SHA-256 digest. Parent ownership requires verified Firebase Auth custom
 * claim mmParentVerified. Commercial admins require mmCommercialAdmin.
 * Admin claims are set only from trusted Admin SDK operations, never by app.
 */
const {onCall,HttpsError}=require("firebase-functions/v2/https");
const {defineSecret}=require("firebase-functions/params");
const {getFirestore,FieldValue}=require("firebase-admin/firestore");
const policy=require("./src/institution-policy.cjs");

const db=getFirestore();
const region="europe-west1";
const codeSecret=defineSecret("MM_LICENSE_SIGNING_KEY");
const contracts=db.collection("institutionContractsV1");
const batches=db.collection("institutionCodeBatchesV1");
const codes=db.collection("institutionCodesV1");
const assignments=db.collection("institutionAssignmentsV1");
const googlePurchases=db.collection("parentPremiumEntitlementsV1");

function requireLoggedIn(req){
  if(!req.auth?.uid || !req.app?.appId)
    throw new HttpsError("unauthenticated","Doğrulanmış oturum ve uygulama gerekli.");
  return req.auth.uid;
}
function requireCommercialAdmin(req){
  const uid=requireLoggedIn(req);
  if(req.auth.token.mmCommercialAdmin!==true)
    throw new HttpsError("permission-denied","Ticari lisans yöneticisi yetkisi gerekli.");
  return uid;
}
function requireVerifiedParent(req){
  const uid=requireLoggedIn(req);
  if(req.auth.token.mmParentVerified!==true ||
    req.auth.token.firebase?.sign_in_provider==="anonymous")
    throw new HttpsError("permission-denied","Doğrulanmış ebeveyn hesabı gerekli.");
  return uid;
}
function invalid(error){
  if(error instanceof HttpsError)throw error;
  if(error instanceof Error && /^[A-Z_]+$/.test(error.message))
    throw new HttpsError("invalid-argument","Geçersiz lisans bilgisi: "+error.message);
  throw error;
}
function safeInt(value) {
  if(!Number.isSafeInteger(value)||value<0)return 0;
  return value;
}

/**
 * ADMIN ONLY. Business payment is recorded OUTSIDE the app. The invoice ref
 * is idempotent: retry cannot silently create more seats. No automatic charge.
 */
exports.createInstitutionContract=onCall({
  region,enforceAppCheck:true,maxInstances:3,timeoutSeconds:30
},async req=>{
  const uid=requireCommercialAdmin(req);
  let fields;
  try{fields=policy.validateContractInput(req.data,Date.now());}
  catch(err){invalid(err);}
  const id=policy.contractId(uid,fields.invoiceReference);
  const ref=contracts.doc(id);
  await db.runTransaction(async tx=>{
    const snap=await tx.get(ref);
    if(snap.exists){
      const existing=snap.data();
      if(existing.createdByUid===uid && existing.invoiceReference===fields.invoiceReference &&
        existing.seats===fields.seats && existing.expiresAtMs===fields.expiresAtMs &&
        existing.activateUntilMs===fields.activateUntilMs &&
        existing.institutionName===fields.institutionName)return;
      throw new HttpsError("already-exists","Fatura referansı farklı lisans bilgileriyle kullanılmış.");
    }
    tx.create(ref,{
      institutionName:fields.institutionName,
      invoiceReference:fields.invoiceReference,
      seats:fields.seats,seatsIssued:0,seatsRedeemed:0,seatsRevoked:0,
      status:"ACTIVE",expiresAtMs:fields.expiresAtMs,
      activateUntilMs:fields.activateUntilMs,
      createdByUid:uid,verifiedByUid:uid,paymentRecorded:true,
      createdAt:FieldValue.serverTimestamp()
    });
  });
  return {contractId:id,seats:fields.seats,status:"ACTIVE"};
});

/**
 * ADMIN ONLY. One unique code per licensed seat; batches cap at 200 codes.
 * The supplied requestId MUST be unique per delivery/invoice action.
 * HMAC makes lost responses safely retriable without saving code plaintext.
 */
exports.issueInstitutionSeatCodes=onCall({
  region,enforceAppCheck:true,secrets:[codeSecret],
  maxInstances:3,timeoutSeconds:60,memory:"512MiB",
},async req=>{
  requireCommercialAdmin(req);
  let contractId,requestId,count,secret;
  try{
    contractId=policy.validateId(req.data?.contractId);
    requestId=policy.validateRequestId(req.data?.requestId);
    count=policy.validateCount(req.data?.count);
    secret=codeSecret.value();
    // Fail before any writes if Secret Manager isn't configured.
    policy.deterministicCode(secret,contractId,requestId,0);
  }catch(e){invalid(e);}
  const codeList=Array.from({length:count},(_,i)=>
    policy.deterministicCode(secret,contractId,requestId,i));
  const hashes=codeList.map(policy.codeHash);
  const batchId=policy.batchId(contractId,requestId);
  const contractRef=contracts.doc(contractId),batchRef=batches.doc(batchId);
  let repeated=false;
  await db.runTransaction(async tx=>{
    // All reads BEFORE any writes.
    const [contractSnap,batchSnap]=await Promise.all([
      tx.get(contractRef),tx.get(batchRef)
    ]);
    if(!contractSnap.exists)
      throw new HttpsError("not-found","Kurum sözleşmesi bulunamadı.");
    if(batchSnap.exists){
      const record=batchSnap.data();
      if(record.contractId!==contractId || record.requestId!==requestId ||
         record.count!==count)
        throw new HttpsError("already-exists","Paket isteği başka içerikle oluşturulmuş.");
      repeated=true;
      return;
    }
    if(!policy.canIssue(contractSnap.data(),count,Date.now()))
      throw new HttpsError("failed-precondition","Sözleşme aktif değil veya lisans kapasitesi doldu.");
    for(let i=0;i<count;i++){
      tx.create(codes.doc(hashes[i]),{
        contractId,batchId,status:"ISSUED",
        issuedAt:FieldValue.serverTimestamp()
      });
    }
    tx.create(batchRef,{
      contractId,requestId,count,issuedByUid:req.auth.uid,
      createdAt:FieldValue.serverTimestamp()
    });
    tx.update(contractRef,{
      seatsIssued:FieldValue.increment(count),
      updatedAt:FieldValue.serverTimestamp()
    });
  });
  // Only the authorized admin receives plaintext ONCE per call; never log them.
  return {contractId,batchId,count,alreadyIssued:repeated,codes:codeList};
});

/**
 * VERIFIED PARENT ONLY. This is an atomic one-seat redemption. A shared code
 * cannot grant two students; multiple parents racing on the same code cannot
 * oversubscribe the contract. Student identity/PII is not stored in the code.
 */
exports.redeemInstitutionSeatCode=onCall({
  region,enforceAppCheck:true,maxInstances:8,timeoutSeconds:30,
},async req=>{
  const uid=requireVerifiedParent(req);
  let hash;
  try{hash=policy.codeHash(req.data?.code);}
  catch(e){invalid(e);}
  const codeRef=codes.doc(hash);
  const seatRef=assignments.doc(uid);
  let result;
  await db.runTransaction(async tx=>{
    const codeSnap=await tx.get(codeRef);
    if(!codeSnap.exists)
      throw new HttpsError("not-found","Lisans kodu bulunamadı.");
    const code=codeSnap.data();
    // Corrupted records fail closed; never use unvalidated Firestore paths.
    let contractId;
    try{contractId=policy.validateId(code.contractId);}
    catch{throw new HttpsError("failed-precondition","Lisans kaydı geçersiz.");}
    const contractRef=contracts.doc(contractId);
    const [contractSnap,seatSnap]=await Promise.all([
      tx.get(contractRef),tx.get(seatRef)
    ]);
    const contract=contractSnap.exists
      ? {id:contractId,...contractSnap.data()} : null;
    const seat=seatSnap.exists ? seatSnap.data() : null;
    const now=Date.now();
    if(code.status==="REDEEMED" && code.ownerUid===uid &&
       seat?.codeHash===hash &&
       policy.institutionActive(contract,seat,now)){
      result={status:"ALREADY_REDEEMED",active:true};
      return;
    }
    if(code.status!=="ISSUED")
      throw new HttpsError("failed-precondition","Kod daha önce kullanılmış veya iptal edilmiş.");
    if(!policy.canRedeem(contract,now))
      throw new HttpsError("failed-precondition","Kurum lisansının aktivasyon süresi dolmuş.");
    if(seat && policy.institutionActive(contract,seat,now))
      throw new HttpsError("already-exists","Bu hesapta zaten aktif bir kurum lisansı var.");
    // A still-live seat in another contract must not be overwritten by a new
    // claim without explicitly migrating it. Fail closed on ambiguity.
    if(seat?.status==="ACTIVE" && Number.isSafeInteger(seat.expiresAtMs) &&
       now<seat.expiresAtMs)
      throw new HttpsError("failed-precondition","Aktif lisans devri için yönetici gerekli.");
    tx.update(codeRef,{
      status:"REDEEMED",ownerUid:uid,
      redeemedAt:FieldValue.serverTimestamp()
    });
    tx.set(seatRef,{
      status:"ACTIVE",contractId,codeHash:hash,
      expiresAtMs:contract.expiresAtMs,
      activatedAt:FieldValue.serverTimestamp()
    });
    tx.update(contractRef,{
      seatsRedeemed:FieldValue.increment(1),
      updatedAt:FieldValue.serverTimestamp()
    });
    result={status:"REDEEMED",active:true};
  });
  return result;
});

/**
 * ADMIN ONLY. Revoking a code affects ONLY the corresponding institutional
 * right. An independently owned Google Play purchase stays untouched.
 */
exports.revokeInstitutionSeatCode=onCall({
  region,enforceAppCheck:true,maxInstances:3,timeoutSeconds:30,
},async req=>{
  requireCommercialAdmin(req);
  let hash;
  try{hash=policy.codeHash(req.data?.code);}
  catch(e){invalid(e);}
  const codeRef=codes.doc(hash);
  let status="REVOKED";
  await db.runTransaction(async tx=>{
    const codeSnap=await tx.get(codeRef);
    if(!codeSnap.exists)
      throw new HttpsError("not-found","Lisans kodu bulunamadı.");
    const code=codeSnap.data();
    if(code.status==="REVOKED")return;
    const contractRef=contracts.doc(policy.validateId(code.contractId));
    const seatRef=code.ownerUid ? assignments.doc(code.ownerUid) : null;
    // All reads before writes.
    const [contractSnap,seatSnap]=await Promise.all([
      tx.get(contractRef),seatRef?tx.get(seatRef):Promise.resolve(null)
    ]);
    if(!contractSnap.exists)
      throw new HttpsError("failed-precondition","Kurum sözleşmesi kaydı eksik.");
    tx.update(codeRef,{
      status:"REVOKED",revokedByUid:req.auth.uid,
      revokedAt:FieldValue.serverTimestamp()
    });
    if(seatSnap?.exists && seatSnap.data().codeHash===hash){
      tx.update(seatRef,{
        status:"REVOKED",revokedAt:FieldValue.serverTimestamp()
      });
    }
    if(code.status==="REDEEMED"){
      tx.update(contractRef,{
        seatsRevoked:FieldValue.increment(1),
        updatedAt:FieldValue.serverTimestamp()
      });
    }
  });
  return {status};
});

/** ADMIN ONLY; suspend/reactivate, or permanently revoke a whole contract. */
exports.updateInstitutionContractStatus=onCall({
  region,enforceAppCheck:true,maxInstances:3
},async req=>{
  requireCommercialAdmin(req);
  let contractId;
  try{contractId=policy.validateId(req.data?.contractId);}
  catch(e){invalid(e);}
  const desired=req.data?.status;
  if(!["ACTIVE","SUSPENDED","REVOKED"].includes(desired))
    throw new HttpsError("invalid-argument","Geçersiz sözleşme durumu.");
  const ref=contracts.doc(contractId);
  await db.runTransaction(async tx=>{
    const snap=await tx.get(ref);
    if(!snap.exists)throw new HttpsError("not-found","Sözleşme yok.");
    if(snap.data().status==="REVOKED" && desired!=="REVOKED")
      throw new HttpsError("failed-precondition","İptal edilmiş sözleşme tekrar açılmaz.");
    if(desired==="ACTIVE" && Date.now()>=snap.data().expiresAtMs)
      throw new HttpsError("failed-precondition","Süresi dolmuş sözleşme tekrar açılamaz.");
    tx.update(ref,{
      status:desired,updatedAt:FieldValue.serverTimestamp(),
      lastChangedByUid:req.auth.uid
    });
  });
  return {contractId,status:desired};
});

/**
 * Single server-side source-of-truth QUERY, not a new local isPremium flag.
 * During migration, legacy Realtime Database activations remain on Android
 * and are NOT silently copied into server grants.
 */
exports.getVerifiedPremiumAccess=onCall({
  region,enforceAppCheck:true,maxInstances:8
},async req=>{
  const uid=requireVerifiedParent(req);
  const [playSnap,seatSnap]=await Promise.all([
    googlePurchases.doc(uid).get(),assignments.doc(uid).get()
  ]);
  const seat=seatSnap.exists?seatSnap.data():null;
  let contract=null;
  if(seat?.contractId){
    try{
      const id=policy.validateId(seat.contractId);
      const doc=await contracts.doc(id).get();
      if(doc.exists)contract={id,...doc.data()};
    }catch{contract=null;}
  }
  return policy.effectivePremium(
    playSnap.exists?playSnap.data():null,contract,seat,Date.now()
  );
});
