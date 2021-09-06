#!/bin/sh
SDC_HOME="/home/sdc"
cd $SDC_HOME || { echo "$(date) Failed to access directory $SDC_HOME"; exit 1; }

CS_PORT=""
CS_HOST=127.0.0.1

if [ -n "${CS_HOST_IP}" ]; then
    CS_HOST=$CS_HOST_IP
fi

if [ -n "${CS_HOST_PORT}" ]; then
    CS_PORT=$CS_HOST_PORT
fi

echo "$(date) [Info] Going to initialize sdc onboard cassandra: user=$SDC_USER; host=$CS_HOST; port=$CS_PORT"

echo "$(date) [Info] Initializing onboard keyspaces"
cqlsh -u $SDC_USER -p $SDC_PASSWORD -f init_keyspaces.cql $CS_HOST $CS_PORT
rc=$?

if [ $rc != 0 ]; then
	echo "$(date) [Error] Failed to initialize onboard keyspaces";
	exit $rc;
fi
echo "$(date) [Info] Finished initializing onboard keyspaces"

echo "$(date) [Info] Initializing onboard schemas"
cqlsh -u $SDC_USER -p $SDC_PASSWORD -f init_schemas.cql $CS_HOST $CS_PORT
rc=$?

if [ $rc != 0 ]; then
	echo "$(date) [Error] Failed to initialize onboard schemas";
	exit $rc;
fi
echo "$(date) [Info] Finished initializing onboard schemas"

echo "$(date) [Info] Upgrading onboard schemas"
for entry in "$SDC_HOME/upgrade-scripts"/*
do
  echo "$(date) Running upgrade file '$entry'"
  cqlsh -u $SDC_USER -p $SDC_PASSWORD -f $entry $CS_HOST $CS_PORT
  rc=$?
  if [ $rc != 0 ]; then
    echo "$(date) [Warn] Upgrade failed for file '$entry'. It is possible that the upgrade was previously applied.";
  fi
  echo "$(date) Successfully ran upgrade file '$entry'"
done

echo "$(date) [Info] Onboarding init was successful"
