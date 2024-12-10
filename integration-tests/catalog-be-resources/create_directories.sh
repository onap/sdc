#!/bin/sh

# Set environment variables (replace with actual values)
JETTY_BASE="/app/jetty"
#JETTY_USER="onap"  # Replace with actual user
#JETTY_GROUP="onap"  # Replace with actual group

# Create the temp directory if it doesn't exist
if [ ! -d "$JETTY_BASE/temp" ]; then
  mkdir -p "$JETTY_BASE/temp"
  chown "$JETTY_USER:$JETTY_GROUP" "$JETTY_BASE/temp"
  chmod 755 "$JETTY_BASE/temp"
  echo "Created $JETTY_BASE/temp directory."
fi

# Create the config directory if it doesn't exist
if [ ! -d "$JETTY_BASE/config" ]; then
  mkdir -p "$JETTY_BASE/config"
  chown "$JETTY_USER:$JETTY_GROUP" "$JETTY_BASE/config"
  chmod 755 "$JETTY_BASE/config"
  echo "Created $JETTY_BASE/config directory."
fi

# Create the config/catalog-be directory if it doesn't exist
if [ ! -d "$JETTY_BASE/config/catalog-be" ]; then
  mkdir -p "$JETTY_BASE/config/catalog-be"
  chown "$JETTY_USER:$JETTY_GROUP" "$JETTY_BASE/config/catalog-be"
  chmod 755 "$JETTY_BASE/config/catalog-be"
  echo "Created $JETTY_BASE/config/catalog-be directory."
fi
