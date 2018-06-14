#!/bin/sh

JAVA_OPTIONS=" ${JAVA_OPTIONS} \
            -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
            -Dconfig.home=${CONFIG_FILES_DIR}/config \
            -Dlog.home=${LOGS_DIR} \
            -Dlogback.configurationFile=${CONFIG_FILES_DIR}/logback.xml \
            -Dconfiguration.yaml=${CONFIG_FILES_DIR}/config/configuration.yaml"

java ${JAVA_OPTIONS} -jar "$JETTY_HOME/start.jar"