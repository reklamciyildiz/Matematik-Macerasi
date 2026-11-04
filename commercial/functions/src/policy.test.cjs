"use strict";

const {test} = require("node:test");
const assert = require("node:assert/strict");
const {PACKAGE,PREMIUM_PRODUCT,tokenHash,accountTag,classifyGoogleProduct} =
  require("./policy.cjs");

const purchased = (options={}) => ({
  purchaseStateContext:{purchaseState:"PURCHASED"},
  productLineItem:[{productId:PREMIUM_PRODUCT,
    productOfferDetails:{consumptionState:"CONSUMPTION_STATE_YET_TO_BE_CONSUMED",refundableQuantity:1}}],
  ...options
});
test("stable application and existing non-consumable SKU",()=>{
  assert.equal(PACKAGE,"com.yigit033.matematikmacarasi");
  assert.equal(PREMIUM_PRODUCT,"matematik_macerasi_premium");
});
test("purchase is the only granting state",()=>{
  assert.equal(classifyGoogleProduct(purchased(),"parent"),"PURCHASED");
  for (const state of ["PENDING","CANCELLED","PURCHASE_STATE_UNSPECIFIED"]) {
    assert.notEqual(classifyGoogleProduct(purchased({purchaseStateContext:{purchaseState:state}}),"parent"),"PURCHASED");
  }
  assert.equal(classifyGoogleProduct(null,"parent"),"INVALID");
});
test("exact product and single item required",()=>{
  assert.equal(classifyGoogleProduct(purchased({productLineItem:[{productId:"fake"}]}),"p"),"INVALID");
  assert.equal(classifyGoogleProduct(purchased({productLineItem:[]}),"p"),"INVALID");
  assert.equal(classifyGoogleProduct(purchased({productLineItem:[{productId:PREMIUM_PRODUCT},{productId:PREMIUM_PRODUCT}]}),"p"),"INVALID");
});
test("consumed and fully refunded must never unlock Premium",()=>{
  assert.equal(classifyGoogleProduct(purchased({productLineItem:[{productId:PREMIUM_PRODUCT,
    productOfferDetails:{consumptionState:"CONSUMPTION_STATE_CONSUMED"}}]}),"p"),"INVALID");
  assert.equal(classifyGoogleProduct(purchased({productLineItem:[{productId:PREMIUM_PRODUCT,
    productOfferDetails:{refundableQuantity:0}}]}),"p"),"CANCELLED");
});
test("account-bound transactions cannot be rebound",()=>{
  assert.equal(classifyGoogleProduct(purchased({obfuscatedExternalAccountId:accountTag("a")}),"a"),"PURCHASED");
  assert.equal(classifyGoogleProduct(purchased({obfuscatedExternalAccountId:accountTag("a")}),"b"),"WRONG_ACCOUNT");
  assert.notEqual(accountTag("a"),accountTag("b"));
});
test("token is never stored directly as document identifier",()=>{
  const token="token-1234567890";
  assert.match(tokenHash(token),/^[0-9a-f]{64}$/);
  assert.notEqual(tokenHash(token),token);
  assert.equal(tokenHash(token),tokenHash(token));
  assert.throws(()=>tokenHash("short"),/INVALID_TOKEN/);
  assert.throws(()=>tokenHash("with\nnewline000"),/INVALID_TOKEN/);
});
