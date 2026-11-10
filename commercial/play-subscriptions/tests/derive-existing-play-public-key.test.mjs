import assert from "node:assert/strict";
import test from "node:test";
import {generateKeyPairSync} from "node:crypto";
import {mkdtempSync,writeFileSync,readFileSync,rmSync} from "node:fs";
import {tmpdir} from "node:os";
import {join,resolve} from "node:path";
import {spawnSync} from "node:child_process";

const script=resolve("scripts/derive-existing-play-public-key.mjs");

test("derives only the matching RSA SPKI public key without changing existing PEM",()=>{
  const dir=mkdtempSync(join(tmpdir(),"mm-play-existing-key-"));
  try {
    const keys=generateKeyPairSync("rsa",{modulusLength:2048});
    const pem=keys.privateKey.export({type:"pkcs8",format:"pem"}).toString();
    const path=join(dir,"original-private.pem");
    writeFileSync(path,pem);
    const result=spawnSync(process.execPath,[script,path],{encoding:"utf8"});
    assert.equal(result.status,0,result.stderr);
    assert.equal(result.stdout,
      keys.publicKey.export({type:"spki",format:"der"}).toString("base64"));
    assert.equal(readFileSync(path,"utf8"),pem);
  } finally { rmSync(dir,{recursive:true,force:true}); }
});

test("rejects missing or corrupt private PEM and never creates a new one",()=>{
  const dir=mkdtempSync(join(tmpdir(),"mm-play-key-failure-"));
  try {
    const path=join(dir,"bad-private.pem");
    writeFileSync(path,"NOT_AN_RSA_KEY");
    const result=spawnSync(process.execPath,[script,path],{encoding:"utf8"});
    assert.notEqual(result.status,0);
    assert.equal(result.stdout,"");
    assert.equal(readFileSync(path,"utf8"),"NOT_AN_RSA_KEY");
  } finally { rmSync(dir,{recursive:true,force:true}); }
});
