#!/bin/sh

JAVA_OPTIONS=" ${JAVA_OPTIONS} -Dconfig.home=${JETTY_BASE}/config -Dlog.home=${JETTY_BASE}/logs -Dlogback.configurationFile=${JETTY_BASE}/config/onboarding-be/logback.xml -Dconfiguration.yaml=${JETTY_BASE}/config/onboarding-be/onboarding_configuration.yaml"

cd /var/lib/jetty

sed -i "s/localhost/$HOST_IP/g" config/onboarding-be/onboarding_configuration.yaml
sed -i "s/CLUSTER_NAME/$SDC_CLUSTER_NAME/g" config/onboarding-be/onboarding_configuration.yaml
sed -i "s/CS_USER/$SDC_USER/g" config/onboarding-be/onboarding_configuration.yaml
sed -i "s/CS_PASS/$SDC_PASSWORD/g" config/onboarding-be/onboarding_configuration.yaml

/docker-entrypoint.sh &

while true; do sleep 2; done