#!/bin/bash

OSTYPE=`uname -a | grep -iq ubuntu; echo $?`
echo "${OSTYPE}"

if [ ${OSTYPE} -eq 1 ]
then
   CONF_FILE_LOCATION="/opt/app/jetty/base/be/config/catalog-be/configuration.yaml"
else
  CONF_FILE_LOCATION="/apps/jetty/base/be/config/catalog-be/configuration.yaml"
fi
echo "Configuration file location:  ${CONF_FILE_LOCATION}"

# change exist package and service templates in db
java -Dlog.home=/apps/jetty/base/be/logs -Dconfiguration.yaml=${CONF_FILE_LOCATION}  -jar openecomp-zusammen-tools-1.0-SNAPSHOT.jar org.openecomp.core.tools.main.ZusammenMainTool $1 $2 $3 $4 $5 $6
STATUS="${?}" echo "${STATUS}"