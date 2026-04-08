#!/usr/bin/env bash
# wait_for_parent_and_create_service.sh
# Usage:
#   ./wait_for_parent_and_create_service.sh <HOST> <PORT> <PARENT_NAME> <SERVICE_JSON> [USER_ID] [TIMEOUT_SECONDS]
# Example:
#   ./wait_for_parent_and_create_service.sh localhost 8080 org.openecomp.resource.abstract.nodes.service ./service.json jh0003 60

HOST="$1"
PORT="$2"
PARENT_NAME="$3"
SERVICE_JSON="$4"
USER_ID="${5:-jh0003}"
TIMEOUT_SECONDS="${6:-60}"

if [ -z "$HOST" ] || [ -z "$PORT" ] || [ -z "$PARENT_NAME" ] || [ -z "$SERVICE_JSON" ]; then
  echo "Usage: $0 <HOST> <PORT> <PARENT_NAME> <SERVICE_JSON> [USER_ID] [TIMEOUT_SECONDS]"
  exit 2
fi

END=$((SECONDS + TIMEOUT_SECONDS))
DELAY=2

echo "Polling Catalog BE for parent name '$PARENT_NAME' on $HOST:$PORT (timeout ${TIMEOUT_SECONDS}s)..."

while [ $SECONDS -lt $END ]; do
  # Query by name
  RESP=$(curl -s -w "\n%{http_code}" "http://$HOST:$PORT/sdc2/rest/v1/catalog/resources?name=$PARENT_NAME")
  HTTP_CODE=$(echo "$RESP" | tail -n1)
  BODY=$(echo "$RESP" | sed '$d')

  if [ "$HTTP_CODE" = "200" ] && echo "$BODY" | grep -q "$PARENT_NAME"; then
    echo "Parent '$PARENT_NAME' visible (HTTP 200). Proceeding to create service..."
    break
  fi

  echo "Not found yet (status=$HTTP_CODE). Sleeping ${DELAY}s..."
  sleep $DELAY
  # exponential backoff but cap at 10s
  if [ $DELAY -lt 10 ]; then
    DELAY=$((DELAY * 2))
    if [ $DELAY -gt 10 ]; then
      DELAY=10
    fi
  fi
done

if [ $SECONDS -ge $END ]; then
  echo "Timed out waiting for parent '$PARENT_NAME' after ${TIMEOUT_SECONDS}s"
  exit 3
fi

# POST service JSON
if [ ! -f "$SERVICE_JSON" ]; then
  echo "Service JSON file not found: $SERVICE_JSON"
  exit 4
fi

echo "Posting service JSON to /sdc2/rest/v1/catalog/services"
HTTP_RESP=$(curl -s -o /dev/stderr -w "%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -H "USER_ID: $USER_ID" \
  --data-binary @"$SERVICE_JSON" \
  "http://$HOST:$PORT/sdc2/rest/v1/catalog/services")

echo "Create service HTTP status: $HTTP_RESP"
exit 0
