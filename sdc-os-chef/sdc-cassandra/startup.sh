#!/bin/bash

cd /root/chef-solo
mkdir -p /root/chef-solo/cookbooks/cassandra-actions/attributes
echo "normal['version'] = \"${RELEASE}\""  > /root/chef-solo/cookbooks/cassandra-actions/attributes/default.rb
echo "normal['HOST_IP'] = \"${HOST_IP}\"" >> /root/chef-solo/cookbooks/cassandra-actions/attributes/default.rb

export CHEFNAME=${ENVNAME}

sed -i "s/HOSTIP/${HOST_IP}/g" /root/chef-solo/cookbooks/cassandra-actions/recipes/02-createCsUser.rb
sed -i "s/HOSTIP/${HOST_IP}/g" /root/chef-solo/cookbooks/cassandra-actions/recipes/03-createDoxKeyspace.rb
sed -i "s/HOSTIP/${HOST_IP}/g" /root/chef-solo/cookbooks/cassandra-actions/recipes/04-schemaCreation.rb

chef-solo -c solo.rb -o recipe[cassandra-actions::01-configureCassandra] -E ${CHEFNAME}
rc=$?

if [[ $rc != 0 ]]; then exit $rc; fi
echo "########### starting cassandra ###########"
# start cassandra
/docker-entrypoint.sh cassandra -f &

sleep 10

chef-solo -c solo.rb  -E ${CHEFNAME}

while true; do sleep 2; done

