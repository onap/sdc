#!/bin/bash

health_Check_http_code=$(curl --max-time 5 -o /dev/null -w '%{http_code}' -X GET --header "USER_ID: cs0008" --header "Accept: application/json" "http://127.0.0.1:8080/onboarding-api/v1.0/healthcheck")
if [[ "$health_Check_http_code" -eq 500 ]]; then
   exit 200
else
   exit $health_Check_http_code
fi
