"use strict";

const {createHash, createHmac} = require("node:crypto");

const MAX_BATCH=200;
const MAX_CONTRACT_SEATS=10000;
const CODE_PATTERN=/^MM-(?:[A-F0-9]{4}-){7}[A-F0-9]{4}$/;
const ID_PATTERN=/^[a-f0-9]{32}$/;
const REQUEST_PATTERN=/^[a-zA-Z0-9_-]{16,80}$/;
const DAY=24*60*60*1000;

function hash(value) {
  return createHash("sha256").update(value,"utf8").digest("hex");
}
function contractId(adminUid,invoiceRef){
  return hash("commercial-contract-v1:"+adminUid+":"+invoiceRef).slice(0,32);
}
function batchId(contract,requestId){
  return hash("commercial-batch-v1:"+contract+":"+requestId).slice(0,32);
}
function validateId(id){
  if(typeof id!=="string"||!ID_PATTERN.test(id))throw new Error("INVALID_ID");
  return id;
}
function validateRequestId(value){
  if(typeof value!=="string"||!REQUEST_PATTERN.test(value))throw new Error("INVALID_REQUEST_ID");
  return value;
}
function normalizeCode(value){
  if(typeof value!=="string")throw new Error("INVALID_CODE");
  const code=value.trim().toUpperCase();
  if(!CODE_PATTERN.test(code))throw new Error("INVALID_CODE");
  return code;
}
function codeHash(code){return hash("commercial-code-v1:"+normalizeCode(code));}
function deterministicCode(secret,contract,requestId,index){
  if(typeof secret!=="string"||Buffer.byteLength(secret,"utf8")<32)
    throw new Error("SIGNING_KEY_UNAVAILABLE");
  validateId(contract);
  validateRequestId(requestId);
  if(!Number.isInteger(index)||index<0||index>=MAX_BATCH)
    throw new Error("INVALID_SEAT_INDEX");
  const digest=createHmac("sha256",secret)
    .update("commercial-code-v1:"+contract+":"+requestId+":"+index)
    .digest("hex").slice(0,32).toUpperCase();
  return "MM-"+digest.match(/.{4}/g).join("-");
}
function validateCount(count,limit=MAX_BATCH) {
  if(!Number.isInteger(count)||count<1||count>limit)throw new Error("INVALID_COUNT");
  return count;
}
function validateContractInput(data,now) {
  if(!data||typeof data!=="object")throw new Error("INVALID_CONTRACT");
  const institutionName=data.institutionName;
  if(typeof institutionName!=="string"||institutionName.trim().length<2||
      institutionName.trim().length>100)throw new Error("INVALID_INSTITUTION");
  const invoiceReference=data.invoiceReference;
  if(typeof invoiceReference!=="string"||
     !/^[A-Za-z0-9][A-Za-z0-9_-]{5,79}$/.test(invoiceReference))
    throw new Error("INVALID_INVOICE_REFERENCE");
  const seats=validateCount(data.seats,MAX_CONTRACT_SEATS);
  const expiresAtMs=data.expiresAtMs;
  const activateUntilMs=data.activateUntilMs;
  if(!Number.isSafeInteger(expiresAtMs)||expiresAtMs<=now+DAY||
     expiresAtMs>now+3*366*DAY)throw new Error("INVALID_LICENSE_EXPIRY");
  if(!Number.isSafeInteger(activateUntilMs)||activateUntilMs<=now||
     activateUntilMs>expiresAtMs)throw new Error("INVALID_ACTIVATION_DEADLINE");
  if(data.paymentRecorded!==true)throw new Error("PAYMENT_NOT_RECORDED");
  return {institutionName:institutionName.trim(),invoiceReference,seats,
    expiresAtMs,activateUntilMs};
}
function canIssue(contract,count,now) {
  return Boolean(contract && contract.status==="ACTIVE" &&
    Number.isSafeInteger(contract.seats)&&Number.isSafeInteger(contract.seatsIssued) &&
    now<contract.expiresAtMs && now<contract.activateUntilMs &&
    Number.isInteger(count)&&count>=1&&count<=MAX_BATCH &&
    contract.seatsIssued+count<=contract.seats);
}
function canRedeem(contract,now) {
  return Boolean(contract && contract.status==="ACTIVE" &&
    Number.isSafeInteger(contract.expiresAtMs) &&
    Number.isSafeInteger(contract.activateUntilMs) &&
    now<contract.expiresAtMs && now<contract.activateUntilMs);
}
function institutionActive(contract,assignment,now) {
  return Boolean(assignment && contract &&
    assignment.status==="ACTIVE" && assignment.contractId===contract.id &&
    contract.status==="ACTIVE" && Number.isSafeInteger(contract.expiresAtMs) &&
    Number.isSafeInteger(assignment.expiresAtMs) &&
    now<contract.expiresAtMs && now<assignment.expiresAtMs);
}
/** The sources are independent; a Play refund NEVER revokes a live school seat. */
function effectivePremium(playRecord,contract,assignment,now) {
  const googlePlay=Boolean(playRecord && playRecord.active===true &&
    playRecord.platform==="google_play" && playRecord.productId==="matematik_macerasi_premium");
  const institution=institutionActive(contract,assignment,now);
  return {active:googlePlay||institution,googlePlay,institution};
}
module.exports={
  MAX_BATCH, MAX_CONTRACT_SEATS, contractId,batchId,validateId,
  validateRequestId,normalizeCode,codeHash,deterministicCode,
  validateCount,validateContractInput,canIssue,canRedeem,
  institutionActive,effectivePremium
};
