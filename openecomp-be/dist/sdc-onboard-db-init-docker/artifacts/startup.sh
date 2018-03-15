#!/bin/bash

cd /root

CS_PORT=""
CS_HOST=127.0.0.1

if [ ! -z "${HOST_IP}" ]; then
    CS_HOST=$HOST_IP
fi

if [ ! -z "${HOST_PORT}" ]; then
    CS_PORT=$HOST_PORT
fi

echo "[Info] Going to initialize sdc onboard cassandra: user=$SDC_USER; host=$CS_HOST; port=$CS_PORT"
echo "[Info] Initializing onboard keyspaces"
cqlsh -u $SDC_USER -p $SDC_PASS -f init_keyspaces.cql $CS_HOST $CS_PORT

echo "[Info] Initializing onboard schemas"
cqlsh -u $SDC_USER -p $SDC_PASS -f init_schemas.cql $CS_HOST $CS_PORT

rc=$?
if [[ $rc != 0 ]]; then exit $rc; fi