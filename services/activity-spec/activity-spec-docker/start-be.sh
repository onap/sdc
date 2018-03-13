#!/bin/sh

if [[ -z "${CASSANDRA_HOST}" ]]; then
	echo "CASSANDRA_HOST environment variable must be set"
	exit 1
fi

#Replace 'CASSANDRA_HOST' in configuration.yaml with value of CASSANDRA_HOST environment variable
sed -i "s/CASSANDRA_HOST/${CASSANDRA_HOST}/" /var/lib/jetty/configuration.yaml

java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 "$JETTY_HOME/start.jar" -Dconfiguration.yaml=/var/lib/jetty/configuration.yaml