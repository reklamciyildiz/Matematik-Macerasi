// Repair Android's public subscription RSA key from the EXISTING local PEM.
// All key derivation and file writing occurs in Node, not PowerShell stdout.
// No private key is printed, replaced, rotated, or uploaded.
import {createPrivateKey,createPublicKey} from "node:crypto";
import {readFileSync,writeFileSync,copyFileSync,existsSync,constants as fsConstants} from "node:fs";
import {homedir} from "node:os";
import {dirname,join,resolve} from "node:path";
import {fileURLToPath} from "node:url";

const scriptDir=dirname(fileURLToPath(import.meta.url));
const root=resolve(scriptDir,"..","..","..");
const privateDir=join(root,"commercial","play-subscriptions","private");
const privateFile=join(privateDir,"play-lease-private.pem");
const publicFile=join(privateDir,"play-lease-public.b64.txt");
const propertiesFile=join(root,"gradle.properties");
const property="MM_PLAY_SUBS_PUBLIC_KEY_B64";
const propertyPattern=/^[ \t]*MM_PLAY_SUBS_PUBLIC_KEY_B64[ \t]*=[^\r\n]*/gm;

function readUtf8(filename) {return readFileSync(filename,"utf8");}
function keyLines(contents) {return [...contents.matchAll(propertyPattern)];}
function normalizedBase64(contents) {
  return contents.replace(/^\uFEFF/,"").replace(/\s+/g,"");
}
function isCanonicalBase64(contents) {
  return /^[A-Za-z0-9+/]+={0,2}$/.test(contents) &&
    contents.length>=200 && contents.length%4===0 &&
    Buffer.from(contents,"base64").toString("base64")===contents;
}

try {
  if(!existsSync(privateFile))
    throw new Error("Existing private PEM missing: commercial/play-subscriptions/private/play-lease-private.pem. Do not generate a replacement.");
  if(!existsSync(propertiesFile))throw new Error("Root gradle.properties missing.");
  const key=createPrivateKey(readFileSync(privateFile));
  if(key.asymmetricKeyType!=="rsa" ||
    (key.asymmetricKeyDetails?.modulusLength??0)<2048)
    throw new Error("Existing signing private key is not compatible RSA-2048+.");
  const canonical=createPublicKey(key).export({type:"spki",format:"der"}).toString("base64");
  if(!isCanonicalBase64(canonical))throw new Error("Unable to derive canonical SPKI public key.");

  // Validate input and higher-priority Gradle overrides before any modification.
  const oldPublic=existsSync(publicFile)?readUtf8(publicFile):null;
  const normalized=oldPublic===null?null:normalizedBase64(oldPublic);
  if(normalized!==null && isCanonicalBase64(normalized) && normalized!==canonical)
    throw new Error("Existing public key is well-formed but DOES NOT MATCH the private key. No changes made.");
  const originalProperties=readUtf8(propertiesFile);
  const matches=keyLines(originalProperties);
  if(matches.length>1)
    throw new Error("Multiple MM_PLAY_SUBS_PUBLIC_KEY_B64 lines in root gradle.properties. No changes made.");
  const gradleHome=process.env.GRADLE_USER_HOME||join(homedir(),".gradle");
  const userPropertiesFile=join(gradleHome,"gradle.properties");
  if(existsSync(userPropertiesFile)){
    const userMatches=keyLines(readUtf8(userPropertiesFile));
    if(userMatches.length>1)
      throw new Error("Duplicate subscription key properties in Gradle user-home. No changes made.");
    if(userMatches.length===1 &&
      userMatches[0][0].split("=").slice(1).join("=").trim()!==canonical)
      throw new Error("Conflicting MM_PLAY_SUBS_PUBLIC_KEY_B64 in Gradle user-home. No changes made.");
  }
  if(process.env.ORG_GRADLE_PROJECT_MM_PLAY_SUBS_PUBLIC_KEY_B64 &&
    process.env.ORG_GRADLE_PROJECT_MM_PLAY_SUBS_PUBLIC_KEY_B64!==canonical)
    throw new Error("Conflicting ORG_GRADLE_PROJECT_MM_PLAY_SUBS_PUBLIC_KEY_B64 environment property. No changes made.");

  const eol=originalProperties.includes("\r\n")?"\r\n":"\n";
  const newProperty=property+"="+canonical;
  const newProperties=matches.length===1?
    originalProperties.replace(propertyPattern,newProperty):
    originalProperties+(originalProperties.endsWith("\n")?"":eol)+newProperty+eol;

  if(oldPublic===null||oldPublic.trim()!==canonical){
    if(oldPublic!==null){
      const backup=publicFile+".invalid-"+Date.now()+"-"+process.pid+".bak";
      copyFileSync(publicFile,backup,fsConstants.COPYFILE_EXCL);
      console.log("Backed up malformed public key in local ignored private directory.");
    }
    writeFileSync(publicFile,canonical+"\n",{encoding:"utf8"});
  }
  if(newProperties!==originalProperties)
    writeFileSync(propertiesFile,newProperties,{encoding:"utf8"});

  const expected={
    MM_SUBSCRIPTIONS_ENABLED:"true",
    MM_PLAY_SUBS_INTERNAL_QA_BUILD:"true",
    MM_PLAY_SUBS_URL:"https://matematik-macerasi-play-subscriptions.matematik-macerasi-yigit033.workers.dev",
    MM_PLAY_INTEGRITY_PROJECT_NUMBER:"740643037501",
  };
  for(const [name,value] of Object.entries(expected)){
    const match=newProperties.match(new RegExp("^\\s*"+name+"\\s*=([^\\r\\n]*)$","m"));
    if(!match||match[1].trim()!==value)
      console.warn("WARNING: Check "+name+" in gradle.properties.");
  }
  console.log("PLAY_SUBS_PUBLIC_KEY_READY: existing RSA key used; root gradle.properties updated.");
  console.log("No private key, Worker secret or Cloudflare deployment was changed.");
} catch(error){
  // Do not emit PEM content or cryptographic parser diagnostics.
  const msg=error instanceof Error && /^(Existing private PEM missing|Root gradle.properties missing|Existing signing private key|Unable to derive|Existing public key is|Multiple MM_PLAY|Duplicate subscription|Conflicting MM_PLAY|Conflicting ORG_GRADLE)/.test(error.message)
    ?error.message:"Existing private PEM unreadable or local file operation failed.";
  console.error("PLAY_SUBS_KEY_REPAIR_FAILED: "+msg);
  process.exitCode=1;
}
