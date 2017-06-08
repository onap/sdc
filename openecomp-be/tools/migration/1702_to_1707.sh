#!/bin/bash

###########################################################################################################
# script name - 1702_to_1707.sh
# run script - ./1702_to_1707.sh
# this script replaces tosca namespace to org.openecomp in translated files from 1702 version
###########################################################################################################


# change exist package and service templates in db
java -Dlog.home=/apps/jetty/base/be/logs -Dconfiguration.yaml=/apps/jetty/base/be/config/catalog-be/configuration.yaml -jar openecomp-migration-lib-1707.0.0-SNAPSHOT.jar org.openecomp.sdc.migration.ToscaNamespaceMigration

STATUS="${?}"
echo "${STATUS}"
echo "All log messages for the migration proccess are in /apps/jetty/base/be/logs/ASDC/ASDC-BE/migration_debug.log"
