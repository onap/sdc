#!/bin/sh

# OpenTelemetry Agent Configuration
OTEL_AGENT_PATH="$JETTY_BASE/otel/opentelemetry-javaagent.jar"
OTEL_OPTS=""

if [ -f "$OTEL_AGENT_PATH" ] && [ "${OTEL_ENABLED:-false}" = "true" ]; then
    OTEL_OPTS="-javaagent:$OTEL_AGENT_PATH"
    echo "OpenTelemetry agent enabled"
fi

JAVA_OPTIONS="$JAVA_OPTIONS \
               -Dconfig.home=$JETTY_BASE/config \
               -Dlog.home=$JETTY_BASE/logs \
               -Dlogback.configurationFile=$JETTY_BASE/config/catalog-fe/logback.xml \
               -Dconfiguration.yaml=$JETTY_BASE/config/catalog-fe/configuration.yaml \
               -Donboarding_configuration.yaml=$JETTY_BASE/config/onboarding-fe/onboarding_configuration.yaml \
               -Djavax.net.ssl.trustStore=$JETTY_BASE/etc/org.onap.sdc.trust.jks \
               -Djavax.net.ssl.trustStorePassword=z+KEj;t+,KN^iimSiS89e#p0"

cd $JETTY_HOME

java $OTEL_OPTS $JAVA_OPTIONS -jar "${JETTY_HOME}/start.jar"
