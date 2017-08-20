#!/bin/sh

export CHEFNAME=${ENVNAME}
cd /root/chef-solo
echo "normal['HOST_IP'] = \"${HOST_IP}\"" > /root/chef-solo/cookbooks/sdc-simulator/attributes/default.rb
chef-solo -c solo.rb -E ${CHEFNAME}

sed -i '/^set -e/aJAVA_OPTIONS=\"-Xdebug -Xmx128m -Xms128m -Xss1m -Dconfig.home=${JETTY_BASE}/config/sdc-simulator -Dlog.home=${JETTY_BASE}/logs -Dlogback.configurationFile=${JETTY_BASE}/config/sdc-simulator/logback.xml -Djetty.logging.dir=${JETTY_BASE}/logs -Djetty.base=${JETTY_BASE} \"' /docker-entrypoint.sh 
sed -i '/^set -e/aTMPDIR=${JETTY_BASE}\/temp' /docker-entrypoint.sh

cd /var/lib/jetty
/docker-entrypoint.sh 

