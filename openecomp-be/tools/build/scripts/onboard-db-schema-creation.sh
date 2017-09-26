#!/bin/bash



# Define the name of the CQL files
CASSANDRA_CQL_FILE=create_dox_db.cql
CASSANDRA_ALTER_CQL_FILE=alter_dox_db.cql

# Generate the create schema CQL file
./generate-cassandra-init-cql.sh ./cassandra-commands.json dox no > ${CASSANDRA_CQL_FILE}
./generate-application-config-insert-cql.sh vsp.schemaTemplates ../../../tools/install/database/schemaTemplates >> ${CASSANDRA_CQL_FILE}
./generate-application-config-insert-cql.sh vsp.monitoring ../../../tools/install/database/monitoring >> ${CASSANDRA_CQL_FILE}
./zusammen-generate-cassandra-init-cql.sh ./zusammen-cassandra-commands.json zusammen_dox no >> ${CASSANDRA_CQL_FILE}

# Generate the alter schema CQL file
./generate-cassandra-alter-cql.sh ./cassandra-commands.json > ${CASSANDRA_ALTER_CQL_FILE}


