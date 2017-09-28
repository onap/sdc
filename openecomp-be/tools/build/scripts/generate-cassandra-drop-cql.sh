#!/bin/bash

##############################################################################
###
### generate-cassandra-drop-cql.sh
###
### A script that generates the CQL commands of DROP for the Cassnadra init.
###
### Usage:
###
###    ./generate-cassandra-init-cql.sh cassandra-commands.json
###
###
### Author: Avi Ziv
### Version 1.0
### Date: 21 Sep 2016
###
##############################################################################

#GLOBALS

RUN_PATH=$(cd "$(dirname "$0")" && pwd)

#### Functions - Start  ####
usage() { echo "Usage: $0 <db-cql-json-file>, for example: $0 cassandra-commands.json" 1>&2; exit 1; }

main()
{
        echo "USE dox;"
        $RUN_PATH/parse-json.py -t drop -f $1
}

#### Functions - End    ####

# Check arguements
if [ "$#" -lt 1 ] || [ "$#" -gt 1 ]; then
        usage
fi

main $1
