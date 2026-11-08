import assert from "node:assert/strict";
import test from "node:test";
import worker,{validateVerifyBody} from "../src/worker.mjs";
test("unfinished server never accepts payments or grants subscriptions", async()=>{
  const result=await worker.fetch(new Request("https://staging.example/v1/verify",{
    method:"POST",headers:{"content-type":"application/json"},
    body:JSON.stringify({purchaseToken:"fake-token"})
  }),{SUBSCRIPTIONS_SERVER_ENABLED:"false"});
  assert.equal(result.status,503);
  assert.equal((await result.json()).error,"SUBSCRIPTIONS_DISABLED");
});
test("health does not expose service account configuration or D1", async()=>{
  const result=await worker.fetch(new Request("https://staging.example/health"),{
    SUBSCRIPTIONS_SERVER_ENABLED:"false",GOOGLE_SERVICE_ACCOUNT_JSON:"secret"
  });
  assert.equal(result.status,200);
  const body=await result.text();
  assert.equal(body.includes("secret"),false);
  assert.ok(body.includes('"enabled":false'));
});
test("verify input requires token, stable device and integrity attestation",()=>{
  assert.equal(validateVerifyBody({}),false);
  const full={purchaseToken:"genuine-play-token-abc123",
    deviceId:"device-abc123",integrityToken:"i".repeat(80)};
  assert.equal(validateVerifyBody(full),true);
  assert.equal(validateVerifyBody({...full,deviceId:"abc"}),false);
  assert.equal(validateVerifyBody({...full,purchaseToken:"fake"}),false);
  assert.equal(validateVerifyBody({...full,integrityToken:"short"}),false);
});
test("no configured secrets or D1 is a hard server error",async()=>{
  const result=await worker.fetch(new Request("https://staging.example/v1/verify",{
    method:"POST",headers:{"content-type":"application/json"},
    body:"{}"
  }),{SUBSCRIPTIONS_SERVER_ENABLED:"true"});
  assert.equal(result.status,503);
  assert.equal((await result.json()).error,"NOT_CONFIGURED");
});
