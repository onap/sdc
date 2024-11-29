#!/bin/sh

export CASSANDRA_IP=${CASSANDRA_IP:-"sdc-cassandra-1"}
export CS_PORT=${CS_PORT:-9042}
export CS_PASSWORD=${CS_PASSWORD:-"onap123#@!"}
export SDC_USER=${SDC_USER:-"asdc_user"}
export SDC_PASSWORD=${SDC_PASSWORD:-"Aa1234%^!"}
export CASSANDRA_PASS=${CS_PASSWORD:-"cassandra"}
export DC_NAME=${DC_NAME:-"dc1"}
export cqlversion=${cqlversion:-"3.4.4"}
export DISABLE_HTTP="false"