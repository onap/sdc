#!/bin/sh
if [ -n "${FE_TLS_CERT}" ]; then
  openssl pkcs12 -inkey ${FE_TLS_KEY} -in ${FE_TLS_CERT} -export -out /tmp/keystore.pkcs12 -passin pass:${FE_TLS_PASSWORD} -passout pass:${FE_TLS_PASSWORD}
  keytool -importkeystore -srcstoretype PKCS12 -srckeystore /tmp/keystore.pkcs12 -srcstorepass ${FE_TLS_PASSWORD} -destkeystore ${JETTY_BASE}/${FE_KEYSTORE_PATH} -deststorepass ${FE_KEYSTORE_PASSWORD} -noprompt
fi

if [ -n "${FE_CA_CERT}" ]; then
  keytool -delete -alias sdc-be -storepass ${FE_TRUSTSTORE_PASSWORD} -keystore ${JETTY_BASE}/${FE_TRUSTSTORE_PATH} || true
  keytool -import -alias sdc-be -file ${FE_CA_CERT} -storetype JKS -keystore ${JETTY_BASE}/${FE_TRUSTSTORE_PATH} -storepass ${FE_TRUSTSTORE_PASSWORD} -noprompt
fi
