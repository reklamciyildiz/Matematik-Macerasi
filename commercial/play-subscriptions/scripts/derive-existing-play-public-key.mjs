// Read the EXISTING local Play subscription private key and derive its
// corresponding SPKI public key. Never generates/replaces a keypair or logs PEM.
import {createPrivateKey, createPublicKey} from "node:crypto";
import {readFileSync} from "node:fs";

const filename=process.argv[2];
if(!filename) {
  console.error("Existing local Play subscription private PEM path required.");
  process.exit(2);
}
try {
  const privateKey=createPrivateKey(readFileSync(filename));
  if(privateKey.asymmetricKeyType!=="rsa" ||
      (privateKey.asymmetricKeyDetails?.modulusLength??0)<2048) {
    throw new Error("Not a compatible RSA-2048-or-larger signing key.");
  }
  const publicKey=createPublicKey(privateKey);
  const der=publicKey.export({type:"spki",format:"der"});
  process.stdout.write(der.toString("base64"));
} catch {
  console.error("Cannot derive an RSA public key from the existing private PEM. Keep current Worker keys unchanged.");
  process.exit(2);
}
