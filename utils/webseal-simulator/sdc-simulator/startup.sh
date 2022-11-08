#!/bin/sh

JAVA_OPTIONS=" $JAVA_OPTIONS \
		-Xdebug -agentlib:jdwp=transport=dt_socket,address=*:5000,server=y,suspend=n -Xmx128m -Xms128m -Xss1m \
  -Dconfig.home=$JETTY_BASE/config/sdc-simulator \
  -Dlog.home=$JETTY_BASE/logs \
  -Dlogback.configurationFile=$JETTY_BASE/config/sdc-simulator/logback.xml \
  -Djavax.net.ssl.trustStore=$JETTY_BASE/etc/org.onap.sdc.trust.jks \
  -Djavax.net.ssl.trustStorePassword=z+KEj;t+,KN^iimSiS89e#p0 \
  -Djetty.console-capture.dir=$JETTY_BASE/logs"

cd $JETTY_BASE/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}

cd $JETTY_HOME
echo "etc/rewrite-root-to-sdc1.xml" >> $JETTY_HOME/start.d/rewrite.ini
echo "jetty.httpConfig.sendServerVersion=false" >> $JETTY_HOME/start.d/start.ini

java $JAVA_OPTIONS -jar "${JETTY_HOME}/start.jar"
