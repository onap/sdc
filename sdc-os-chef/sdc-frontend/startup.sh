#!/bin/sh

export CHEFNAME=${ENVNAME}
cd /root/chef-solo
echo "normal['HOST_IP'] = \"${HOST_IP}\"" > /root/chef-solo/cookbooks/sdc-catalog-fe/attributes/default.rb
chef-solo -c solo.rb -E ${CHEFNAME}

sed -i '/^set -e/aJAVA_OPTIONS=\"-XX:MaxPermSize=256m -Xmx512m -Dconfig.home=${JETTY_BASE}\/config -Dlog.home=${JETTY_BASE}\/logs -Dlogback.configurationFile=${JETTY_BASE}\/config\/catalog-fe\/logback.xml -Dconfiguration.yaml=${JETTY_BASE}\/config\/catalog-fe\/configuration.yaml -Donboarding_configuration.yaml=${JETTY_BASE}\/config\/onboarding-fe\/onboarding_configuration.yaml\"' /docker-entrypoint.sh
sed -i '/^set -e/aTMPDIR=${JETTY_BASE}\/temp' /docker-entrypoint.sh

cd /var/lib/jetty
/docker-entrypoint.sh 

