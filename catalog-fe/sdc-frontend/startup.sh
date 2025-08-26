#!/bin/sh

JAVA_OPTIONS="$JAVA_OPTIONS \
               -Dconfig.home=$JETTY_BASE/config \
               -Dapp.config.dir=$JETTY_BASE/config \
               -Dlog.home=$JETTY_BASE/logs \
               -Dlogback.configurationFile=$JETTY_BASE/config/catalog-fe/logback.xml \
               -Dconfiguration.yaml=$JETTY_BASE/config/catalog-fe/configuration.yaml \
               -Donboarding_configuration.yaml=$JETTY_BASE/config/onboarding-fe/onboarding_configuration.yaml \
               -Djavax.net.ssl.trustStore=$JETTY_BASE/etc/org.onap.sdc.trust.jks \
               -Djavax.net.ssl.trustStorePassword=z+KEj;t+,KN^iimSiS89e#p0"

cd $JETTY_HOME

java $JAVA_OPTIONS -jar "${JETTY_HOME}/start.jar"
