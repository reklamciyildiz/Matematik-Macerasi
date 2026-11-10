import assert from "node:assert/strict";
import test from "node:test";
import {generateKeyPairSync} from "node:crypto";
import {mkdtempSync,mkdirSync,readFileSync,writeFileSync,cpSync,rmSync,readdirSync} from "node:fs";
import {tmpdir} from "node:os";
import {join,resolve} from "node:path";
import {spawnSync} from "node:child_process";

const helper=resolve("scripts/repair-android-play-public-key.mjs");
function setup(publicContents="broken base64"){
  const root=mkdtempSync(join(tmpdir(),"mm-play-repair-"));
  const path=join(root,"commercial","play-subscriptions");
  mkdirSync(join(path,"scripts"),{recursive:true});
  mkdirSync(join(path,"private"),{recursive:true});
  const script=join(path,"scripts","repair-android-play-public-key.mjs");
  cpSync(helper,script);
  const keys=generateKeyPairSync("rsa",{modulusLength:2048});
  const pem=keys.privateKey.export({type:"pkcs8",format:"pem"}).toString();
  const publicKey=keys.publicKey.export({type:"spki",format:"der"}).toString("base64");
  writeFileSync(join(path,"private","play-lease-private.pem"),pem);
  if(publicContents!==null)writeFileSync(join(path,"private","play-lease-public.b64.txt"),publicContents);
  const gradle=join(root,"gradle.properties");
  writeFileSync(gradle,"MM_SUBSCRIPTIONS_ENABLED=true\nMM_PLAY_SUBS_INTERNAL_QA_BUILD=true\nMM_PLAY_SUBS_URL=https://matematik-macerasi-play-subscriptions.matematik-macerasi-yigit033.workers.dev\nMM_PLAY_INTEGRITY_PROJECT_NUMBER=740643037501\n");
  const gradleHome=join(root,"isolated-gradle-home");
  mkdirSync(gradleHome);
  function run(){return spawnSync(process.execPath,[script],{
    encoding:"utf8",env:{...process.env,GRADLE_USER_HOME:gradleHome,ORG_GRADLE_PROJECT_MM_PLAY_SUBS_PUBLIC_KEY_B64:""},
  });}
  return {root,path,gradle,run,pem,publicKey,gradleHome};
}
function cleanup(f){rmSync(f.root,{recursive:true,force:true});}

test("recovers corrupt public key from existing PEM, writes Gradle and remains idempotent",()=>{
  const f=setup("not valid base64");
  try{
    const first=f.run();
    assert.equal(first.status,0,first.stderr);
    assert.match(first.stdout,/PLAY_SUBS_PUBLIC_KEY_READY/);
    const keyFile=join(f.path,"private","play-lease-public.b64.txt");
    assert.equal(readFileSync(keyFile,"utf8").trim(),f.publicKey);
    assert.ok(readFileSync(f.gradle,"utf8").includes("MM_PLAY_SUBS_PUBLIC_KEY_B64="+f.publicKey));
    assert.equal(readFileSync(join(f.path,"private","play-lease-private.pem"),"utf8"),f.pem);
    assert.equal(readdirSync(join(f.path,"private")).filter(x=>x.includes(".invalid-")).length,1);
    const second=f.run();
    assert.equal(second.status,0,second.stderr);
    assert.equal(readdirSync(join(f.path,"private")).filter(x=>x.includes(".invalid-")).length,1);
  } finally {cleanup(f);}
});
test("refuses well-formed but mismatched public key without modifying properties",()=>{
  const unrelated=generateKeyPairSync("rsa",{modulusLength:2048})
    .publicKey.export({type:"spki",format:"der"}).toString("base64");
  const f=setup(unrelated);
  try{
    const old=readFileSync(f.gradle,"utf8");
    const result=f.run();
    assert.notEqual(result.status,0);
    assert.equal(readFileSync(f.gradle,"utf8"),old);
    assert.equal(readFileSync(join(f.path,"private","play-lease-public.b64.txt"),"utf8"),unrelated);
    assert.doesNotMatch(result.stderr,/PRIVATE KEY-----/);
  } finally {cleanup(f);}
});
test("refuses missing private key without creating a replacement",()=>{
  const f=setup(null);
  try{
    rmSync(join(f.path,"private","play-lease-private.pem"));
    const result=f.run();
    assert.notEqual(result.status,0);
    assert.equal(readdirSync(join(f.path,"private")).length,0);
  } finally {cleanup(f);}
});
test("does not overwrite conflicting Gradle user-home properties",()=>{
  const f=setup(null);
  try{
    writeFileSync(join(f.gradleHome,"gradle.properties"),"MM_PLAY_SUBS_PUBLIC_KEY_B64=wrong\n");
    const original=readFileSync(f.gradle,"utf8");
    assert.notEqual(f.run().status,0);
    assert.equal(readFileSync(f.gradle,"utf8"),original);
  } finally {cleanup(f);}
});
