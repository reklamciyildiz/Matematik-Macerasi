import assert from "node:assert/strict";
import test from "node:test";
import { generateKeyPairSync, webcrypto } from "node:crypto";
import {PACKAGE_NAME, createGoogleAccessToken,
  verifyGoogleSubscriptionToken} from "../src/google-play-verifier.mjs";

globalThis.crypto ??= webcrypto;
const now = Date.parse("2026-11-08T12:00:00Z");
const purchase = (state="SUBSCRIPTION_STATE_ACTIVE") => ({
  acknowledgementState:"ACKNOWLEDGEMENT_STATE_PENDING",
  subscriptionState:state,
  lineItems:[{productId:"matematik_macerasi_premium_subscription",
    expiryTime:"2026-12-08T12:00:00Z",
    offerDetails:{basePlanId:"monthly"}}],
});
const verify = async (body, httpCode=200) => {
  let called = 0;
  const value = await verifyGoogleSubscriptionToken("real-google-purchase-token",{
    nowMs:now,
    getAccessToken:async () => "server-only-access-token",
    fetchImpl:async (url, init) => {
      called++;
      assert.ok(url.includes("applications/"+PACKAGE_NAME+"/purchases/subscriptionsv2/tokens/"));
      assert.equal(init.headers.authorization,"Bearer server-only-access-token");
      return new Response(JSON.stringify(body), {status:httpCode});
    },
  });
  assert.equal(called,1);
  return value;
};
test("server-verified monthly grants only a temporary entitlement result", async () => {
  const result=await verify(purchase());
  assert.equal(result.entitled,true);
  assert.equal(result.basePlanId,"monthly");
  assert.equal(result.needsAcknowledgement,true);
  assert.equal(Object.hasOwn(result,"purchaseToken"),false);
});
test("expired, on-hold, and unrelated products cannot grant Premium", async () => {
  const hold=await verify(purchase("SUBSCRIPTION_STATE_ON_HOLD"));
  assert.equal(hold.entitled,false);
  assert.equal(hold.needsAcknowledgement,false);
  const bad=purchase();bad.lineItems[0].productId="another-product";
  assert.equal((await verify(bad)).entitled,false);
  const expired=purchase();expired.lineItems[0].expiryTime="2026-10-08T12:00:00Z";
  assert.equal((await verify(expired)).entitled,false);
});
test("Google API error and forged or missing tokens fail closed", async () => {
  await assert.rejects(verify(purchase(),403),/PLAY_SUBSCRIPTION_VERIFICATION_FAILED/);
  await assert.rejects(
    verifyGoogleSubscriptionToken("fake",{
      getAccessToken:async () => "token",
    }),/INVALID_PURCHASE_TOKEN/);
});
test("OAuth assertion uses scoped RS256 signature and never sends private PEM", async () => {
  const {privateKey,publicKey}=generateKeyPairSync("rsa",{modulusLength:2048});
  const pem=privateKey.export({type:"pkcs8",format:"pem"}).toString();
  let sent=false;
  const token=await createGoogleAccessToken({
    client_email:"billing-qa@example.iam.gserviceaccount.com",
    private_key:pem,
  },{
    nowMs:now,
    fetchImpl:async (url,opts) => {
      sent=true;
      assert.equal(url,"https://oauth2.googleapis.com/token");
      assert.ok(!opts.body.includes("PRIVATE KEY"));
      const jwt=new URLSearchParams(opts.body).get("assertion");
      const parts=jwt.split(".");
      const {verify}=await import("node:crypto");
      assert.equal(verify("RSA-SHA256",Buffer.from(parts[0]+"."+parts[1]),
        publicKey,Buffer.from(parts[2],"base64url")),true);
      const claims=JSON.parse(Buffer.from(parts[1],"base64url").toString());
      assert.equal(claims.scope,"https://www.googleapis.com/auth/androidpublisher");
      assert.equal(claims.aud,url);
      return new Response(JSON.stringify({access_token:"server-only-access-token"}));
    }
  });
  assert.equal(sent,true);
  assert.equal(token,"server-only-access-token");
});
