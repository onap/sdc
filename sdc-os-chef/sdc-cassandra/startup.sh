#!/bin/bash

export CHEFNAME=${ENVNAME}
cd /root/chef-solo
chef-solo -c solo.rb -o recipe[cassandra-actions::01-configureCassandra] -E ${CHEFNAME}
rc=$?
if [[ $rc != 0 ]]; then exit $rc; fi

echo "########### starting cassandra ###########"
# start cassandra
/docker-entrypoint.sh cassandra -f &

echo ""#### monitor cassandra init  ###########"
end=$((SECONDS+300))
while [[ $SECONDS -lt $end ]]; do
    /var/lib/ready-probe.sh
    rc=$?
        if [[ $rc == 0 ]]; then
           break
        fi
    sleep 10
done

chef-solo -c solo.rb -o recipe[cassandra-actions::02-changeCSpass] -E ${CHEFNAME}
if [[ $rc != 0 ]]; then exit $rc; fi
while true; do sleep 2; done

