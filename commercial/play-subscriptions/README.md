# Google Play monthly/yearly — trusted entitlement policy (staging only)

**Not deployed. Not a working payments server. No recurring sales enabled.**

Pure, dependency-free `src/entitlement.mjs` models entitlement decisions **only after** a backend has queried Google's Android Publisher API (`purchases.subscriptionsv2.get`) with **server-side** credentials and verified the package/token ownership. Never feed arbitrary Android client JSON to this policy.

Local QA: `cd commercial/play-subscriptions && npm test` (Node 20+).

Product ID: `matematik_macerasi_premium_subscription`.
Base plans: `monthly`, `yearly`. Existing non-consumable
`matematik_macerasi_premium` must remain lifetime access.
No one may promote a subscription to permanent ownership.

| Verified state | Entitled? |
|---|---|
| ACTIVE, future expiry | yes |
| IN_GRACE_PERIOD, future expiry | yes |
| CANCELED but paid-through expiry still future | yes |
| ON_HOLD / PAUSED / EXPIRED / PENDING | no |
| missing or expired line item / wrong product | no |
| purchase token unverified or replayed | no: do not call this policy |

Before live launch: implement authenticated purchase-token registration (with Play Integrity / equivalent abuse controls and stable parent account), verify token & package via Android Publisher API, bind token to the proper account, persist expiration and state securely, handle RTDN with Google Pub/Sub authentication, idempotent acknowledgement, refunds and voided purchases, migration, restored devices, no personal data in client logs, and offline entitlement TTL. Run internal Play license-tester purchases and negative test cases.

Changing an Android Gradle flag alone **must not** enable paid subscription sales.

Server-side Play Developer API integration first boundary:
`src/google-play-verifier.mjs` and `tests/google-play-verifier.test.mjs`.
This code fetches a Google-signed **verified** subscriptionsv2 response using
server-only OAuth credentials. It does **not** grant entitlement to a player,
acknowledge purchases, store tokens, handle RTDN or make live subscription
sales possible. Never expose a service account private key in the Android APK.

Pricing and Play Console checklist: `docs/PLAY_SUBSCRIPTIONS_ROLLOUT_TR_2026_11_08.md`.
