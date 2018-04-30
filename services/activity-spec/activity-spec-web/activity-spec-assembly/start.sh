#!/bin/sh

JAVA_OPTIONS=" ${JAVA_OPTIONS} \
            -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
            -Dconfig.home=${CONFIG_FILES_DIR} \
            -Dlog.home=${LOGS_DIR} \
            -Dlogback.configurationFile=${CONFIG_FILES_DIR}/logback.xml \
            -Dconfiguration.yaml=${CONFIG_FILES_DIR}/configuration.yaml"

java -jar "$JETTY_HOME/start.jar" ${JAVA_OPTIONS}