#!/bin/sh

# Set environment variables
DISABLE_HTTP="false"
BE_HTTP_PORT="8080"
BE_HTTPS_PORT="8443"
BE_IP="sdc-BE"
BASIC_AUTH_ENABLED="true"
BASIC_AUTH_USER="testName"
BASIC_AUTH_PASS="testPass"

# Set protocol and port based on the HTTP setting
if [ "$DISABLE_HTTP" = "true" ]; then
  protocol="https"
  https_flag="--https"
  be_port=$BE_HTTPS_PORT
else
  protocol="http"
  https_flag=""
  be_port=$BE_HTTP_PORT
fi

basic_auth_config=""

# Check if both username and password are provided for basic authentication
if [ -n "$BASIC_AUTH_USER" ] && [ -n "$BASIC_AUTH_PASS" ]; then
  # Base64 encode the "username:password"
  basic_auth_config="--header $(echo -n "$BASIC_AUTH_USER:$BASIC_AUTH_PASS" | base64)"
fi

# Execute the check backend health command
start_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "[$start_time] Starting sdccheckbackend..."

sdccheckbackend -i $BE_IP -p $be_port $basic_auth_config $https_flag

end_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "[$end_time] Finished sdccheckbackend."

start_ts=$(date -d "$start_time" +%s)
end_ts=$(date -d "$end_time" +%s)
elapsed=$((end_ts - start_ts))
echo "Elapsed time: $elapsed seconds"
