#!/bin/sh

##############################################################################
###
### zusammen-generate-cassandra-init-cql.sh
###
### A script that generates the CQL commands of CREATE for the Cassnadra init for the Zusammen keyspace.
###
### Usage:
###
###    ./zusammen-generate-cassandra-init-cql.sh cassandra-commands.json
###
###
### Author: Avi Ziv
### Version 1.0
### Date: 23 Apr 2017, first version for Zusammen
###
##############################################################################

#GLOBALS
KEYSPACE_ZUSAMMEN=zusammen_dox

RUN_PATH=$(cd "$(dirname "$0")" && pwd)

#### Functions - Start  ####
usage() { echo "Usage: $0 <db-cql-json-file> keyspace yes/no, for example: $0 cassandra-commands.json keyspace yes" 1>&2; exit 1; }

main()
{
        if [ "$3" = "yes" ]; then
            echo "CREATE KEYSPACE IF NOT EXISTS $KEYSPACE_ZUSAMMEN WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };"
        fi
        echo "USE $KEYSPACE_ZUSAMMEN;"
        $RUN_PATH/parse-json.py -t create -f $1
}

#### Functions - End    ####

# Check arguements
if [ "$#" -lt 1 ] || [ "$#" -gt 3 ]; then
        usage
fi

main $1 $2 $3
