#!/bin/sh

# Source the environment file
. /root/scripts/cassandra-env.sh

if [ "$(nodetool status | tail -n2 | grep -Ev '^$')" != "" ]; then
    if echo "exit" | cqlsh -u cassandra -p "$CASSANDRA_PASS" "$CASSANDRA_IP" "$CASSANDRA_PORT" > /dev/null 2>&1; then
        exit 0
    else
        exit 1
    fi
else
    echo "Not Up"
    exit 1
fi
