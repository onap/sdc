#!/bin/sh

# Define environment variables
export JETTY_HOME="/app/jetty/" # Replace with your Jetty home directory
export JETTY_BASE="/app/jetty/" # Replace with your Jetty base directory

# Navigate to the Jetty base directory
cd "$JETTY_BASE"

# Run the Jetty start-up jar with deploy module added
java -jar $JETTY_HOME/start.jar --add-to-start=deploy

# Create startd configuration and add http, https, setuid modules
java -jar $JETTY_HOME/start.jar --create-startd --add-to-start=http,https,setuid,rewrite
