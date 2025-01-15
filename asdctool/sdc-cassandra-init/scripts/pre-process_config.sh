#!/bin/sh

set -x  # Enable debug mode

# Determine protocol
if [ "$DISABLE_HTTP" = "true" ]; then
  beProtocol="https"
else
  beProtocol="http"
fi
echo "Using protocol: $beProtocol"

# Check if configuration.yaml exists
if [ -f /home/sdc/sdctool/config/configuration.yaml ]; then
  echo "Found configuration.yaml"
else
  echo "configuration.yaml not found!"
  exit 1
fi

# Replace placeholder in the template
sed -i "s|{{beProtocol}}|$beProtocol|g" /home/sdc/sdctool/config/configuration.yaml
if [ $? -eq 0 ]; then
  echo "Placeholder replaced successfully"
else
  echo "Failed to replace placeholder"
  exit 1
fi
