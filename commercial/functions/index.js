"use strict";

/**
 * M1 reference backend. DO NOT deploy until Google Play Console service account,
 * Firestore, parental Firebase Auth, Firebase App Check and Pub/Sub permissions
 * have been configured and tested. Android release currently uses legacy billing.
 *
 * All endpoints require verified Firebase Auth + enforced App Check.
 * Never put Google API credentials in the mobile app.
 */
const {onCall,HttpsError} = require("firebase-functions/v2/https");
const {onMessagePublished} = require("firebase-functions/v2/pubsub");
const {onSchedule} = require("firebase-functions/v2/scheduler");
const logger = require("firebase-functions/logger");
const {initializeApp} = require("firebase-admin/app");
const {getFirestore,FieldValue} = require("firebase-admin/firestore");
const {GoogleAuth} = require("google-auth-library");
const {PACKAGE, PREMIUM_PRODUCT, VERSION, tokenHash, classifyGoogleProduct} =
  require("./policy.cjs");

initializeApp();
const database = getFirestore();
const googleAuth = new GoogleAuth({scopes:["https://www.googleapis.com/auth/androidpublisher"]});
const region = "europe-west1";
const base = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/" +
  encodeURIComponent(PACKAGE) + "/purchases/";

async function playRequest(method,path){
  const client = await googleAuth.getClient();
  const response = await client.request({url:base+path,method,timeout:12000});
  return response.data || {};
}

async function currentProduct(token){
  return playRequest("GET","productsv2/tokens/"+encodeURIComponent(token));
}
async function acknowledge(token){
  await playRequest("POST","products/"+encodeURIComponent(PREMIUM_PRODUCT)+
    "/tokens/"+encodeURIComponent(token)+":acknowledge");
}

function authenticate(request){
  if(!request.auth?.uid || !request.app?.appId){
    throw new HttpsError("unauthenticated","Ebeveyn oturumu ve uygulama doğrulaması gerekli.");
  }
  return request.auth.uid;
}

const tokenDocument = hash => database.collection("billingTokensV1").doc(hash);
const userDocument = uid => database.collection("parentPremiumEntitlementsV1").doc(uid);

async function markRevoked(token,reason){
  const hash = tokenHash(token);
  const ref = tokenDocument(hash);
  await database.runTransaction(async txn=>{
    const snapshot = await txn.get(ref);
    if(!snapshot.exists) {
      // Tombstone blocks race where verification follows a refund callback.
      txn.create(ref,{status:"REVOKED",reason,updatedAt:FieldValue.serverTimestamp()});
      return;
    }
    const item = snapshot.data();
    if(item.status==="REVOKED") return;
    const entitled=item.ownerUid ? userDocument(item.ownerUid) : null;
    const active=entitled ? await txn.get(entitled) : null;
    // Firestore transactions require every read before the first write.
    txn.update(ref,{status:"REVOKED",reason,updatedAt:FieldValue.serverTimestamp()});
    if(entitled && active.exists && active.data().tokenHash===hash){
      txn.set(entitled,{active:false,reason,updatedAt:FieldValue.serverTimestamp()},
        {merge:true});
    }
  });
}

/**
 * Validates a Play-supplied purchase token with Google, grants once to a verified
 * parent identity, and acknowledges after the database grant. No raw token,
 * order ID, payment amount or child's information is stored in Firestore.
 *
 * Migration: Old users have no parent UID; do not enable this for clients until
 * account linking and an explicit legacy-rights transition are implemented.
 */
exports.verifyGooglePremium = onCall({
  region,enforceAppCheck:true,maxInstances:5,timeoutSeconds:30,
},async request=>{
  const uid = authenticate(request);
  const token = request.data?.purchaseToken;
  let hash;
  try { hash = tokenHash(token); }
  catch { throw new HttpsError("invalid-argument","Geçersiz satın alma bilgisi."); }

  let purchase,classification;
  try {
    purchase=await currentProduct(token);
    classification=classifyGoogleProduct(purchase,uid);
  } catch(error){
    logger.error("Play purchase verification unavailable",{status:error?.response?.status || null});
    throw new HttpsError("unavailable","Google Play doğrulaması tamamlanamadı.");
  }

  if(classification==="PENDING") return {status:"PENDING",active:false};
  if(classification==="CANCELLED"){
    await markRevoked(token,"PLAY_CANCELLED");
    return {status:"CANCELLED",active:false};
  }
  if(classification!=="PURCHASED"){
    throw new HttpsError("failed-precondition","Ödeme doğrulanamadı veya hesap uyuşmuyor.");
  }

  const ref=tokenDocument(hash);
  await database.runTransaction(async txn=>{
    const previous=await txn.get(ref);
    if(previous.exists){
      const data=previous.data();
      if(data.status==="REVOKED") {
        throw new HttpsError("permission-denied","İade edilmiş işlem tekrar kullanılamaz.");
      }
      if(data.ownerUid!==uid){
        throw new HttpsError("permission-denied","Satın alma başka bir hesaba bağlı.");
      }
    }
    txn.set(ref,{ownerUid:uid,status:"ACTIVE",productId:PREMIUM_PRODUCT,
      platform:"google_play",version:VERSION,
      updatedAt:FieldValue.serverTimestamp()}, {merge:true});
    txn.set(userDocument(uid),{
      active:true,platform:"google_play",tokenHash:hash,
      productId:PREMIUM_PRODUCT,updatedAt:FieldValue.serverTimestamp()
    },{merge:true});
  });

  // Acknowledge only AFTER a confirmed durable entitlement grant. Retry
  // the callable on transient failures (the client must not grant a second right).
  if(purchase.acknowledgementState!=="ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED"){
    try { await acknowledge(token); }
    catch(error){
      logger.error("Play acknowledgement requires retry",{status:error?.response?.status||null});
      throw new HttpsError("unavailable","Ödeme onayı tamamlanamadı; yeniden dene.");
    }
  }
  return {status:"VERIFIED",active:true,productId:PREMIUM_PRODUCT};
});

/** A read-only, authenticated snapshot; never a client-writable entitlement. */
exports.getParentPremium = onCall({
  region,enforceAppCheck:true,maxInstances:5,
},async request=>{
  const uid=authenticate(request);
  const snapshot=await userDocument(uid).get();
  return {active:snapshot.exists && snapshot.data().active===true};
});

/**
 * RTDN is delivered by trusted Google Cloud Pub/Sub infrastructure. The Play
 * Console topic must be configured; raw token is never logged or saved.
 */
exports.googlePlayOneTimeRtdn = onMessagePublished({
  topic:"mm-google-play-billing",region,maxInstances:3,retry:true,
},async event=>{
  const raw=event.data?.message?.json ||
    JSON.parse(Buffer.from(event.data?.message?.data||"","base64").toString("utf8"));
  if(raw.packageName!==PACKAGE)return;
  const voided=raw.voidedPurchaseNotification;
  if(voided?.purchaseToken){
    await markRevoked(voided.purchaseToken,"PLAY_VOIDED");
    return;
  }
  const product=raw.oneTimeProductNotification;
  if(!product?.purchaseToken || product.sku!==PREMIUM_PRODUCT)return;
  const token=product.purchaseToken;
  const hash=tokenHash(token);
  const record=await tokenDocument(hash).get();
  if(!record.exists || record.data().status==="REVOKED")return;
  const state=classifyGoogleProduct(await currentProduct(token),record.data().ownerUid);
  if(state==="CANCELLED" || state==="INVALID" || state==="WRONG_ACCOUNT"){
    await markRevoked(token,"PLAY_RTDN_"+state);
  }else if(state==="PURCHASED"){
    const current=await currentProduct(token);
    if(current.acknowledgementState!=="ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED"){
      await acknowledge(token);
    }
  }
});

/** Voided Purchases API backstop for delayed/missed RTDN, with paginated scans. */
exports.reconcilePlayRefunds = onSchedule({
  schedule:"every 12 hours",region,maxInstances:1,timeoutSeconds:240,
},async()=>{
  let page="";
  let count=0;
  const now=Date.now();
  do{
    const qs=new URLSearchParams({
      startTime:String(now-8*24*60*60*1000),
      endTime:String(now),
      type:"0",
      "pageSelection.maxResults":"500"
    });
    if(page)qs.set("pageSelection.token",page);
    const result=await playRequest("GET","voidedpurchases?"+qs.toString());
    for(const purchase of result.voidedPurchases||[]){
      if(purchase.purchaseToken){
        await markRevoked(purchase.purchaseToken,"PLAY_VOIDED_RECONCILIATION");
      }
    }
    count += 1;
    page=result.tokenPagination?.nextPageToken||"";
    if(count>=100 && page)throw new Error("Voided purchase pagination cap reached");
  }while(page);
  logger.info("Google Play voided purchase reconciliation finished",{pages:count});
});
