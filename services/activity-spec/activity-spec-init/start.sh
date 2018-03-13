#!/bin/bash

if [[ -z "${CASSANDRA_HOST}" ]]; then
	echo "CASSANDRA_HOST environment variable must be set"
	exit 1
fi

cqlsh -f /create_activityspec_db.cql $CASSANDRA_HOST $CASSANDRA_PORT