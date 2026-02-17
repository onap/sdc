#!/bin/sh

# OpenTelemetry Agent Configuration
OTEL_AGENT_PATH="$JETTY_BASE/otel/opentelemetry-javaagent.jar"
OTEL_OPTS=""

if [ -f "$OTEL_AGENT_PATH" ] && [ "${OTEL_ENABLED:-false}" = "true" ]; then
    OTEL_OPTS="-javaagent:$OTEL_AGENT_PATH"
    echo "OpenTelemetry agent enabled"
fi

export JAVA_OPTIONS="$JAVA_OPTIONS -Dconfig.home=$JETTY_BASE/config \
       -Dcom.datastax.driver.USE_NATIVE_CLOCK=false \
       -Dlog.home=$JETTY_BASE/logs \
       -Dlogback.configurationFile=$JETTY_BASE/config/catalog-be/logback.xml \
       -Dconfiguration.yaml=$JETTY_BASE/config/catalog-be/configuration.yaml \
       -Dartifactgenerator.config=$JETTY_BASE/config/catalog-be/Artifact-Generator.properties \
	     -Donboarding_configuration.yaml=$JETTY_BASE/config/onboarding-be/onboarding_configuration.yaml \
       -Djavax.net.ssl.trustStore=$JETTY_BASE/etc/org.onap.sdc.trust.jks \
       -Djavax.net.ssl.trustStorePassword=z+KEj;t+,KN^iimSiS89e#p0"

# Execute Jetty
cd $JETTY_HOME
echo "jetty.httpConfig.sendServerVersion=false" >> $JETTY_HOME/start.d/start.ini
echo "$JETTY_BASE/etc/rewrite-root-to-swagger-ui.xml" >> $JETTY_HOME/start.d/rewrite.ini

java $OTEL_OPTS $JAVA_OPTIONS -jar "$JETTY_HOME/start.jar"
