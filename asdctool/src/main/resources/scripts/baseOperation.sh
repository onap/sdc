#!/bin/bash

CURRENT_DIR=`pwd`
BASEDIR=$(dirname $0)

if [ ${BASEDIR:0:1} = "/" ]
then
                FULL_PATH=$BASEDIR
else
                FULL_PATH=$CURRENT_DIR/$BASEDIR
fi

#echo $FULL_PATH

LIB_DIR=$FULL_PATH/..
BIN_DIR=$FULL_PATH/../bin
CONFIG_DIR=$FULL_PATH/../config

JVM_VERBOSE=-Dverbose

JVM_LOG_FILE="-Dlogback.configurationFile=${CONFIG_DIR}/logback.xml"

######################################################

#BINS=`find $BIN_DIR -name "*.jar" | tr "\\n" ":"`

LIBS=`find $LIB_DIR -name "*.jar" | tr "\\n" ":"`

JARS=$JARS:$BINS:$LIBS


export JARS
export JVM_LOG_FILE
