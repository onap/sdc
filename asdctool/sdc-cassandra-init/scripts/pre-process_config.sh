#!/bin/sh

# Source the environment file
. /home/sdc/scripts/cassandra-env.sh  # Replace with the actual path to your env file

if [ "$DISABLE_HTTP" = "true" ]; then
  beProtocol="https"
else
  beProtocol="http"
fi

# Replace placeholder in the template
sed -i "s|{{beProtocol}}|$beProtocol|g" /home/sdc/sdctool/config/configuration.yaml
