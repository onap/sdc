#!/bin/sh

##############################################################################
###
### generate-cassandra-init-cql.sh
###
### A script that generates the CQL commands of CREATE for the Cassnadra init.
###
### Usage:
###
###    ./generate-cassandra-init-cql.sh cassandra-commands.json
###
###
### Author: Avi Ziv
### Version 2.0
### Date: 21 Sep 2016, added support for keyspace yes/no for DevOps build
###
##############################################################################

#GLOBALS

RUN_PATH=$(cd "$(dirname "$0")" && pwd)

#### Functions - Start  ####
usage() { echo "Usage: $0 <db-cql-json-file> keyspace yes/no, for example: $0 cassandra-commands.json keyspace yes" 1>&2; exit 1; }

main()
{
        if [ "$3" = "yes" ]; then
            echo "CREATE KEYSPACE IF NOT EXISTS dox WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };"
        fi
        echo "USE dox;"
        $RUN_PATH/parse-json.py -t create -f $1
}

#### Functions - End    ####

# Check arguements
if [ "$#" -lt 1 ] || [ "$#" -gt 3 ]; then
        usage
fi

main $1 $2 $3
