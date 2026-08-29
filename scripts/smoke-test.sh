#!/usr/bin/env bash
# =============================================================================
# End-to-end smoke test for the krishna.shop stack (all traffic via the gateway).
#
#   1. register a customer      2. log in (get JWT)
#   3. add a seeded product     4. place an order
#   5. poll the order until the saga settles (CONFIRMED)
#
# Requires: curl. Optional: jq (falls back to grep if absent).
# Run AFTER `docker compose up` reports all services healthy.
# =============================================================================
set -euo pipefail

GATEWAY="${GATEWAY:-http://localhost:8080}"
EMAIL="buyer_$$@example.com"
PASSWORD="Passw0rd!$$"

have_jq() { command -v jq >/dev/null 2>&1; }
json_get() {           # json_get <field> ; reads stdin
  if have_jq; then jq -r ".$1 // empty"
  else grep -o "\"$1\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | head -1 | sed 's/.*:[[:space:]]*"//;s/"$//'
  fi
}

echo "==> 1. Register $EMAIL"
curl -fsS -X POST "$GATEWAY/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"fullName\":\"Smoke Test\"}" >/dev/null

echo "==> 2. Login"
TOKEN=$(curl -fsS -X POST "$GATEWAY/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" | json_get accessToken)
[ -n "$TOKEN" ] || { echo "!! no access token returned"; exit 1; }
AUTH=(-H "Authorization: Bearer $TOKEN")

echo "==> 3. Add product 1 (qty 2) to cart"
curl -fsS -X POST "$GATEWAY/api/cart/items" "${AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"productId":1,"quantity":2}' >/dev/null

echo "==> 4. Place order"
ORDER_ID=$(curl -fsS -X POST "$GATEWAY/api/orders" "${AUTH[@]}" | json_get orderId)
[ -n "$ORDER_ID" ] || { echo "!! no orderId returned"; exit 1; }
echo "    order = $ORDER_ID"

echo "==> 5. Poll order status (saga: RESERVED -> PAID -> CONFIRMED)"
for i in $(seq 1 20); do
  STATUS=$(curl -fsS "$GATEWAY/api/orders/$ORDER_ID" "${AUTH[@]}" | json_get status)
  echo "    [$i] status = ${STATUS:-?}"
  case "$STATUS" in
    CONFIRMED) echo "==> SUCCESS: order confirmed. Check emails at http://localhost:8025"; exit 0 ;;
    FAILED|CANCELLED) echo "!! order ended as $STATUS"; exit 1 ;;
  esac
  sleep 2
done
echo "!! timed out waiting for terminal status"; exit 1
