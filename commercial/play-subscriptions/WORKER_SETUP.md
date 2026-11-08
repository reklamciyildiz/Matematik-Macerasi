# Staged Cloudflare Worker for Google Play recurring Premium

**STAGING CODE ONLY · DO NOT DEPLOY YET · SUBSCRIPTION PURCHASES DISABLED**

This Worker is deliberately isolated from the active Premium-code D1.
It contains an authenticated verification route \`POST /v1/verify\` that:
1. Requires Google Play STANDARD Integrity attestation with SHA-256 request
   binding to the install's \`deviceId\` and Play Billing purchase token.
2. Calls Android Publisher \`purchases.subscriptionsv2.get\` with a **server-side**
   service account (never accepts client-produced subscription state).
3. Checks entitlement policy and writes token/device **hashes** to a separate D1.
4. Acknowledges newly granted payments through Google's backend API.
5. Produces a 24-hour, **separate-purpose** RS256 signed subscription lease
   bound to the Android install ID. This is not the old Cloudflare code JWT.

RTDN: Cron pulls a configured Google Cloud Pub/Sub subscription (5-minute cadence),
re-fetches verified Play state, records latest token hash/status, and
acknowledges only successful events. The app must refuse expired leases and
recheck Google purchases on launch. Pub/Sub IAM permissions, state transitions,
acknowledgement, refunds, restored devices, parent flows and Play testing are
**not configured or validated**. Do not assume this staging code is production
safe until all gates below have passed.

## Console / infrastructure still required

- Play Console: subscription product
  \`matematik_macerasi_premium_subscription\`, plans \`monthly\` and \`yearly\`.
- Play Console > Play Integrity API > link Google Cloud project and enable API.
- Google Cloud APIs: Android Publisher API, Play Integrity API and Pub/Sub API.
- Create least-privilege service account with permitted Play Console purchase
  access, Play Integrity decode permission, Pub/Sub pull/ack permissions.
- Cloudflare: isolated D1 instance, Wrangler bindings, migration \`0001.sql\`.
- Cloudflare Worker secrets: \`GOOGLE_SERVICE_ACCOUNT_JSON\` (service acct JSON),
  \`PLAY_LEASE_PRIVATE_KEY\` (separate RSA PKCS8). **Never** commit either.
- Securely configure the corresponding RSA SPKI public key in the Android
  client, not the old license signing key.
- Configure Play Console RTDN topic and a Pub/Sub pull subscription; set
  \`PUBSUB_SUBSCRIPTION_ID\` and verify notifications with sandbox purchases.
- Real Android Play Integrity client and signed-lease validator must be
  implemented, along with parent-only payment UI and restore paths.
- Update production secret flags from false only after real Google test purchase
  → verify → acknowledge → cancellation/renewal → expiration → restore works.
- Audit refunds / Voided Purchases API; at worst an already issued 24-hour
  signed lease permits access until its expiry. Policy should be disclosed.
- Keep old lifetime INAPP rights and Cloudflare code rights separate.

**NO deployment/production migration has been performed by this change.**
The owner must configure the accounts and API privileges; the current ChatGPT
GitHub integration cannot operate an authenticated Play Console.

References:
https://developer.android.com/google/play/billing/security
https://developer.android.com/google/play/integrity/standard
https://developer.android.com/google/play/billing/lifecycle/subscriptions
https://developer.android.com/google/play/billing/rtdn-reference
