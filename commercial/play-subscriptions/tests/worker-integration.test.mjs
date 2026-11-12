import assert from "node:assert/strict";
import test from "node:test";
import {createHash, generateKeyPairSync, verify as verifySignature, webcrypto} from "node:crypto";
import worker from "../src/worker.mjs";

globalThis.crypto ??= webcrypto;
const packageName="com.yigit033.matematikmacarasi";
const product="matematik_macerasi_premium_subscription";
const nowMs=Date.now();
const expiryTime=new Date(nowMs + 5*24*60*60*1000).toISOString();
const {privateKey,publicKey}=generateKeyPairSync("rsa",{modulusLength:2048});
const pem=privateKey.export({type:"pkcs8",format:"pem"}).toString();
const service=JSON.stringify({client_email:"qa@example.iam.gserviceaccount.com",private_key:pem});
const hash=(s)=>createHash("sha256").update(s).digest("hex");
const token="qa-google-billing-purchase-token-valid-123";
const deviceId="qa-device-123456";
const integrityToken="google-standard-integrity-opaque-token-not-a-real-purchase-123";
const bearer="qa-service-account-access-token-with-correct-length";
const payload=()=>({purchaseToken:token,deviceId,integrityToken});

async function runScenario({plan="monthly",state="SUBSCRIPTION_STATE_ACTIVE",
    recognition="PLAY_RECOGNIZED",ackStatus=200}={}) {
  const calls=[];
  const sql=[];
  const env={
    SUBSCRIPTIONS_SERVER_ENABLED:"true",
    GOOGLE_SERVICE_ACCOUNT_JSON:service,
    PLAY_LEASE_PRIVATE_KEY:pem,
    VERIFY_RATE_LIMITER:{limit:async()=>({success:true})},
    DB:{prepare:query=>({bind:(...args)=>({run:async()=>{
      sql.push({query,args});return {success:true};
    }})})},
  };
  const previous=globalThis.fetch;
  globalThis.fetch=async (url,options={})=>{
    calls.push(String(url));
    if(String(url)==="https://oauth2.googleapis.com/token") {
      assert.equal(options.method,"POST");
      return new Response(JSON.stringify({access_token:bearer}));
    }
    if(String(url).includes(":decodeIntegrityToken")) {
      assert.equal(options.method,"POST");
      return new Response(JSON.stringify({tokenPayloadExternal:{
        requestDetails:{
          requestPackageName:packageName,
          requestHash:hash("mm-play-subscription:v1:"+deviceId+":"+token),
          timestampMillis:String(Date.now()),
        },
        appIntegrity:{packageName,appRecognitionVerdict:recognition},
        deviceIntegrity:{deviceRecognitionVerdict:["MEETS_DEVICE_INTEGRITY"]},
      }}));
    }
    if(String(url).includes("purchases/subscriptionsv2/tokens/")) {
      assert.ok(String(url).endsWith(encodeURIComponent(token)));
      return new Response(JSON.stringify({
        acknowledgementState:"ACKNOWLEDGEMENT_STATE_PENDING",
        subscriptionState:state,
        lineItems:[{productId:product,expiryTime,offerDetails:{basePlanId:plan}}],
      }));
    }
    if(String(url).includes(":acknowledge")) {
      assert.equal(options.method,"POST");
      return new Response("{}",{status:ackStatus});
    }
    throw new Error("UNEXPECTED_EXTERNAL_API");
  };
  try {
    const request=new Request("https://subscription-qa.example/v1/verify",{
      method:"POST",headers:{"content-type":"application/json"},
      body:JSON.stringify(payload()),
    });
    const result=await worker.fetch(request,env);
    return {status:result.status,body:await result.json(),calls,sql};
  } finally {globalThis.fetch=previous;}
}

for(const plan of ["monthly","yearly"]) {
  test("mocked Google "+plan+" payment gets a device-bound signed 24h Play lease",async()=>{
    const result=await runScenario({plan});
    assert.equal(result.status,200);
    assert.equal(result.body.status,"ACTIVE");
    assert.equal(result.body.acknowledgement,"CONFIRMED");
    assert.equal(result.body.basePlanId,plan);
    assert.equal(result.calls.filter(s=>s.includes("purchases/subscriptionsv2/tokens/")).length,1);
    assert.equal(result.calls.filter(s=>s.includes(":acknowledge")).length,1);
    assert.equal(result.sql.length,2);
    assert.ok(result.sql.every(row=>!JSON.stringify(row).includes(token)));
    const [header,claims,sig]=result.body.lease.split(".");
    const jwt=JSON.parse(Buffer.from(claims,"base64url").toString());
    const h=JSON.parse(Buffer.from(header,"base64url").toString());
    assert.equal(h.alg,"RS256");
    assert.equal(h.kid,"mmplay1");
    assert.equal(jwt.aud,"matematik-macerasi-play-subscription");
    assert.equal(jwt.plan,plan);
    assert.equal(jwt.device_tag,hash("license-device:v1:"+deviceId));
    assert.equal(jwt.token_hash,hash("subscription-token:v1:"+token));
    assert.ok(jwt.exp-jwt.iat>0 && jwt.exp-jwt.iat<=86400);
    assert.equal(verifySignature("RSA-SHA256",
      Buffer.from(header+"."+claims),publicKey,Buffer.from(sig,"base64url")),true);
  });
}

test("mocked Google account-hold cannot activate Premium or acknowledge",async()=>{
  const r=await runScenario({state:"SUBSCRIPTION_STATE_ON_HOLD"});
  assert.equal(r.status,403);
  assert.equal(r.body.status,"NOT_ENTITLED");
  assert.equal(r.calls.some(x=>x.includes(":acknowledge")),false);
  assert.equal(Object.hasOwn(r.body,"lease"),false);
  assert.equal(r.sql.length,1);
});

test("unrecognized Play app cannot reach paid lookup or D1",async()=>{
  const r=await runScenario({recognition:"UNRECOGNIZED_VERSION"});
  assert.equal(r.status,403);
  assert.equal(r.body.error,"INTEGRITY_DENIED");
  assert.equal(r.calls.some(x=>x.includes("purchases/subscriptionsv2/tokens/")),false);
  assert.equal(r.sql.length,0);
});

test("verified purchase retains signed Premium lease when Google ack is transiently unavailable",async()=>{
  const r=await runScenario({ackStatus:503});
  assert.equal(r.status,200);
  assert.equal(r.body.status,"ACTIVE");
  assert.equal(r.body.acknowledgement,"RETRY_REQUIRED");
  assert.equal(r.calls.filter(s=>s.includes("purchases/subscriptionsv2/tokens/")).length,1);
  assert.equal(r.calls.filter(s=>s.includes(":acknowledge")).length,1);
  assert.ok(r.body.lease.split(".").length===3);
  assert.equal(r.sql.length,2,"grant only verified, persisted purchases");
  assert.ok(r.sql.every(row=>!JSON.stringify(row).includes(token)));
});
