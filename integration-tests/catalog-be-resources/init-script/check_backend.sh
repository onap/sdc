#!/bin/sh


LOG_FILE="/home/onap/logs/"

# Output the logs to both console and file
exec > /home/onap/logs/1script.log 2>&1


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

# Initialize optional flags as empty strings
tls_cert=""
tls_key=""
tls_key_pw=""
ca_cert=""
basic_auth_config=""

# Set TLS configuration flags if using HTTPS
if [ "$protocol" = "https" ]; then
  [ -n "$TLS_CERT" ] && tls_cert="--tls_cert $TLS_CERT"
  [ -n "$TLS_KEY" ] && tls_key="--tls_key $TLS_KEY"
  [ -n "$TLS_KEY_PW" ] && tls_key_pw="--tls_key_pw $TLS_KEY_PW"
  [ -n "$CA_CERT" ] && ca_cert="--ca_cert $CA_CERT"
fi


# Check if both username and password are provided
if [ -n "$BASIC_AUTH_USER" ] && [ -n "$BASIC_AUTH_PASS" ]; then
  # Create just the Base64-encoded value of "username:password"
  basic_auth_config="--header $(echo -n "$BASIC_AUTH_USER:$BASIC_AUTH_PASS" | base64)"
fi



# Execute sdccheckbackend command with the gathered flags and environment variables
echo "Running sdccheckbackend..."

sdccheckbackend -i $BE_IP -p $be_port $basic_auth_config $https_flag $tls_cert $tls_key $tls_key_pw $ca_cert
