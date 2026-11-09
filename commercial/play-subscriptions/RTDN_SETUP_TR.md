# Google Play RTDN kurulumu — Matematik Macerası (2026-11-09)

Google Cloud projesi: **740643037501**. Pub/Sub API etkin.
Google Play Publisher ve Play Integrity API etkin. Dedicated service account
`mm-play-billing-verifier@matematik-macerasi.iam.gserviceaccount.com` in
Cloudflare Worker secrets and separate D1 `3ad1286a-45b3-412e-b09e-0cbbf4943549`
setup complete per owner's successful local output.

## Account-level work that GitHub cannot do for you

1. In Google Cloud Shell for **the same** Cloud project, paste and run the
   contents of `scripts/create-google-pubsub.sh`. It idempotently creates
   `mm-play-rtdn` topic, `mm-play-rtdn-pull` subscription and grants
   only topic publisher permission to the Google Play notifications service
   and only subscription reader permission to the billing verifier account.
   **It does not enable paid Google Cloud billing or app purchases.**
2. In Google Play Console → Matematik Macerası →
   Monetize → Monetization setup → Real-time developer notifications:
   enable notifications, input the **exact full topic** printed by the
   script, and click **Send Test Message**. Save settings.
3. Then run locally in `commercial/play-subscriptions`:
   `powershell -ExecutionPolicy Bypass -File .\scripts\configure-rtdn.ps1 -ProjectId PROJECT_ID_FROM_CLOUD_SHELL`
   Check the Worker `/health` response:
   `enabled:false,rtdnEnabled:true`. The RTDN pull cron runs every five
   minutes; check Cloudflare Worker Logs for delivery/ack errors.
4. Do **not** enable sales until real license-tester purchases monthly and
   yearly work; Play test cards don't charge real money. Still required:
   safe parent purchase flow, account/device binding and restore,
   reconciliation after refunds/voids, subscription expiration,
   app-store internal test, Android release build config and AAB.
   User must explicitly approve final production rollout.

Source: https://developer.android.com/google/play/billing/getting-ready
Source: https://developer.android.com/google/play/billing/test

## Cost limitations

Pub/Sub has a usage-based billing model and free quotas. This bootstrap
does not turn on billing, nor guarantee no charge on accounts with existing
billing. Existing Premium codes and lifetime purchases are untouched.
