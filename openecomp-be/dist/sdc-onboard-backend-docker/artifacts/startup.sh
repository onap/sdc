#!/bin/sh

JAVA_OPTIONS="$JAVA_OPTIONS \
            -Dcom.datastax.driver.USE_NATIVE_CLOCK=false \
            -Dconfig.home=$JETTY_BASE/config \
            -Dlog.home=$JETTY_BASE/logs \
            -Dlogback.configurationFile=$JETTY_BASE/config/onboarding-be/logback.xml \
            -Dconfiguration.yaml=$JETTY_BASE/config/onboarding-be/onboarding_configuration.yaml \
            -Dfeatures.properties=$JETTY_BASE/config/onboarding-be/features.properties \
            -XX:+HeapDumpOnOutOfMemoryError \
            -Dconfig.location=$JETTY_BASE/config/onboarding-be/."

cd $JETTY_BASE

cd $JETTY_BASE/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}

cd $JETTY_HOME
echo "jetty.httpConfig.sendServerVersion=false" >> $JETTY_HOME/start.d/start.ini

java $JAVA_OPTIONS -jar "${JETTY_HOME}/start.jar"
