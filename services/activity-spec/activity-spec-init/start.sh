#!/bin/sh

cqlsh -u $SDC_USER -p $SDC_PASSWORD -f /create_activityspec_db.cql $CS_HOST $CS_PORT
