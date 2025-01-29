#!/bin/sh


# Export other necessary variables
export CASSANDRA_IP=${CASSANDRA_IP:-"SDC-CS"}
export CS_PORT=${CS_PORT:-9042}
export SDC_USER=${SDC_USER:-"asdc_user"}
export SDC_PASSWORD=${SDC_PASSWORD:-"Aa1234%^!"}
export CASSANDRA_PASS=${CS_PASSWORD:-"onap123#@!"}
export DC_NAME=${DC_NAME:-"SDC-CS-integration-test"}
export cqlversion=${cqlversion:-"3.4.4"}
export DISABLE_HTTP="false"

# Your Cassandra operations or commands go here
echo "Using the following configuration:"
echo "CASSANDRA_IP: $CASSANDRA_IP"
echo "CS_PORT: $CS_PORT"
echo "CS_PASSWORD: $CS_PASSWORD"
echo "SDC_USER: $SDC_USER"
echo "DC_NAME: $DC_NAME"

# Example command (replace with actual logic)
echo "SELECT release_version FROM system.local;" | cqlsh ${CASSANDRA_IP} ${CS_PORT} -u cassandra -p "${CS_PASSWORD}"
