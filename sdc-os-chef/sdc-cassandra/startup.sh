#!/bin/sh

cd /root/scripts
cp -pr /root/scripts/cassandra.yaml /etc/cassandra/cassandra.yaml
cp -pr /root/scripts/cassandra-rackdc.properties /etc/cassandra/cassandra-rackdc.properties

rc=$?
if [[ $rc != 0 ]]; then exit $rc; fi

echo "########### starting cassandra ###########"

/docker-entrypoint.sh cassandra -f &
sleep 60
sh -x /root/scripts/change_cassandra_pass.sh
sh -x /var/lib/ready_probe.sh

rc=$?
if [[ $rc != 0 ]]; then exit $rc; fi
while true; do sleep 30; done
