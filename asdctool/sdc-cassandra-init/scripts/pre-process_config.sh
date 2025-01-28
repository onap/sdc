#!/bin/sh

set -x  # Enable debug mode

# Check if configuration.yaml exists
if [ -f /home/sdc/sdctool/config/configuration.yaml ]; then
  echo "Found configuration.yaml"
else
  echo "configuration.yaml not found!"
  exit 1
fi
