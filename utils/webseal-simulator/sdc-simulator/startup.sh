#!/bin/sh



cd /root/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}
rc=$?
if [[ $rc != 0 ]]; then
    echo "Chef exaction failed."
    exit $rc;
fi

JAVA_OPTIONS=" ${JAVA_OPTIONS} \
                -Dconfig.home=${JETTY_BASE}/config/sdc-simulator \
                -Dlog.home=${JETTY_BASE}/logs \
                -Dlogback.configurationFile=${JETTY_BASE}/config/sdc-simulator/logback.xml \
                -Djetty.logging.dir=${JETTY_BASE}/logs"

echo "---------------------------------------------"
echo $TMPDIR
echo $JAVA_OPTIONS
echo $JETTY_BASE
echo "---------------------------------------------"

cd /var/lib/jetty
/docker-entrypoint.sh 

