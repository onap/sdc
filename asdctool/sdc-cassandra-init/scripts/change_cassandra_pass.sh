#!/bin/sh

# Source the environment file
. /root/scripts/cassandra-env.sh

echo "Changing Cassandra password..."

pass_changed=99
retry_num=1
is_up=0

while [ $is_up -eq 0 ] && [ $retry_num -le 100 ]; do
    echo "Checking if cqlsh can connect to Cassandra..."
    
    # Try connecting with default credentials
    echo "exit" | cqlsh -u cassandra -p cassandra $CASSANDRA_IP $CASSANDRA_PORT --cqlversion="$cqlversion" >/dev/null 2>&1
    res1=$?

    # Try connecting with the provided password
    echo "exit" | cqlsh -u cassandra -p "$CASSANDRA_PASS" $CASSANDRA_IP $CASSANDRA_PORT --cqlversion="$cqlversion" >/dev/null 2>&1
    res2=$?

    if [ $res1 -eq 0 ] || [ $res2 -eq 0 ]; then
        echo "$(date) --- cqlsh is able to connect."
        is_up=1
    else
        echo "$(date) --- cqlsh is NOT able to connect yet. Sleeping for 5 seconds."
        sleep 5
    fi

    retry_num=$((retry_num + 1))
done

if [ $res1 -eq 0 ] && [ $res2 -ne 0 ] && [ $is_up -eq 1 ]; then
    echo "Modifying Cassandra password"
    echo "ALTER USER cassandra WITH PASSWORD '$CASSANDRA_PASS';" | cqlsh -u cassandra -p cassandra $CASSANDRA_IP $CASSANDRA_PORT --cqlversion="$cqlversion"
elif [ $res1 -ne 0 ] && [ $res2 -eq 0 ] && [ $is_up -eq 1 ]; then
    echo "Cassandra password already modified"
else
    echo "Failed to connect to Cassandra after multiple retries. Exiting."
    exit 1
fi
