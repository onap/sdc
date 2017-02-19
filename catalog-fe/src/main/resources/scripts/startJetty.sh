#!/bin/sh

export JETTY_HOME=/home/jetty/jetty-distribution-9.2.7.v20150116
export JETTY_BASE=/home/jetty/base

eval "jvmargs=`sed '/^#/d'  jvm.properties | paste -s -d"#"`"
jvmargs=`echo $jvmargs | sed 's/#/ /g'`
echo $jvmargs

java  -jar $JETTY_HOME/start.jar $jvmargs $@