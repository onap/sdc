#!/bin/bash

###########################################################################################################
# script name - zusammenMainTool.sh
# run script - ./zusammenMainTool.sh
#  1.  Export all  - ./zusammenMainTool.sh -c EXPORT
#  2.  Export one item  ./zusammenMainTool.sh -c EXPORT -i ${elementId}
#  3.  Import one item - ./zusammenMainTool.sh -c  IMPORT -f ${fileName} -i ${elementId}
#  4.  Import all - ./zusammenMainTool.sh -c  IMPORT -f  ${fileName}
#  5.  Reset old version - ./zusammenMainTool.sh -c  RESET_OLD_VERSION
#  6.  Heal all - ./zusammenMainTool.sh -c  HEAL_ALL -t ${thread number}
#
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

java -Dconfig.home=/opt/app/jetty/base/be/config -Dlog.home=/apps/jetty/base/be/logs -Dconfiguration.yaml=${CONF_FILE_LOCATION}  -classpath openecomp-zusammen-tools-1.2.0-SNAPSHOT.jar:lib/* org.openecomp.core.tools.main.ZusammenMainTool $1 $2 $3 $4 $5 $6
STATUS="${?}" echo "${STATUS}"
