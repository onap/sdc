#!/bin/bash

###########################################################################################################
# script name - cassandra2zusammen.sh
# run script - ./cassandra2zusammen.sh
# This script migrates ASDC 1st class citizen entities and their sub-entities from Cassandra to Zusammen.
# This script should be run when upgrading from 1702 to 1707
###########################################################################################################


# change exist package and service templates in db
java -Dlog.home=/apps/jetty/base/be/logs -Dconfiguration.yaml=/apps/jetty/base/be/config/catalog-be/configuration.yaml  -jar openecomp-zusammen-migration-1.0-SNAPSHOT.jar org.openecomp.core.migration.MigrationMain

STATUS="${?}"
echo "${STATUS}"
