#!/bin/sh

JAVA_OPTIONS="$JAVA_OPTIONS \
               -Dconfig.home=$JETTY_BASE/config \
               -Dlog.home=$JETTY_BASE/logs \
               -Dlogback.configurationFile=$JETTY_BASE/config/catalog-fe/logback.xml \
               -Dconfiguration.yaml=$JETTY_BASE/config/catalog-fe/configuration.yaml \
               -Donboarding_configuration.yaml=$JETTY_BASE/config/onboarding-fe/onboarding_configuration.yaml \
               -Djavax.net.ssl.trustStore=$JETTY_BASE/etc/org.onap.sdc.trust.jks \
               -Djavax.net.ssl.trustStorePassword=z+KEj;t+,KN^iimSiS89e#p0 \
               -Djetty.console-capture.dir=$JETTY_BASE/logs"

cd $JETTY_BASE/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}

cd $JETTY_HOME
echo "etc/rewrite-root-to-sdc1.xml" >> $JETTY_HOME/start.d/rewrite.ini
echo "jetty.httpConfig.sendServerVersion=false" >> $JETTY_HOME/start.d/start.ini

java $JAVA_OPTIONS -jar "${JETTY_HOME}/start.jar"
