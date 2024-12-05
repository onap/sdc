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
mkdir -p "$JETTY_BASE/config/onboarding-be"
chown -R "$JETTY_USER:$JETTY_GROUP" "$JETTY_BASE/config/onboarding-be"
chmod -R 0755 "$JETTY_BASE/config/onboarding-be"

#Create etc directory
mkdir -p "$JETTY_BASE/etc"
chown -R "$JETTY_USER:$JETTY_GROUP" "$JETTY_BASE/etc"
chmod -R 0755 "$JETTY_BASE/etc"
