#!/bin/sh

export CHEFNAME=${ENVNAME}
cd /root/chef-solo
echo "normal['HOST_IP'] = \"${HOST_IP}\"" > /root/chef-solo/cookbooks/sdc-simulator/attributes/default.rb
chef-solo -c solo.rb -E ${CHEFNAME}

sed -i '/^set -e/aJAVA_OPTIONS=\"-XX:MaxPermSize=256m -Xmx1500m -Dconfig.home=${JETTY_BASE}\/config -Dlog.home=${JETTY_BASE}\/logs -Dlogback.configurationFile=${JETTY_BASE}\/config\/catalog-fe\/logback.xml -Dconfiguration.yaml=${JETTY_BASE}\/config\/catalog-fe\/configuration.yaml -Donboarding_configuration.yaml=${JETTY_BASE}\/config\/onboarding-fe\/onboarding_configuration.yaml\"' /docker-entrypoint.sh
#\"-Xdebug -agentlib:jdwp=transport=dt_socket,address=4002,server=y,suspend=n -Dconfig.home=/home/vagrant/webseal-simulator/config -Dlog.home=/home/vagrant/webseal-simulator/logs -Dlogback.configurationFile=/home/vagrant/webseal-simulator/config/webseal-simulator/logback.xml -Djetty.logging.dir=/home/vagrant/webseal-simulator/logs -Djetty.home=/home/vagrant/jetty/jetty-distribution-9.3.6.v20151106 -Djetty.base=/home/vagrant/webseal-simulator -Djava.io.tmpdir=/home/vagrant/webseal-simulator/tmp -jar /home/vagrant/jetty/jetty-distribution-9.3.6.v20151106/start.jar jetty.state=/home/vagrant/webseal-simulator/webseal-simulator.state jetty-logging.xml jetty-started.xml 

sed -i '/^set -e/aTMPDIR=${JETTY_BASE}\/temp' /docker-entrypoint.sh

cd /var/lib/jetty
/docker-entrypoint.sh 

