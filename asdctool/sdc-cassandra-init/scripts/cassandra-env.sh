# cassandra-env.sh

# Cassandra configuration
export CASSANDRA_HOST=${CASSANDRA_HOST:-sdc-cs}
export CS_PASSWORD=${CS_PASSWORD:-"onap123@#!"}
export CS_PORT=${CS_PORT:-"9042"}  # Correct default port for Cassandra

# SDC user configuration
export SDC_USER=${SDC_USER:-"asdc_user"}
export SDC_PASSWORD=${SDC_PASSWORD:-"Aa1234%^!"}
export CASSANDRA_PASS=${CS_PASSWORD:-"onap123@#!"}
export DC_NAME=${DC_NAME:-"dc1"}
export cqlversion=${cqlversion:-"3.4.6"}
export DISABLE_HTTP="false"