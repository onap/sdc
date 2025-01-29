#!/bin/sh

#Create temp directory
mkdir -p "$JETTY_BASE/temp"
chown "$JETTY_USER:$JETTY_GROUP" "$JETTY_BASE/temp"
chmod 0755 "$JETTY_BASE/temp"

# Create config directory
mkdir -p "$JETTY_BASE/config"
chown "$JETTY_USER:$JETTY_GROUP" "$JETTY_BASE/config"
chmod 0755 "$JETTY_BASE/config"

# Create onboarding-be directory
mkdir -p "$JETTY_BASE/config/onboarding-fe"
chown -R "$JETTY_USER:$JETTY_GROUP" "$JETTY_BASE/config/onboarding-fe"
chmod -R 0755 "$JETTY_BASE/config/onboarding-fe"

# Create onboarding-be directory
mkdir -p "$JETTY_BASE/config/catalog-fe"
chown -R "$JETTY_USER:$JETTY_GROUP" "$JETTY_BASE/config/catalog-fe"
chmod -R 0755 "$JETTY_BASE/config/catalog-fe"
