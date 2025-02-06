#!/bin/sh

# Set environment variables
DISABLE_HTTP="false"
BE_HTTP_PORT="8080"
BE_HTTPS_PORT="8443"
BE_IP="sdc-BE"
BASIC_AUTH_ENABLED="true"
BASIC_AUTH_USER="testName"
BASIC_AUTH_PASS="testPass"

# Set protocol and port based on DISABLE_HTTP
if [ "$DISABLE_HTTP" = "true" ]; then
  protocol="https"
  be_port=$BE_HTTPS_PORT
  param="-i $BE_IP -p $be_port --https"
else
  protocol="http"
  be_port="$BE_HTTP_PORT"
  param="-i $BE_IP -p $be_port"
fi

# Set basic authentication if enabled
if [ "$BASIC_AUTH_ENABLED" = "true" ]; then
  basic_auth_user="${BASIC_AUTH_USER:-}"
  basic_auth_pass="${BASIC_AUTH_PASS:-}"
  
  if [ -n "$basic_auth_user" ] && [ -n "$basic_auth_pass" ]; then
    basic_auth_config="--header $(echo -n "$basic_auth_user:$basic_auth_pass" | base64)"
  else
    basic_auth_config=""
  fi
else
  basic_auth_config=""
fi

# Copy normatives.tar.gz to /var/tmp and extract it
echo "Extracting normatives.tar.gz..."
cp /home/onap/normatives.tar.gz /var/tmp/
cd /var/tmp/ || exit 1
tar -xvf normatives.tar.gz

# Run sdcinit with the constructed parameters
start_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "[$start_time] Starting sdcinit..."

cd /var/tmp/normatives/import/tosca || exit 1
sdcinit $param $basic_auth_config > "/home/onap/logs/init.log" 2>&1

end_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "[$end_time] Done sdcinit."

start_ts=$(date -d "$start_time" +%s)
end_ts=$(date -d "$end_time" +%s)
elapsed=$((end_ts - start_ts))
echo "Elapsed time: $elapsed seconds"

echo "SDC initialization done. Logs can be found at ${ONAP_LOG}/init.log"
