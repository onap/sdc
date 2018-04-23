#!/bin/sh

cd /root/chef-solo
find . -type f -print0 | xargs -0 dos2unix

chef-solo -c solo.rb -E ${ENVNAME}
rc=$?
if [[ $rc != 0 ]]; then
    echo "Chef exaction failed."
    exit $rc;
fi


JAVA_OPTIONS=" ${JAVA_OPTIONS} \
            -Dconfig.home=${JETTY_BASE}/config \
            -Dlog.home=${JETTY_BASE}/logs \
            -Dlogback.configurationFile=${JETTY_BASE}/config/logback.xml \
            -Dconfiguration.yaml=${JETTY_BASE}/config/configuration.yaml"

cd /var/lib/jetty

/docker-entrypoint.sh &

while true; do sleep 2; done