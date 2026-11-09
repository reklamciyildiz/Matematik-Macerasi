import test from "node:test";
import assert from "node:assert/strict";
import {generateKeyPairSync,webcrypto} from "node:crypto";
import worker from "../src/worker.mjs";
globalThis.crypto ??= webcrypto;
const {privateKey}=generateKeyPairSync("rsa",{modulusLength:2048});
const secret=JSON.stringify({client_email:"unit@example.iam.gserviceaccount.com",
    private_key:privateKey.export({type:"pkcs8",format:"pem"}).toString()});
test("RTDN Google Cloud test message works with paid checkout still DISABLED",async()=>{
  const calls=[];
  const original=globalThis.fetch;
  globalThis.fetch=async (url,opts={})=>{
    calls.push(String(url));
    if(String(url)==="https://oauth2.googleapis.com/token")
      return new Response(JSON.stringify({access_token:"test-oauth-bearer-for-rtdn-1234567"}));
    if(String(url).endsWith(":pull"))return new Response(JSON.stringify({receivedMessages:[{
      ackId:"ack-test",
      message:{data:btoa(JSON.stringify({version:"1.0",packageName:"com.yigit033.matematikmacarasi",
        testNotification:{version:"1.0"}}))}
    }]}));
    if(String(url).endsWith(":acknowledge")) {
      assert.deepEqual(JSON.parse(opts.body),{ackIds:["ack-test"]});
      return new Response("{}");
    }
    throw Error("unexpected url");
  };
  try{
    const jobs=[];
    const env={SUBSCRIPTIONS_SERVER_ENABLED:"false",RTDN_PULL_ENABLED:"true",
      PUBSUB_SUBSCRIPTION_ID:"projects/matematik-macerasi/subscriptions/mm-play-rtdn-pull",
      GOOGLE_SERVICE_ACCOUNT_JSON:secret,DB:{}};
    const health=await worker.fetch(new Request("https://test.example/health"),env);
    assert.deepEqual(await health.json(),{ok:true,enabled:false,rtdnEnabled:true});
    const denied=await worker.fetch(new Request("https://test.example/v1/verify",{method:"POST"}),env);
    assert.equal(denied.status,503);
    worker.scheduled({},env,{waitUntil:p=>jobs.push(p)});
    await Promise.all(jobs);
    assert.ok(calls.some(x=>x.endsWith(":pull")),"scheduled RTDN pull must run");
    assert.ok(calls.some(x=>x.endsWith(":acknowledge")),"test message must be acknowledged");
    assert.ok(!calls.some(x=>x.includes("subscriptionsv2")),"no purchase verification for test messages");
  }finally{globalThis.fetch=original;}
});
test("no RTDN polling occurs when toggle is disabled",async()=>{
  let scheduled=false;
  worker.scheduled({},{RTDN_PULL_ENABLED:"false",DB:{}},
      {waitUntil:()=>{scheduled=true;}});
  assert.equal(scheduled,false);
});
