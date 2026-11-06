"use strict";
import assert from "node:assert/strict";

const endpoint=process.env.FIREBASE_DATABASE_EMULATOR_HOST || "127.0.0.1:9000";
const namespace="demo-matematik-macerasi-default-rtdb";

async function request(method,path,data,admin=false){
  const url=new URL("http://"+endpoint+"/"+path+".json");
  url.searchParams.set("ns",namespace);
  const res=await fetch(url,{
    method,
    headers:{"content-type":"application/json",...(admin?{authorization:"Bearer owner"}:{})},
    ...(method==="GET"?{}:{body:JSON.stringify(data)})
  });
  const body=await res.text();
  return {status:res.status,body,ok:res.ok};
}
async function allowed(name,method,path,data,admin=false){
  const r=await request(method,path,data,admin);
  assert.ok(r.ok,name+": expected ALLOW; got "+r.status+" "+r.body);
  console.log("ALLOW",name);
}
async function denied(name,method,path,data,admin=false){
  const r=await request(method,path,data,admin);
  assert.ok(!r.ok,name+": expected DENY; got "+r.status+" "+r.body);
  console.log("DENY",name);
}

await allowed("admin seeds fixture","PUT","",{
  app_info:{min_required_version:"1.1.0",force_update:false},
  premium_codes:{
    "VALID-TEST-ONLY":{
      institution:"test",active:true,used_count:0,max_uses:2,
      expiry_date:"2099-12-31"
    },
    "INACTIVE-TEST-ONLY":{
      institution:"test",active:false,used_count:0,max_uses:3,
      expiry_date:"2099-12-31"
    }
  },
  code_activations:{
    "prior-valid-device":{code:"VALID-TEST-ONLY",activated_at:1700000000000}
  }
},true);

await denied("root record enumeration","GET","",null);
await denied("premium code list enumeration","GET","premium_codes",null);
await denied("activation list enumeration","GET","code_activations",null);
await allowed("current app_info read","GET","app_info");
await denied("client app_info update","PUT","app_info/force_update",true);
await allowed("existing legacy exact code read","GET","premium_codes/VALID-TEST-ONLY");
await allowed("existing legacy exact activation read","GET","code_activations/prior-valid-device");
await denied("client overwrites whole code","PUT",
  "premium_codes/VALID-TEST-ONLY",{active:true,max_uses:999});
await denied("client deletes code","DELETE","premium_codes/VALID-TEST-ONLY",null);
await denied("counter skips seats","PUT",
  "premium_codes/VALID-TEST-ONLY/used_count",5);
await denied("counter goes backward","PUT",
  "premium_codes/VALID-TEST-ONLY/used_count",-1);
await denied("inactive code counter update","PUT",
  "premium_codes/INACTIVE-TEST-ONLY/used_count",1);

await allowed("legacy first +1 use","PUT","premium_codes/VALID-TEST-ONLY/used_count",1);
await denied("cannot reset counter","PUT","premium_codes/VALID-TEST-ONLY/used_count",0);
await allowed("legacy second +1 use","PUT","premium_codes/VALID-TEST-ONLY/used_count",2);
await denied("max-uses exceeded","PUT","premium_codes/VALID-TEST-ONLY/used_count",3);

await denied("nonexistent code activation","PUT",
 "code_activations/forged-device",{code:"NONEXISTENT-CODE",activated_at:1700000000000});
await denied("disabled code activation","PUT",
 "code_activations/disabled-device",{code:"INACTIVE-TEST-ONLY",activated_at:1700000000000});
await denied("bad activation types","PUT",
 "code_activations/bad-device",{code:123,activated_at:"yesterday"});
await allowed("legacy new activation","PUT",
 "code_activations/brand-new-device",{code:"VALID-TEST-ONLY",activated_at:1700000000000});
await denied("cannot overwrite activation","PUT",
 "code_activations/prior-valid-device",{code:"VALID-TEST-ONLY",activated_at:1700000000001});
await denied("cannot delete activation","DELETE",
 "code_activations/prior-valid-device",null);
console.log("ALL EMULATOR RULES TESTS PASS (partial protections ONLY).");
