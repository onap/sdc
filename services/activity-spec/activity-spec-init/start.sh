#!/bin/sh

if [[ -z "${CS_USER}" ]]; then
	echo "CS_USER environment variable must be set"
	exit 1
fi

if [[ -z "${CS_PASSWORD}" ]]; then
	echo "CS_PASSWORD environment variable must be set"
	exit 1
fi

if [[ -z "${CS_HOST}" ]]; then
	echo "CS_HOST environment variable must be set"
	exit 1
fi

cqlsh -u $CS_USER -p $CS_PASSWORD -f /create_activityspec_db.cql $CS_HOST $CS_PORT
