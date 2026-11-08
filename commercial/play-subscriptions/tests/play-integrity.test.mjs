import assert from "node:assert/strict";
import test from "node:test";
import { webcrypto } from "node:crypto";
import {PACKAGE_NAME} from "../src/google-play-verifier.mjs";
import {requestBindingHash,validateIntegrityVerdict,verifyPlayIntegrity} from "../src/play-integrity.mjs";
globalThis.crypto ??= webcrypto;
const now=Date.parse("2026-11-08T20:00:00Z");
const purchaseToken="google-valid-test-purchase-token";
const device="device-valid-abc-123";
const token="google-integrity-opaque-token-must-not-be-sent-to-chat-or-logs";
const response = (hash) => ({tokenPayloadExternal:{
  requestDetails:{requestPackageName:PACKAGE_NAME,requestHash:hash,timestampMillis:String(now)},
  appIntegrity:{packageName:PACKAGE_NAME,appRecognitionVerdict:"PLAY_RECOGNIZED"},
  deviceIntegrity:{deviceRecognitionVerdict:["MEETS_DEVICE_INTEGRITY"]}
}});
test("integrity requires Google-recognized app, device and purchase-bound request hash",async()=>{
  const hash=await requestBindingHash(purchaseToken,device);
  assert.equal(hash.length,64);
  const verdict=response(hash);
  assert.equal(validateIntegrityVerdict(verdict,hash,now),true);
  assert.equal(validateIntegrityVerdict(verdict,"not-same-hash",now),false);
  assert.equal(validateIntegrityVerdict(verdict,hash,now+200000),false);
  verdict.tokenPayloadExternal.appIntegrity.appRecognitionVerdict="UNRECOGNIZED_VERSION";
  assert.equal(validateIntegrityVerdict(verdict,hash,now),false);
});
test("server decodes Play integrity verdict using Google scope",async()=>{
  const hash=await requestBindingHash(purchaseToken,device);
  let called=false;
  const passed=await verifyPlayIntegrity(token,purchaseToken,device,{
    nowMs:now,
    getAccessToken:async(_,args)=>{
      assert.equal(args.scope,"https://www.googleapis.com/auth/playintegrity");
      return "service-account-OAuth-token";
    },
    fetchImpl:async(url,init)=>{
      called=true;
      assert.ok(url.endsWith(PACKAGE_NAME+":decodeIntegrityToken"));
      assert.equal(init.headers.authorization,"Bearer service-account-OAuth-token");
      return new Response(JSON.stringify(response(hash)));
    },
  });
  assert.equal(passed,true);assert.equal(called,true);
});
test("mismatched token/device and invalid API response are rejected",async()=>{
  const hash=await requestBindingHash(purchaseToken,device);
  await assert.rejects(verifyPlayIntegrity(token,purchaseToken,"different-device-123",{
    nowMs:now,getAccessToken:async()=>"ok",
    fetchImpl:async()=>new Response(JSON.stringify(response(hash)))
  }),/PLAY_INTEGRITY_REJECTED/);
  await assert.rejects(verifyPlayIntegrity(token,purchaseToken,device,{
    nowMs:now,getAccessToken:async()=>"ok",
    fetchImpl:async()=>new Response("{}",{status:403})
  }),/PLAY_INTEGRITY_DECODE_FAILED/);
});
