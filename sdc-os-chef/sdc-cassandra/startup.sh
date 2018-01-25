#!/bin/bash

cd /root/chef-solo
chef-solo -c solo.rb -o recipe[cassandra-actions::01-configureCassandra] -E ${ENVNAME}
rc=$?
if [[ $rc != 0 ]]; then exit $rc; fi

echo "########### starting cassandra ###########"
# start cassandra
/docker-entrypoint.sh cassandra -f &

chef-solo -c solo.rb  -E ${ENVNAME}
if [[ $rc != 0 ]]; then exit $rc; fi

exec "$@";

