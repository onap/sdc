#!/bin/sh

cd /root/chef-solo
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

chef-solo -c solo.rb  -E ${CHEFNAME}

cd /tmp/
/tmp/create_cassandra_user.sh
/tmp/create_dox_keyspace.sh
/bin/chmod +x sdctool/scripts/*.sh
./sdctool/scripts/schemaCreation.sh /tmp/sdctool/config

while true; do sleep 2; done


