#!/bin/sh

# Environment variables
export behttpport=8080
export permittedAncestors= 
export fe_conf_https_port=9443
export fe_conf_onboard_http_port=8181
export onboarding_be_http_port=8081
export onboarding_be_https_port=8445
export conf_http_port=8181
export conf_https_port=9443
export keystore_path= 
export keystore_password= 
export truststore_path= 
export truststore_password= 
export CATALOG_FACADE_HOST= 
export CATALOG_FACADE_PORT= 
export BASIC_AUTH_USERNAME=testName
export BASIC_AUTH_PASSWORD=testPass
export SECURITY_KEY= 
########### Apply env variables to the existing files ###########

# Apply environment variables to .yaml files in /app/jetty/config directory
for file in /app/jetty/config/*/*.yaml; do
  envsubst < "$file" > "$file.tmp" && mv -f "$file.tmp" "$file"
done
