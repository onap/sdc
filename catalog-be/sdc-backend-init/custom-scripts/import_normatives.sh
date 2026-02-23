#!/bin/sh


# Set protocol and port based on DISABLE_HTTP
if [ "$DISABLE_HTTP" = "true" ]; then
  protocol="https"
  be_port=$BE_HTTPS_PORT
  param="-i $BE_IP -p $be_port --https"
  
  # Set TLS flags if certificates are provided
  if [ -n "$TLS_CERT" ]; then
    tls_cert="--tls_cert $TLS_CERT"
  fi
  if [ -n "$TLS_KEY" ]; then
    tls_key="--tls_key $TLS_KEY"
  fi
  if [ -n "$TLS_KEY_PW" ]; then
    tls_key_pw="--tls_key_pw $TLS_KEY_PW"
  fi
  if [ -n "$CA_CERT" ]; then
    ca_cert="--ca_cert $CA_CERT"
  fi
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

# Extract normatives tarball and run the initialization command
echo "Extracting normatives.tar.gz and initializing SDC..."
cd /var/tmp/ || exit 1
cp /home/onap/normatives.tar.gz /var/tmp/
tar -xvf /var/tmp/normatives.tar.gz

start_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "[$start_time] Starting sdcinit..."

# Run sdcinit command with the constructed parameters
cd /var/tmp/normatives/import/tosca || exit 1
sdcinit $param $basic_auth_config $tls_cert $tls_key $tls_key_pw $ca_cert

end_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "[$end_time] Done sdcinit."

start_ts=$(date -d "$start_time" +%s)
end_ts=$(date -d "$end_time" +%s)
elapsed=$((end_ts - start_ts))
echo "Elapsed time: $elapsed seconds"

echo "SDC initialization Done."

