#!/bin/sh

export CHEFNAME=${ENVNAME}
# executing chef-solo for configuration
cd /root/chef-solo
echo "normal['HOST_IP'] = \"${HOST_IP}\"" > /root/chef-solo/cookbooks/sdc-catalog-be/attributes/default.rb
chef-solo -c solo.rb -E ${CHEFNAME}

sed -i '/^set -e/aJAVA_OPTIONS=\"-Xdebug -agentlib:jdwp=transport=dt_socket,address=4000,server=y,suspend=n -XX:MaxPermSize=256m -Xmx1500m -Dconfig.home=${JETTY_BASE}\/config -Dlog.home=${JETTY_BASE}\/logs -Dlogback.configurationFile=${JETTY_BASE}\/config\/catalog-be\/logback.xml -Dconfiguration.yaml=${JETTY_BASE}\/config\/catalog-be\/configuration.yaml -Dartifactgenerator.config=${JETTY_BASE}\/config\/catalog-be\/Artifact-Generator.properties\ -Donboarding_configuration.yaml=${JETTY_BASE}\/config\/onboarding-be\/onboarding_configuration.yaml" ' /docker-entrypoint.sh
sed -i '/^set -e/aTMPDIR=${JETTY_BASE}\/temp' /docker-entrypoint.sh

# executiong the jetty
cd /var/lib/jetty

/docker-entrypoint.sh &

# check if BackEnd is up
python /root/chef-solo/cookbooks/sdc-normatives/files/default/check_Backend_Health.py

echo "###### DOCKER STARTED #####"

while true; do sleep 2; done



