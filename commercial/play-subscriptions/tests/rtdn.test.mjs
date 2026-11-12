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

test("RTDN recovers missing Google subscription acknowledgement, retries transient errors safely",async()=>{
  const calls=[];
  let googleAckStatus=503;
  const original=globalThis.fetch;
  const token="valid-google-play-test-purchase-token-12345";
  const purchaseMessage={
    version:"1.0",packageName:"com.yigit033.matematikmacarasi",
    subscriptionNotification:{version:"1.0",notificationType:4,
      purchaseToken:token,subscriptionId:"matematik_macerasi_premium_subscription"}
  };
  const statements=[];
  const env={
    SUBSCRIPTIONS_SERVER_ENABLED:"true",RTDN_PULL_ENABLED:"true",
    PUBSUB_SUBSCRIPTION_ID:"projects/matematik-macerasi/subscriptions/mm-play-rtdn-pull",
    GOOGLE_SERVICE_ACCOUNT_JSON:secret,
    DB:{prepare:sql=>({bind:(...args)=>({run:async()=>{statements.push({sql,args});}})})}
  };
  globalThis.fetch=async (url,opts={})=>{
    const target=String(url);
    calls.push(target);
    if(target==="https://oauth2.googleapis.com/token")
      return new Response(JSON.stringify({access_token:"valid-rtdn-test-bearer-token-12345"}));
    if(target.endsWith(":pull"))
      return new Response(JSON.stringify({receivedMessages:[{
        ackId:"ack-purchase",message:{data:btoa(JSON.stringify(purchaseMessage))}
      }]}));
    if(target.includes("purchases/subscriptionsv2/tokens/"))
      return new Response(JSON.stringify({
        acknowledgementState:"ACKNOWLEDGEMENT_STATE_PENDING",
        subscriptionState:"SUBSCRIPTION_STATE_ACTIVE",
        lineItems:[{productId:"matematik_macerasi_premium_subscription",
          expiryTime:new Date(Date.now()+86400e3).toISOString(),
          offerDetails:{basePlanId:"monthly"}}]
      }));
    if(target.includes("purchases/subscriptions/") && target.endsWith(":acknowledge"))
      return new Response("{}",{status:googleAckStatus});
    if(target.includes("pubsub.googleapis.com") && target.endsWith(":acknowledge")) {
      assert.deepEqual(JSON.parse(opts.body),{ackIds:["ack-purchase"]});
      return new Response("{}");
    }
    throw new Error("Unexpected RTDN API");
  };
  try {
    const run=async()=>{
      const tasks=[];
      worker.scheduled({},env,{waitUntil:p=>tasks.push(p)});
      await Promise.all(tasks);
    };
    await run();
    assert.equal(calls.filter(x=>x.includes("purchases/subscriptions/") &&
      x.endsWith(":acknowledge")).length,1);
    assert.equal(calls.filter(x=>x.includes("pubsub.googleapis.com") &&
      x.endsWith(":acknowledge")).length,0,
      "do not ack PubSub while Google Play purchase is still unacknowledged");

    googleAckStatus=200;
    await run();
    assert.equal(calls.filter(x=>x.includes("purchases/subscriptions/") &&
      x.endsWith(":acknowledge")).length,2);
    assert.equal(calls.filter(x=>x.includes("pubsub.googleapis.com") &&
      x.endsWith(":acknowledge")).length,1);
    assert.ok(statements.length>=2);
    assert.ok(statements.every(x=>!JSON.stringify(x).includes(token)),
      "purchase tokens must never be stored as raw D1 values");
  } finally { globalThis.fetch=original; }
});

test("RTDN cannot acknowledge inactive Google subscriptions",async()=>{
  const calls=[];
  const previous=globalThis.fetch;
  globalThis.fetch=async (url,opts={})=>{
    const target=String(url);calls.push(target);
    if(target==="https://oauth2.googleapis.com/token")
      return new Response(JSON.stringify({access_token:"valid-rtdn-test-bearer-token-12345"}));
    if(target.endsWith(":pull"))
      return new Response(JSON.stringify({receivedMessages:[{
        ackId:"ack-on-hold",message:{data:btoa(JSON.stringify({
          packageName:"com.yigit033.matematikmacarasi",
          subscriptionNotification:{notificationType:4,
            purchaseToken:"valid-google-play-test-purchase-token-12345"}
        }))}
      }]}));
    if(target.includes("purchases/subscriptionsv2/tokens/"))
      return new Response(JSON.stringify({
        acknowledgementState:"ACKNOWLEDGEMENT_STATE_PENDING",
        subscriptionState:"SUBSCRIPTION_STATE_ON_HOLD",lineItems:[]
      }));
    if(target.includes("pubsub.googleapis.com") && target.endsWith(":acknowledge"))
      return new Response("{}");
    throw new Error("RTDN must not acknowledge denied Google purchase");
  };
  try {
    const jobs=[];
    worker.scheduled({},{
      RTDN_PULL_ENABLED:"true",
      PUBSUB_SUBSCRIPTION_ID:"projects/matematik-macerasi/subscriptions/mm-play-rtdn-pull",
      GOOGLE_SERVICE_ACCOUNT_JSON:secret,
      DB:{prepare:()=>({bind:()=>({run:async()=>{}})})}
    },{waitUntil:p=>jobs.push(p)});
    await Promise.all(jobs);
    assert.equal(calls.filter(s=>s.includes("purchases/subscriptions/") &&
      s.endsWith(":acknowledge")).length,0);
    assert.equal(calls.filter(s=>s.includes("pubsub.googleapis.com") &&
      s.endsWith(":acknowledge")).length,1);
  }finally{globalThis.fetch=previous;}
});
