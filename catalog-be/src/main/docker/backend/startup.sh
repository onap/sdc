#!/bin/sh

export JAVA_OPTIONS="$JAVA_OPTIONS -Dconfig.home=$JETTY_BASE/config \
       -Dcom.datastax.driver.USE_NATIVE_CLOCK=false \
       -Dlog.home=$JETTY_BASE/logs \
       -Dlogback.configurationFile=$JETTY_BASE/config/catalog-be/logback.xml \
       -Dconfiguration.yaml=$JETTY_BASE/config/catalog-be/configuration.yaml \
       -Dartifactgenerator.config=$JETTY_BASE/config/catalog-be/Artifact-Generator.properties \
	     -Donboarding_configuration.yaml=$JETTY_BASE/config/onboarding-be/onboarding_configuration.yaml \
       -Djavax.net.ssl.trustStore=$JETTY_BASE/etc/org.onap.sdc.trust.jks \
       -Djavax.net.ssl.trustStorePassword=z+KEj;t+,KN^iimSiS89e#p0 \
       -Djetty.console-capture.dir=$JETTY_BASE/logs"

cd $JETTY_BASE/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}

# Execute Jetty
cd $JETTY_HOME
echo "jetty.httpConfig.sendServerVersion=false" >> $JETTY_HOME/start.d/start.ini

java $JAVA_OPTIONS -jar "$JETTY_HOME/start.jar"
