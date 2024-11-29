#!/bin/bash
set -x  # Enable debug mode

cd /root/scripts || exit 1
cp -pr /root/scripts/cassandra.yaml /etc/cassandra/cassandra.yaml || exit 1
cp -pr /root/scripts/cassandra-rackdc.properties /etc/cassandra/cassandra-rackdc.properties || exit 1

echo "########### starting cassandra ###########"
/docker-entrypoint.sh cassandra -f &

/root/scripts/change_cassandra_pass.sh || echo "Password change failed"
/var/lib/ready_probe.sh || echo "Ready probe failed"

while true; do sleep 30; done
