#!/bin/sh

# Export other necessary variables
export CASSANDRA_IP="SDC-CS"
export CS_PORT=9042
export SDC_USER="asdc_user"
export SDC_PASSWORD="Aa1234%^!"
export CASSANDRA_PASS=${CS_PASSWORD:-"onap123#@!"}
export DC_NAME="SDC-CS-integration-test"
export cqlversion="3.4.4"
export DISABLE_HTTP="false"
