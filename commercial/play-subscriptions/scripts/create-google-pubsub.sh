#!/usr/bin/env bash
# Run in Google Cloud Shell, already signed in as the Google Play project owner.
# One project only; does not turn on billing, app purchases or Cloudflare sales.
set -euo pipefail
PROJECT_NUMBER="740643037501"
SERVICE="mm-play-billing-verifier@matematik-macerasi.iam.gserviceaccount.com"
PLAY_PUBLISHER="google-play-developer-notifications@system.gserviceaccount.com"
TOPIC="mm-play-rtdn"
SUB="mm-play-rtdn-pull"
if ! command -v gcloud >/dev/null; then echo "Please use Google Cloud Shell"; exit 1; fi
PROJECT_ID="$(gcloud projects describe "$PROJECT_NUMBER" --format='value(projectId)')"
if [ -z "$PROJECT_ID" ]; then echo "Cannot find selected Google project"; exit 1; fi
echo "Setting up RTDN only for $PROJECT_ID ($PROJECT_NUMBER)"
echo "Google API access is already enabled; no paid service or billing activation requested."
if ! gcloud pubsub topics describe "$TOPIC" --project="$PROJECT_ID" >/dev/null 2>&1; then
  gcloud pubsub topics create "$TOPIC" --project="$PROJECT_ID"
fi
if ! gcloud pubsub subscriptions describe "$SUB" --project="$PROJECT_ID" >/dev/null 2>&1; then
  gcloud pubsub subscriptions create "$SUB" --project="$PROJECT_ID" \
    --topic="$TOPIC" --ack-deadline=60 --message-retention-duration=1d \
    --expiration-period=never
fi
ACTUAL_TOPIC="$(gcloud pubsub subscriptions describe "$SUB" --project="$PROJECT_ID" --format='value(topic)')"
EXPECTED_TOPIC="projects/$PROJECT_ID/topics/$TOPIC"
if [ "$ACTUAL_TOPIC" != "$EXPECTED_TOPIC" ]; then
  echo "SAFETY BLOCK: existing Pub/Sub subscription points to wrong topic: $ACTUAL_TOPIC"
  exit 2
fi
gcloud pubsub topics add-iam-policy-binding "$TOPIC" --project="$PROJECT_ID" \
  --member="serviceAccount:$PLAY_PUBLISHER" \
  --role="roles/pubsub.publisher" --quiet >/dev/null
gcloud pubsub subscriptions add-iam-policy-binding "$SUB" --project="$PROJECT_ID" \
  --member="serviceAccount:$SERVICE" \
  --role="roles/pubsub.subscriber" --quiet >/dev/null
echo ""
echo "RTDN_CLOUD_READY"
echo "Play Console RTDN topic: $EXPECTED_TOPIC"
echo "Cloudflare pull subscription: projects/$PROJECT_ID/subscriptions/$SUB"
echo "Google Play Console > Monetize > Monetization setup > Real-time developer notifications"
echo "Paste the topic name, enable notifications and click Send Test Message."
echo "Sales remain disabled. Pub/Sub usage is subject to Google Cloud quotas/billing settings."
