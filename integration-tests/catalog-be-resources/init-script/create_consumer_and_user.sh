#!/bin/sh

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

# Check if both username and password are provided
if [ -n "$BASIC_AUTH_USER" ] && [ -n "$BASIC_AUTH_PASS" ]; then
  # Create just the Base64-encoded value of "username:password"
  basic_auth_config="--header $(echo -n "$BASIC_AUTH_USER:$BASIC_AUTH_PASS" | base64)"
fi


tls_cert=""
tls_key=""
tls_key_pw=""
ca_cert=""

if [ "$protocol" = "https" ]; then
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
fi

# Execute sdcuserinit command
start_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "[$start_time] Starting sdcuserinit..."

sdcuserinit -i $BE_IP -p $be_port $basic_auth_config $user_conf_dir $https_flag $tls_cert $tls_key $tls_key_pw $ca_cert
echo "sdcuserinit executed successfully."

start_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "[$start_time] Starting sdcconsumerinit..."
sdcconsumerinit -i $BE_IP -p $be_port $basic_auth_config $https_flag $tls_cert $tls_key $tls_key_pw $ca_cert

end_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "[$end_time] Finished sdcuserinit and sdcconsumerinit."

start_ts=$(date -d "$start_time" +%s)
end_ts=$(date -d "$end_time" +%s)
elapsed=$((end_ts - start_ts))
echo "Elapsed time: $elapsed seconds"
