"use strict";
const {test}=require("node:test");
const assert=require("node:assert/strict");
const P=require("./institution-policy.cjs");
const NOW=1783000000000;
const CONTRACT_ID="a".repeat(32);
const REQUEST_ID="institution-order-2026-01";
const SECRET="not-an-actual-production-key-please-replace-123456";
const contract=(patch={})=>({
  id:CONTRACT_ID,status:"ACTIVE",seats:200,seatsIssued:0,
  expiresAtMs:NOW+365*86400000,activateUntilMs:NOW+70*86400000,...patch
});
test("all 200 contract codes are unique, deterministic and do not disclose UID",()=>{
  const set=new Set();
  for(let i=0;i<200;i++){
    const code=P.deterministicCode(SECRET,CONTRACT_ID,REQUEST_ID,i);
    assert.equal(P.normalizeCode(code.toLowerCase()),code);
    assert.equal(P.deterministicCode(SECRET,CONTRACT_ID,REQUEST_ID,i),code);
    assert.match(P.codeHash(code),/^[0-9a-f]{64}$/);
    assert.ok(!P.codeHash(code).includes(code));
    set.add(code);
  }
  assert.equal(set.size,200);
  assert.throws(()=>P.deterministicCode(SECRET,CONTRACT_ID,REQUEST_ID,200));
  assert.throws(()=>P.deterministicCode("short",CONTRACT_ID,REQUEST_ID,0));
});
test("contract sale requires admin-recorded payment, bounded lifetime and seat count",()=>{
  const base={institutionName:"Math Academy",invoiceReference:"INV-2026-122",
    paymentRecorded:true,seats:200,expiresAtMs:NOW+365*86400000,
    activateUntilMs:NOW+60*86400000};
  assert.equal(P.validateContractInput(base,NOW).seats,200);
  assert.throws(()=>P.validateContractInput({...base,paymentRecorded:false},NOW));
  assert.throws(()=>P.validateContractInput({...base,seats:0},NOW));
  assert.throws(()=>P.validateContractInput({...base,seats:10001},NOW));
  assert.throws(()=>P.validateContractInput({...base,expiresAtMs:NOW+1000},NOW));
  assert.throws(()=>P.validateContractInput({...base,activateUntilMs:NOW+366*86400000},NOW));
});
test("20 and 200 are valid; overselling 201st seat is rejected before write",()=>{
  assert.equal(P.canIssue(contract(),20,NOW),true);
  assert.equal(P.canIssue(contract({seatsIssued:199}),1,NOW),true);
  assert.equal(P.canIssue(contract({seatsIssued:199}),2,NOW),false);
  assert.equal(P.canIssue(contract({seatsIssued:200}),1,NOW),false);
  assert.equal(P.canIssue(contract(),201,NOW),false);
  assert.equal(P.canIssue(contract({status:"SUSPENDED"}),20,NOW),false);
  assert.equal(P.canIssue(contract({activateUntilMs:NOW-1}),20,NOW),false);
});
test("expired or suspended institutions fail closed but paid Play rights survive",()=>{
  const assignment={status:"ACTIVE",contractId:CONTRACT_ID,
    expiresAtMs:NOW+365*86400000};
  const active=P.effectivePremium(null,contract(),assignment,NOW);
  assert.deepEqual(active,{active:true,googlePlay:false,institution:true});
  const play={active:true,platform:"google_play",
    productId:"matematik_macerasi_premium"};
  const suspended=P.effectivePremium(play,contract({status:"SUSPENDED"}),assignment,NOW);
  assert.deepEqual(suspended,{active:true,googlePlay:true,institution:false});
  const refunded=P.effectivePremium({active:false,...play,active:false},contract(),
    assignment,NOW);
  assert.deepEqual(refunded,{active:true,googlePlay:false,institution:true});
  assert.deepEqual(P.effectivePremium(null,contract({expiresAtMs:NOW-1}),
    assignment,NOW),{active:false,googlePlay:false,institution:false});
  assert.deepEqual(P.effectivePremium(null,null,assignment,NOW),
    {active:false,googlePlay:false,institution:false});
});
test("activation deadline differs from actual license expiry",()=>{
  const c=contract({activateUntilMs:NOW-1});
  const assignment={status:"ACTIVE",contractId:CONTRACT_ID,
    expiresAtMs:c.expiresAtMs};
  assert.equal(P.canRedeem(c,NOW),false);
  assert.equal(P.institutionActive(c,assignment,NOW),true);
});
test("unknown, forged or malformed codes always fail validation",()=>{
  for(const code of ["","ABCDE","MM-not-a-code","MM-"+("A".repeat(32)),
    "MM-ABCD/../../../",null,123])assert.throws(()=>P.normalizeCode(code));
  assert.throws(()=>P.validateId("../foo"));
  assert.throws(()=>P.validateRequestId("x"));
});
test("same idempotency request generates same contract and code sequence",()=>{
  const a=P.contractId("admin123","INV-2026-122");
  assert.equal(a,P.contractId("admin123","INV-2026-122"));
  assert.notEqual(a,P.contractId("admin123","INV-2026-123"));
  assert.equal(P.batchId(a,REQUEST_ID),P.batchId(a,REQUEST_ID));
  assert.notEqual(P.deterministicCode(SECRET,CONTRACT_ID,REQUEST_ID,0),
    P.deterministicCode(SECRET,CONTRACT_ID,REQUEST_ID,1));
});
