#!/bin/sh


cd /var/lib/jetty/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}
rc=$?
if [[ $rc != 0 ]]; then
    echo "Chef exaction failed."
    exit $rc;
fi


JAVA_OPTIONS=" ${JAVA_OPTIONS} \
            -Dconfig.home=${JETTY_BASE}/config \
            -Dlog.home=${JETTY_BASE}/logs \
            -Dlogback.configurationFile=${JETTY_BASE}/config/onboarding-be/logback.xml \
            -Dconfiguration.yaml=${JETTY_BASE}/config/onboarding-be/onboarding_configuration.yaml \
            -Dconfig.location=${JETTY_BASE}/config/onboarding-be/."

cd /var/lib/jetty

/docker-entrypoint.sh &

while true; do sleep 2; done
