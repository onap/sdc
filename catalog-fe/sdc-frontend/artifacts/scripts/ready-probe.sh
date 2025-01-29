#!/bin/sh
health_check_http_code=$(curl -k --max-time 5 -o /dev/null -w '%{http_code}' -X GET --header "Accept: application/json" "http://127.0.0.1:8181//sdc1/rest/healthCheck")
if [ "$health_check_http_code" -eq 200 ]; then
  exit 0
else
  echo "Health check http status: $health_check_http_code"
  exit 1
fi