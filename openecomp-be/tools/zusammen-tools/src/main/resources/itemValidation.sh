#!/bin/bash

##########################################################################################################
# script name - itemValidation.sh
# run script - ./itemValidation.sh -i ${itemId}
##########################################################################################################

OSTYPE=`uname -a | grep -iq ubuntu; echo $?`
echo "${OSTYPE}"

if [ ${OSTYPE} -eq 0 ]
then
   CONF_FILE_LOCATION="/opt/app/jetty/base/be/config/catalog-be/configuration.yaml"
else
   CONF_FILE_LOCATION="/apps/jetty/base/be/config/catalog-be/configuration.yaml"
fi
echo "Configuration file location:  ${CONF_FILE_LOCATION}"
mv lib/openecomp-zusammen-tools*.jar openecomp-zusammen-tools.jar  &>/dev/null

java -Dconfig.home=/opt/app/jetty/base/be/config -Dlog.home=/apps/jetty/base/be/logs -Dconfiguration.yaml=${CONF_FILE_LOCATION}  -classpath openecomp-zusammen-tools.jar:lib/* org.openecomp.core.tools.itemvalidation.ItemValidation $1 $2
STATUS="${?}" echo "${STATUS}"
