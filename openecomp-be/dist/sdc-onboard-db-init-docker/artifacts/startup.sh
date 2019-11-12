#!/bin/bash

cd /root

CS_PORT=""
CS_HOST=127.0.0.1

if [ ! -z "${CS_HOST_IP}" ]; then
    CS_HOST=$CS_HOST_IP
fi

if [ ! -z "${CS_HOST_PORT}" ]; then
    CS_PORT=$CS_HOST_PORT
fi

echo "[Info] Going to initialize sdc onboard cassandra: user=$SDC_USER; host=$CS_HOST; port=$CS_PORT"

echo "[Info] Initializing onboard keyspaces"
date;
cqlsh --cqlversion '3.4.4'  -u $SDC_USER -p $SDC_PASSWORD -f init_keyspaces.cql $CS_HOST $CS_PORT
rc=$?
date;

if [[ $rc != 0 ]]; then
	echo "[Error] Failed to initialize onboard keyspaces";
	exit $rc;
fi

echo "[Info] Initializing onboard schemas"
date;
cqlsh --cqlversion '3.4.4'  -u $SDC_USER -p $SDC_PASSWORD -f init_schemas.cql $CS_HOST $CS_PORT
rc=$?
date;

if [[ $rc != 0 ]]; then
	echo "[Error] Failed to initialize onboard schemas";
	exit $rc;
fi
