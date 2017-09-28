#!/bin/bash

##############################################################################
###
### generate-cassandra-alter-cql.sh
###
### A script that generates the CQL commands of ALTER for the Cassnadra init.
###
### Usage:
###
###    ./generate-cassandra-alter-cql.sh cassandra-commands.json
###
###
### Author: Avi Ziv
### Version 2.0
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
        $RUN_PATH/parse-json.py -t alter -f $1
}

#### Functions - End    ####

# Check arguements
if [ "$#" -lt 1 ] || [ "$#" -gt 1 ]; then
        usage
fi

main $1
