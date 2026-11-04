#!/bin/sh

# OpenTelemetry Agent Configuration
OTEL_AGENT_PATH="$JETTY_BASE/otel/opentelemetry-javaagent.jar"
OTEL_OPTS=""

if [ -f "$OTEL_AGENT_PATH" ] && [ "${OTEL_ENABLED:-false}" = "true" ]; then
    OTEL_OPTS="-javaagent:$OTEL_AGENT_PATH"
    echo "OpenTelemetry agent enabled"
fi

JAVA_OPTIONS="$JAVA_OPTIONS \
            -Dcom.datastax.driver.USE_NATIVE_CLOCK=false \
            -Dconfig.home=$JETTY_BASE/config \
	    -Duser.dir=$JETTY_BASE \
            -Dlog.home=$JETTY_BASE/logs \
            -Dlogback.configurationFile=$JETTY_BASE/config/onboarding-be/logback.xml \
            -Dconfiguration.yaml=$JETTY_BASE/config/onboarding-be/onboarding_configuration.yaml \
            -Dfeatures.properties=$JETTY_BASE/config/onboarding-be/features.properties \
            -XX:+HeapDumpOnOutOfMemoryError \
            -Dconfig.location=$JETTY_BASE/config/onboarding-be/."

# The passwords of the image's etc/org.onap.sdc.p12 come before JAVA_OPTIONS, so a deployment's own take precedence.
# resources/ holds base_template.yaml and .env; lib/ comes from the jar's manifest.
cd $JETTY_BASE
java $OTEL_OPTS \
     "-Djetty.sslContext.keyStorePassword=?(kP!Yur![*!Y5!E^f(ZKc31" \
     "-Djetty.sslContext.keyManagerPassword=?(kP!Yur![*!Y5!E^f(ZKc31" \
     "-Djetty.sslContext.trustStorePassword=z+KEj;t+,KN^iimSiS89e#p0" \
     $JAVA_OPTIONS -cp "$JETTY_BASE/onboarding-be.jar:$JETTY_BASE/resources" org.openecomp.server.OnboardingBeApplication
