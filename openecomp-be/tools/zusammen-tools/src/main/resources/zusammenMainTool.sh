#!/bin/bash

###########################################################################################################
# script name - cassandra2zusammen.sh
# run script - ./cassandra2zusammen.sh
# This script migrates ASDC 1st class citizen entities and their sub-entities from Cassandra to
# Zusammen.
# This script should be run when upgrading from 1702 to 1707
###########################################################################################################


# change exist package and service templates in db
java -Dlog.home=/apps/jetty/base/be/logs -Dconfiguration.yaml=/apps/jetty/base/be/config/catalog-be/configuration.yaml  -jar openecomp-zusammen-tools-1.0-SNAPSHOT.jar org.openecomp.core.tools.main.ZusammenMainTool $1 $2 $3 $4

STATUS="${?}" echo "${STATUS}" echo "All log messages for the zusammenMainTool
migration process are in /apps/jetty/base/be/logs/ASDC/ASDC-BE/zusammen_tool_debug.log"
