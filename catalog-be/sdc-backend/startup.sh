#!/bin/sh

export JAVA_OPTIONS=" -Dconfig.home=${JETTY_BASE}/config \
       -Dlog.home=${JETTY_BASE}/logs \
       -Dlogback.configurationFile=${JETTY_BASE}/config/catalog-be/logback.xml \
       -Dconfiguration.yaml=${JETTY_BASE}/config/catalog-be/configuration.yaml \
       -Dartifactgenerator.config=${JETTY_BASE}/config/catalog-be/Artifact-Generator.properties \
	   -Donboarding_configuration.yaml=${JETTY_BASE}/config/onboarding-be/onboarding_configuration.yaml \
       -Djavax.net.ssl.trustStore=${JETTY_BASE}/etc/org.onap.sdc.trust.jks \
       -Djavax.net.ssl.trustStorePassword=].][xgtze]hBhz*wy]}m#lf* \
       -Djetty.console-capture.dir=${JETTY_BASE}/logs \
       ${JAVA_OPTIONS} "

cd /root/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}

status=$?
if [ $status != 0 ]; then
    echo "[ERROR] Problem detected while running chef. Aborting !"
    exit 1
fi

# Execute Jetty
cd /var/lib/jetty
/docker-entrypoint.sh &

exec "$@";

while true; do sleep 2; done



