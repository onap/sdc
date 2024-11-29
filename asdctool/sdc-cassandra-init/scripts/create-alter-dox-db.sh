#!/bin/sh

# Source the environment file
. /home/sdc/scripts/cassandra-env.sh  # Replace with the actual path to your env file

CASSANDRA_COMMAND="cqlsh -u $SDC_USER -p $SDC_PASSWORD $CASSANDRA_IP $CASSANDRA_PORT --cqlversion=$cqlversion"

echo "Running create_dox_db.cql"
chmod 755 /home/sdc/tools/build/scripts/create_dox_db.cql
$CASSANDRA_COMMAND -f /home/sdc/tools/build/scripts/create_dox_db.cql > /dev/null 2>&1

sleep 10

echo "Running alter_dox_db.cql"
chmod 755 /home/sdc/tools/build/scripts/alter_dox_db.cql
$CASSANDRA_COMMAND -f /home/sdc/tools/build/scripts/alter_dox_db.cql > /dev/null 2>&1
