# cassandra-env.sh

# Cassandra configuration 
export CASSANDRA_IP=${CASSANDRA_HOST:-"127.0.0.1"}
export CASSANDRA_PASS=${CS_PASSWORD:-"onap123#@!"}
export CASSANDRA_PORT=${CS_PORT:-"9042"}  # Correct default port for Cassandra
export cqlversion=${cqlversion:-"3.4.4"}