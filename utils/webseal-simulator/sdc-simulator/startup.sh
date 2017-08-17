#!/bin/sh

#export CHEFNAME=${ENVNAME}
cd /root/chef-solo
echo "normal['HOST_IP'] = \"${HOST_IP}\"" > /root/chef-solo/cookbooks/sdc-simulator/attributes/default.rb
chef-solo -c solo.rb
#chef-solo -c solo.rb -E ${CHEFNAME}

sed -i '/^set -e/aJAVA_OPTIONS=\"-Xdebug -Xmx128m -Xms128m -Xss1m -Dconfig.home=${JETTY_BASE}/config/sdc-simulator -Dlog.home=${JETTY_BASE}/logs -Dlogback.configurationFile=${JETTY_BASE}/config/sdc-simulator/logback.xml -Djetty.logging.dir=${JETTY_BASE}/logs -Djetty.base=${JETTY_BASE} \"' /docker-entrypoint.sh
#Moty Default WS startup args
#/usr/bin/java -Dconfig.home=/apps/jetty/base/ws/config -Dlog.home=/apps/jetty/base/ws/logs -Dlogback.configurationFile=/apps/jetty/base/ws/logback.xml -Djetty.logging.dir=/apps/jetty/base/ws/logs -Djetty.home=/apps/jetty/jetty-distribution-9.3.6.v20151106 -Djetty.base=/apps/jetty/base/ws -Djava.io.tmpdir=/tmp -jar /apps/jetty/jetty-distribution-9.3.6.v20151106/start.jar jetty.state=/apps/jetty/base/ws/webseal-simulator.state jetty-logging.xml jetty-started.xml 
sed -i '/^set -e/aTMPDIR=${JETTY_BASE}\/temp' /docker-entrypoint.sh

cd /var/lib/jetty
/docker-entrypoint.sh 

#sed -i '/^set -e/aJAVA_OPTIONS=\"-Xdebug -Xmx128m -Xms128m -Xss1m -agentlib:jdwp=transport=dt_socket,address=4002,server=y,suspend=n -Dconfig.home=${JETTY_BASE}/config -Dlog.home=${JETTY_BASE}/logs -Dlogback.configurationFile=${JETTY_BASE}/config/catalog-be/logback.xml -Djetty.logging.dir=${JETTY_BASE}/logs -Djetty.home=/home/vagrant/jetty/jetty-distribution-9.3.6.v20151106 -Djetty.base=${JETTY_BASE} -Djava.io.tmpdir=/home/vagrant/webseal-simulator/tmp -jar /home/vagrant/jetty/jetty-distribution-9.3.6.v20151106/start.jar jetty.state=/home/vagrant/webseal-simulator/webseal-simulator.state jetty-logging.xml jetty-started.xml\"' /docker-entrypoint.sh