#!/bin/sh

export JETTY_HOME=/home/jetty/jetty-distribution-9.2.7.v20150116
export JETTY_BASE=/home/jetty/base

mkdir -p ${JETTY_BASE}
mkdir -p ${JETTY_BASE}/config

cd ${JETTY_BASE}

java -jar $JETTY_HOME/start.jar --add-to-start=deploy
java -jar $JETTY_HOME/start.jar --add-to-startd=http,https,logging,ipaccess

cd -
