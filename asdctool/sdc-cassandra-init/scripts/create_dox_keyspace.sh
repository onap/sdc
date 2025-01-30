#!/bin/sh

# Source the environment file
. /home/sdc/scripts/cassandra-env.sh

if [ -z "$CASSANDRA_IP" ] || [ -z "$CS_PORT" ] || [ -z "$CASSANDRA_PASS" ] || [ -z "$SDC_USER" ] || [ -z "$SDC_PASSWORD" ]; then
    echo "One or more required environment variables are missing!"
    exit 1
fi

CASSANDRA_PORT=$CS_PORT

CASSANDRA_COMMAND="cqlsh -u $SDC_USER -p $SDC_PASSWORD $CASSANDRA_IP $CS_PORT --cqlversion=$cqlversion"

KEYSPACE="CREATE KEYSPACE IF NOT EXISTS dox WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', '$DC_NAME': '1'};"
KEYSPACE1="CREATE KEYSPACE IF NOT EXISTS zusammen_dox WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', '$DC_NAME': '1'};"

echo "run create_dox_keyspace.cql"
echo "$KEYSPACE" > /tmp/config/create_dox_keyspace.cql
echo "$KEYSPACE1" >> /tmp/config/create_dox_keyspace.cql
chmod 555 /tmp/config/create_dox_keyspace.cql
$CASSANDRA_COMMAND -f /tmp/config/create_dox_keyspace.cql > /dev/null 2>&1
res=$(echo "select keyspace_name from system_schema.keyspaces;" | $CASSANDRA_COMMAND | grep -c dox 2>/dev/null)

if [ $res -gt 0 ]; then
    echo "$(date) --- dox keyspace was created"
else
    echo "$(date) --- Failed to create dox keyspace"
fi
