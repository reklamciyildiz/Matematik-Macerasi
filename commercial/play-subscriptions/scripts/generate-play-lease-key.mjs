import {generateKeyPairSync} from "node:crypto";
import {mkdirSync,existsSync,writeFileSync} from "node:fs";
import {resolve,join} from "node:path";
const dir=resolve("private");mkdirSync(dir,{recursive:true});
const pem=join(dir,"play-lease-private.pem");
const b64=join(dir,"play-lease-public.b64.txt");
if(existsSync(pem)||existsSync(b64)){
  if(existsSync(pem)&&existsSync(b64)){
    console.log("Existing private RSA keypair retained.");process.exit(0);
  }
  throw new Error("Partial keypair found. Refusing to rotate silently.");
}
const keys=generateKeyPairSync("rsa",{modulusLength:2048,publicExponent:65537});
writeFileSync(pem,keys.privateKey.export({type:"pkcs8",format:"pem"}),{flag:"wx",mode:0o600});
writeFileSync(b64,keys.publicKey.export({type:"spki",format:"der"}).toString("base64"),{flag:"wx",mode:0o600});
console.log("New independent Google Play RSA keypair stored in git-ignored private/.");
