"use strict";

/**
 * Deterministic in-memory Firestore transaction harness for M1.
 * Verifies that the ACTUAL callable handlers enforce idempotency and 200
 * independent seats under race. This is NOT a replacement for the Firebase
 * Emulator Suite or real App Check/Play Console integration.
 */
const {test}=require("node:test");
const assert=require("node:assert/strict");
const Module=require("node:module");
const policy=require("./institution-policy.cjs");

const firestore=new Map();
let mutationClock=0;
const snapshot=(path,record)=>{
  const item=record?structuredClone(record.data):undefined;
  return {id:path.split("/").pop(),exists:Boolean(record),data:()=>structuredClone(item)};
};
const get=(path)=>snapshot(path,firestore.get(path));
const serverTimestamp=()=>({sentinel:"timestamp"});
const increment=n=>({sentinel:"increment",n});
const resolve=(obj,old={})=>{
  const fresh={...old,...obj};
  for(const [key,v] of Object.entries(obj)) {
    if(v?.sentinel==="timestamp")fresh[key]="server-timestamp";
    if(v?.sentinel==="increment")fresh[key]=(old[key]||0)+v.n;
  }
  return fresh;
};
function ref(path){
  return {
    path,
    get:async()=>get(path),
    collection:child=>coll(path+"/"+child)
  };
}
function coll(path) {
  return {
    doc:id=>ref(path+"/"+id),
    where:(field,comparison,value)=>{
      assert.equal(field,"contractId");
      assert.equal(comparison,"==");
      return {count:()=>({get:async()=>({data:()=>({
        count:[...firestore.entries()].filter(([name,record])=>
          name.startsWith(path+"/") && record.data.contractId===value).length
      })})})};
    }
  };
}
class Conflict extends Error{}
async function runTransaction(handler){
  for(let retry=0;retry<80;retry++){
    const reads=new Map(),writes=[];
    const tx={
      async get(r){
        if(writes.length)throw new Error("READ_AFTER_WRITE");
        const record=firestore.get(r.path);
        reads.set(r.path,record?.version||0);
        await Promise.resolve();
        return snapshot(r.path,record);
      },
      create(r,data){writes.push({type:"create",path:r.path,data});},
      set(r,data,options){writes.push({type:"set",path:r.path,data,merge:options?.merge});},
      update(r,data){writes.push({type:"update",path:r.path,data});},
    };
    const response=await handler(tx);
    if([...reads].some(([path,version])=>
        (firestore.get(path)?.version||0)!==version))continue;
    const writesValid=writes.every(w=>w.type!=="create"||!firestore.has(w.path));
    if(!writesValid)throw new Error("ALREADY_EXISTS");
    for(const w of writes){
      const old=firestore.get(w.path);
      if(w.type==="update"&&!old)throw new Error("NOT_FOUND");
      const data=resolve(w.data,w.type==="set"&&!w.merge?{}:old?.data||{});
      firestore.set(w.path,{data,version:++mutationClock});
    }
    return response;
  }
  throw new Conflict("TRANSACTION_RETRY_LIMIT");
}
const db={collection:coll,runTransaction};
const admin={
  serverTimestamp,increment,
};
const originalLoad=Module._load;
class HttpsError extends Error {
  constructor(code,message){super(message);this.code=code;}
}
let secret="test-only-development-secret-string-123456789!";
Module._load=function(id,parent,isMain){
  if(id==="firebase-functions/v2/https")return {
    onCall:(_options,handler)=>handler,HttpsError
  };
  if(id==="firebase-functions/params")return {
    defineSecret:()=>({value:()=>secret})
  };
  if(id==="firebase-admin/firestore")return {
    getFirestore:()=>db,FieldValue:admin
  };
  if(id==="firebase-admin/auth")return {
    getAuth:()=>({getUser:async uid=>({
      uid,disabled:false,customClaims:{mmParentVerified:true}
    })})
  };
  return originalLoad.apply(this,arguments);
};
const handlers=require("../institution-functions.cjs");
// Explicit opt-in ONLY in this isolated mock process. Default remains disabled.
const previousFlag=process.env.MM_M1_BACKEND_ENABLED;
process.env.MM_M1_BACKEND_ENABLED="true";
Module._load=originalLoad;

const adminRequest=(data={})=>({
  auth:{uid:"operator-admin",token:{mmCommercialAdmin:true}},
  app:{appId:"test-app"},data
});
const guardianRequest=(uid,data={})=>({
  auth:{uid,token:{mmParentVerified:true}},
  app:{appId:"test-app"},data
});

test("200 parallel code redemption transactions, ownership, retry and source isolation",
  async()=>{
    const now=Date.now();
    const created=await handlers.createInstitutionContract(adminRequest({
      institutionName:"Test School",invoiceReference:"INV_TEST_BATCH_2026",
      seats:200,paymentRecorded:true,
      expiresAtMs:now+365*86400000,activateUntilMs:now+40*86400000
    }));
    assert.equal(created.seats,200);
    const id=created.contractId;
    await assert.rejects(
      handlers.createInstitutionContract(adminRequest({
        institutionName:"Changed School",invoiceReference:"INV_TEST_BATCH_2026",
        seats:201,paymentRecorded:true,
        expiresAtMs:now+365*86400000,activateUntilMs:now+40*86400000
      })),{code:"already-exists"});
    const req={contractId:id,requestId:"BATCH_TEST_2026_0001",count:200};
    const batch=await handlers.issueInstitutionSeatCodes(adminRequest(req));
    assert.equal(batch.codes.length,200);
    assert.equal(new Set(batch.codes).size,200);
    const replay=await handlers.issueInstitutionSeatCodes(adminRequest(req));
    assert.equal(replay.alreadyIssued,true);
    assert.deepEqual(replay.codes,batch.codes);
    await assert.rejects(
      handlers.issueInstitutionSeatCodes(adminRequest({
        ...req,requestId:"BATCH_TEST_2026_0002",count:1
      })),{code:"failed-precondition"});
    secret="rotated-test-secret-string-very-long-and-different!";
    await assert.rejects(
      handlers.issueInstitutionSeatCodes(adminRequest(req)),{code:"already-exists"});
    secret="test-only-development-secret-string-123456789!";

    const guardians=Array.from({length:200},(_,i)=>"guardian-"+i);
    const profiles=Array.from({length:200},(_,i)=>"student_"+String(i).padStart(5,"0"));
    await Promise.all(guardians.map((uid,i)=>
      handlers.setVerifiedStudentProfile(adminRequest({
        guardianUid:uid,studentProfileId:profiles[i],active:true
      }))));
    // A random profile cannot redeem even when the guardian is authenticated.
    await assert.rejects(
      handlers.redeemInstitutionSeatCode(guardianRequest(
        "guardian-0",{code:batch.codes[0],studentProfileId:"unlinked_0001"}
      )),{code:"permission-denied"});

    const result=await Promise.all(batch.codes.map((code,i)=>
      handlers.redeemInstitutionSeatCode(guardianRequest(guardians[i],{
        code,studentProfileId:profiles[i]
      }))));
    assert.equal(result.filter(x=>x.status==="REDEEMED").length,200);
    const contract=firestore.get("institutionContractsV1/"+id).data;
    assert.equal(contract.seatsIssued,200);
    assert.equal(Object.prototype.hasOwnProperty.call(contract,"seatsRedeemed"),false);
    assert.equal([...firestore.keys()].filter(p=>
      p.startsWith("institutionRedemptionsV1/")).length,200);
    const usage=await handlers.getInstitutionContractSummary(
      adminRequest({contractId:id}));
    assert.equal(usage.issued,200);
    assert.equal(usage.everRedeemed,200);
    assert.equal(usage.currentlyGranted,200);
    assert.equal(usage.remainingToIssue,0);

    const already=await handlers.redeemInstitutionSeatCode(
      guardianRequest(guardians[0],{code:batch.codes[0],studentProfileId:profiles[0]}));
    assert.equal(already.status,"ALREADY_REDEEMED");
    await assert.rejects(handlers.redeemInstitutionSeatCode(
      guardianRequest(guardians[1],{code:batch.codes[0],studentProfileId:profiles[1]})),
      {code:"failed-precondition"});
    const access=await handlers.getVerifiedPremiumAccess(
      guardianRequest(guardians[0],{studentProfileId:profiles[0]}));
    assert.deepEqual(access,{active:true,googlePlay:false,institution:true});

    // Pausing a verified profile revokes the institution path without
    // changing the Google Play path (if the parent separately purchased).
    await handlers.setVerifiedStudentProfile(adminRequest({
      guardianUid:guardians[0],studentProfileId:profiles[0],active:false
    }));
    assert.equal((await handlers.getVerifiedPremiumAccess(
      guardianRequest(guardians[0],{studentProfileId:profiles[0]}))).active,false);
    firestore.set("parentPremiumEntitlementsV1/"+guardians[0],{
      data:{active:true,platform:"google_play",
        productId:"matematik_macerasi_premium"},version:++mutationClock});
    let independent=await handlers.getVerifiedPremiumAccess(
      guardianRequest(guardians[0],{studentProfileId:profiles[0]}));
    assert.deepEqual(independent,{active:true,googlePlay:true,institution:false});

    await handlers.revokeInstitutionSeatCode(adminRequest({code:batch.codes[0]}));
    independent=await handlers.getVerifiedPremiumAccess(
      guardianRequest(guardians[0],{studentProfileId:profiles[0]}));
    assert.deepEqual(independent,{active:true,googlePlay:true,institution:false});
    assert.equal(firestore.get("institutionContractsV1/"+id).data.seatsRevoked,1);
    assert.equal((await handlers.getInstitutionContractSummary(
      adminRequest({contractId:id}))).currentlyGranted,199);
    assert.equal(firestore.get("institutionRedemptionsV1/"+
      policy.codeHash(batch.codes[0])).data.status,"REVOKED");
    await handlers.updateInstitutionContractStatus(adminRequest({
      contractId:id,status:"SUSPENDED"
    }));
    assert.equal((await handlers.getInstitutionContractSummary(
      adminRequest({contractId:id}))).currentlyGranted,0);
    assert.equal((await handlers.getVerifiedPremiumAccess(guardianRequest(
      guardians[1],{studentProfileId:profiles[1]}))).active,false);
    assert.equal((await handlers.getVerifiedPremiumAccess(guardianRequest(
      guardians[0],{studentProfileId:profiles[0]}))).active,true);
    // Default-off safety: deployed code cannot mutate or even read commercial
    // entitlements without a deliberate server-side rollout switch.
    delete process.env.MM_M1_BACKEND_ENABLED;
    await assert.rejects(
      handlers.getVerifiedPremiumAccess(guardianRequest(
        guardians[0],{studentProfileId:profiles[0]})),
      {code:"failed-precondition"});
    if(previousFlag===undefined)delete process.env.MM_M1_BACKEND_ENABLED;
    else process.env.MM_M1_BACKEND_ENABLED=previousFlag;
  });
