/*
 * Staged Cloudflare subscription backend; operator may deploy with sales disabled. Google Play recurring subscriptions backend.
 * Never put Play service account JSON or RSA private keys in Android/GitHub.
 * Env:
 *  SUBSCRIPTIONS_SERVER_ENABLED, GOOGLE_SERVICE_ACCOUNT_JSON,
 *  PLAY_LEASE_PRIVATE_KEY, DB (D1), VERIFY_RATE_LIMITER.
 * For RTDN pull: RTDN_PULL_ENABLED, PUBSUB_SUBSCRIPTION_ID.
 * All purchase tokens are hashed in D1, never returned or logged.
 */
import {
  PACKAGE_NAME, PUBSUB_SCOPE, createGoogleAccessToken,
  verifyGoogleSubscriptionToken,
} from "./google-play-verifier.mjs";
import {verifyPlayIntegrity} from "./play-integrity.mjs";

const MAX_LEASE_SECONDS=24*60*60;
const encoder=new TextEncoder();
const json=(payload,status=200)=>new Response(JSON.stringify(payload),{
  status,headers:{"content-type":"application/json; charset=utf-8",
    "cache-control":"no-store","x-content-type-options":"nosniff"},
});
const digest=async text=>Array.from(new Uint8Array(await crypto.subtle.digest(
  "SHA-256",encoder.encode(text)))).map(n=>n.toString(16).padStart(2,"0")).join("");
const b64url=bytes=>{
  let x="";for(const n of new Uint8Array(bytes))x+=String.fromCharCode(n);
  return btoa(x).replaceAll("+","-").replaceAll("/","_").replace(/=+$/,"");
};
const jwtPart=obj=>b64url(encoder.encode(JSON.stringify(obj)));
function account(env) {
  if (typeof env.GOOGLE_SERVICE_ACCOUNT_JSON!=="string")
    throw new Error("MISSING_PLAY_ACCOUNT");
  const key=JSON.parse(env.GOOGLE_SERVICE_ACCOUNT_JSON);
  if (!key || typeof key.private_key!=="string")
    throw new Error("INVALID_PLAY_ACCOUNT");
  return key;
}
function privateKey(env) {
  const pem=env.PLAY_LEASE_PRIVATE_KEY;
  if (typeof pem!=="string" || !pem.includes("-----BEGIN PRIVATE KEY-----"))
    throw new Error("MISSING_PLAY_LEASE_KEY");
  const val=pem.replace("-----BEGIN PRIVATE KEY-----","")
    .replace("-----END PRIVATE KEY-----","").replace(/\s/g,"");
  if (!/^[A-Za-z0-9+/=]+$/.test(val))throw new Error("INVALID_PLAY_LEASE_KEY");
  return crypto.subtle.importKey("pkcs8",
    Uint8Array.from(atob(val),c=>c.charCodeAt(0)),
    {name:"RSASSA-PKCS1-v1_5",hash:"SHA-256"},false,["sign"]);
}
async function signShortLease(env,{deviceId,tokenHash,plan,expiryTime},nowMs) {
  const iat=Math.floor(nowMs/1000);
  const max=Math.min(Math.floor(Date.parse(expiryTime)/1000),
    iat+MAX_LEASE_SECONDS);
  if(!Number.isFinite(max) || max<=iat)throw new Error("NO_ENTITLEMENT_TO_SIGN");
  const header=jwtPart({alg:"RS256",typ:"JWT",kid:"mmplay1"});
  const payload=jwtPart({v:1,aud:"matematik-macerasi-play-subscription",
    token_hash:tokenHash,
    device_tag:await digest("license-device:v1:"+deviceId),
    plan,iat,exp:max});
  const unsigned=header+"."+payload;
  const sig=await crypto.subtle.sign("RSASSA-PKCS1-v1_5",
    await privateKey(env),encoder.encode(unsigned));
  return unsigned+"."+b64url(sig);
}
export function validateVerifyBody(body) {
  return !!(body && !Array.isArray(body) && typeof body==="object" &&
    typeof body.purchaseToken==="string" &&
    body.purchaseToken.length>=16 && body.purchaseToken.length<=4096 &&
    !/\s/.test(body.purchaseToken) &&
    typeof body.deviceId==="string" &&
    /^[A-Za-z0-9_.:-]{8,128}$/.test(body.deviceId) &&
    typeof body.integrityToken==="string" &&
    body.integrityToken.length>=50 && body.integrityToken.length<=14000);
}
async function bodyOf(request) {
  if (!request.headers.get("content-type")?.toLowerCase().startsWith("application/json"))
    throw new Error("NOT_JSON");
  const content=await request.text();
  if (content.length>20000)throw new Error("TOO_LARGE");
  return JSON.parse(content);
}
async function saveReceipt(db,tokenHash,result,nowMs) {
  const expiry= result.entitled ? Math.floor(Date.parse(result.expiryTime)/1000):0;
  const status=result.entitled?"entitled":"not_entitled";
  await db.prepare(
    "INSERT INTO subscription_receipts(token_hash,status,base_plan,expiry_unix,updated_at) "+
    "VALUES(?,?,?,?,?) ON CONFLICT(token_hash) DO UPDATE SET "+
    "status=excluded.status,base_plan=excluded.base_plan,"+
    "expiry_unix=excluded.expiry_unix,updated_at=excluded.updated_at"
  ).bind(tokenHash,status,result.basePlanId,expiry,Math.floor(nowMs/1000)).run();
}
async function acknowledgeGoogle(purchaseToken,env,fetchImpl=fetch,nowMs=Date.now()) {
  const bearer=await createGoogleAccessToken(account(env),{fetchImpl,nowMs});
  const path="https://androidpublisher.googleapis.com/androidpublisher/v3/applications/"+
    PACKAGE_NAME+"/purchases/subscriptions/"+
    "matematik_macerasi_premium_subscription/tokens/"+
    encodeURIComponent(purchaseToken)+":acknowledge";
  const response=await fetchImpl(path,{
    method:"POST",headers:{"authorization":"Bearer "+bearer,
      "content-type":"application/json"},body:"{}"
  });
  if(!response.ok)throw new Error("PLAY_ACKNOWLEDGEMENT_FAILED");
}
async function verifyPurchase(body,env,nowMs=Date.now()) {
  if (!validateVerifyBody(body))return json({error:"INVALID_REQUEST"},400);
  const acct=account(env);
  await verifyPlayIntegrity(body.integrityToken,body.purchaseToken,body.deviceId,{
    serviceAccount:acct,nowMs
  });
  const valid=await verifyGoogleSubscriptionToken(body.purchaseToken,{
    serviceAccount:acct,nowMs
  });
  const tokenHash=await digest("subscription-token:v1:"+body.purchaseToken);
  await saveReceipt(env.DB,tokenHash,valid,nowMs);
  if(!valid.entitled)return json({status:"NOT_ENTITLED"},403);
  if(valid.needsAcknowledgement)await acknowledgeGoogle(body.purchaseToken,env,fetch,nowMs);
  await env.DB.prepare(
    "INSERT INTO subscription_installs(token_hash,device_hash,verified_at)"+
    " VALUES(?,?,?) ON CONFLICT(token_hash,device_hash) DO UPDATE SET"+
    " verified_at=excluded.verified_at"
  ).bind(tokenHash,await digest("subscription-device:v1:"+body.deviceId),
    Math.floor(nowMs/1000)).run();
  const lease=await signShortLease(env,{
    deviceId:body.deviceId,tokenHash,plan:valid.basePlanId,
    expiryTime:valid.expiryTime
  },nowMs);
  return json({status:"ACTIVE",lease,expiryTime:valid.expiryTime,
    basePlanId:valid.basePlanId});
}
function decodePubSubPayload(encoded) {
  if(typeof encoded!=="string" || encoded.length>12000)return null;
  try {return JSON.parse(atob(encoded));}catch {return null;}
}
async function fetchPubSub(env, fetchImpl=fetch,nowMs=Date.now()) {
  const id=env.PUBSUB_SUBSCRIPTION_ID;
  if(typeof id!=="string" ||
     !/^projects\/[A-Za-z0-9_.-]+\/subscriptions\/[A-Za-z0-9_.-]+$/.test(id))
    throw new Error("PUBSUB_NOT_CONFIGURED");
  const bearer=await createGoogleAccessToken(account(env),{
    fetchImpl,nowMs,scope:PUBSUB_SCOPE
  });
  const base="https://pubsub.googleapis.com/v1/"+id;
  const pulled=await fetchImpl(base+":pull",{
    method:"POST",headers:{"authorization":"Bearer "+bearer,
      "content-type":"application/json"},body:JSON.stringify({maxMessages:20})
  });
  if(!pulled.ok)throw new Error("PUBSUB_PULL_FAILED");
  const payload=await pulled.json();
  const ackIds=[];
  const items=Array.isArray(payload.receivedMessages)?payload.receivedMessages:[];
  for(const entry of items) {
    const message=decodePubSubPayload(entry?.message?.data);
    if (!message || message.packageName!==PACKAGE_NAME ||
        !message.subscriptionNotification?.purchaseToken) {
      // Ignore unsupported/test/other-package PubSub events.
      if(typeof entry.ackId==="string")ackIds.push(entry.ackId);
      continue;
    }
    const purchaseToken=message.subscriptionNotification.purchaseToken;
    if(typeof purchaseToken!=="string" || purchaseToken.length<16 ||
       purchaseToken.length>4096)continue;
    try {
      const valid=await verifyGoogleSubscriptionToken(purchaseToken,{
        serviceAccount:account(env),fetchImpl,nowMs
      });
      await saveReceipt(env.DB,
        await digest("subscription-token:v1:"+purchaseToken), valid,nowMs);
      if(typeof entry.ackId==="string")ackIds.push(entry.ackId);
    } catch {
      // Do not acknowledge transient API or DB errors. Pub/Sub will retry.
    }
  }
  if(ackIds.length) {
    const ack=await fetchImpl(base+":acknowledge",{
      method:"POST",headers:{"authorization":"Bearer "+bearer,
        "content-type":"application/json"},body:JSON.stringify({ackIds})
    });
    if(!ack.ok)throw new Error("PUBSUB_ACK_FAILED");
  }
  return {messages:items.length,acknowledged:ackIds.length};
}

export default {
  async fetch(request,env) {
    if(new URL(request.url).pathname==="/health")
      return json({ok:true,enabled:env.SUBSCRIPTIONS_SERVER_ENABLED==="true",
        rtdnEnabled:env.RTDN_PULL_ENABLED==="true"});
    if(env.SUBSCRIPTIONS_SERVER_ENABLED!=="true")
      return json({error:"SUBSCRIPTIONS_DISABLED"},503);
    if(request.method!=="POST" || new URL(request.url).pathname!=="/v1/verify")
      return json({error:"NOT_FOUND"},404);
    if(!env.DB || !env.VERIFY_RATE_LIMITER || !env.GOOGLE_SERVICE_ACCOUNT_JSON ||
       !env.PLAY_LEASE_PRIVATE_KEY)
      return json({error:"NOT_CONFIGURED"},503);
    try {
      const ip=request.headers.get("cf-connecting-ip")||"unidentified";
      const limit=await env.VERIFY_RATE_LIMITER.limit({key:ip});
      if(!limit?.success)return json({error:"RATE_LIMITED"},429);
      return await verifyPurchase(await bodyOf(request),env);
    } catch(error) {
      // Never emit purchase token, private key, device ID or API response text.
      if(error.message==="PLAY_INTEGRITY_REJECTED" ||
         error.message==="INVALID_INTEGRITY_TOKEN" ||
         error.message==="INVALID_BINDING_INPUT")
        return json({error:"INTEGRITY_DENIED"},403);
      return json({error:"VERIFY_UNAVAILABLE"},503);
    }
  },
  async scheduled(_event,env,ctx) {
    // RTDN test messages can be consumed while checkout remains DISABLED.
    // This decouples notification QA from exposing real subscription purchases.
    if(env.RTDN_PULL_ENABLED!=="true" || !env.DB ||
       !env.GOOGLE_SERVICE_ACCOUNT_JSON)return;
    ctx.waitUntil(fetchPubSub(env).catch(()=>{}));
  }
};
