#!/bin/sh

JAVA_OPTIONS=" ${JAVA_OPTIONS} -Dconfig.home=${JETTY_BASE}/config -Dlog.home=${JETTY_BASE}/logs -Dlogback.configurationFile=${JETTY_BASE}/config/catalog-be/logback.xml -Dconfiguration.yaml=${JETTY_BASE}/config/catalog-be/configuration.yaml -Dartifactgenerator.config=${JETTY_BASE}/config/catalog-be/Artifact-Generator.properties -Donboarding_configuration.yaml=${JETTY_BASE}/config/onboarding-be/onboarding_configuration.yaml -Dconfig.location=${JETTY_BASE}/config/onboarding-be/."

cd /root/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}

cd /var/lib/jetty
/docker-entrypoint.sh &

while true; do sleep 2; done



