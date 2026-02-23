#!/bin/sh

# Set up Jetty modules for sdc-simulator
cd "$JETTY_BASE"
java -jar "$JETTY_HOME/start.jar" --add-to-start=deploy
java -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start=http,https,setuid,rewrite
