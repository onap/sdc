#!/bin/sh

# Extract values from environment variables
TLS_CERT=${TLS_CERT}
TLS_KEY=${TLS_KEY}
TLS_PASSWORD=${TLS_PASSWORD}
KEYSTORE_PATH=${KEYSTORE_PATH}
KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD}
CA_CERT=${CA_CERT}
TRUSTSTORE_PATH=${TRUSTSTORE_PATH}
TRUSTSTORE_PASSWORD=${TRUSTSTORE_PASSWORD}
JETTY_BASE=${JETTY_BASE:-"/app/jetty"}

# Print extracted values
echo "Extracted environment variables:"
echo "  TLS_CERT: $TLS_CERT"
echo "  TLS_KEY: $TLS_KEY"
echo "  TLS_PASSWORD: $TLS_PASSWORD"
echo "  KEYSTORE_PATH: $KEYSTORE_PATH"
echo "  KEYSTORE_PASSWORD: $KEYSTORE_PASSWORD"
echo "  CA_CERT: $CA_CERT"
echo "  TRUSTSTORE_PATH: $TRUSTSTORE_PATH"
echo "  TRUSTSTORE_PASSWORD: $TRUSTSTORE_PASSWORD"
echo "  JETTY_BASE: $JETTY_BASE"

# Main logic to generate keystore and truststore
if [ -n $TLS_CERT ]; then
  echo "Generating keystore..."
  openssl pkcs12 -inkey $TLS_KEY -in $TLS_CERT -export \
    -out /tmp/keystore.pkcs12 -passin pass:$TLS_PASSWORD -passout pass:$TLS_PASSWORD

  echo "Importing keystore..."
  keytool -importkeystore -srcstoretype PKCS12 \
    -srckeystore /tmp/keystore.pkcs12 -srcstorepass $TLS_PASSWORD \
    -destkeystore $JETTY_BASE/$KEYSTORE_PATH -deststorepass $KEYSTORE_PASSWORD -noprompt
fi

if [ -n $CA_CERT ]; then
  echo "Deleting existing CA alias..."
  keytool -delete -alias sdc-be \
    -storepass $TRUSTSTORE_PASSWORD -keystore $JETTY_BASE/$TRUSTSTORE_PATH || true

  echo "Generating truststore..."
  keytool -import -alias sdc-be -file $CA_CERT -storetype JKS \
    -keystore $JETTY_BASE/$TRUSTSTORE_PATH -storepass $TRUSTSTORE_PASSWORD -noprompt
fi

echo "Setup completed successfully."
