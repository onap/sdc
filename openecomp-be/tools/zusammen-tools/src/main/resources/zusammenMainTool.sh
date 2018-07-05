#!/bin/bash

###########################################################################################################
# script name - zusammenMainTool.sh
# run script - ./zusammenMainTool.sh
#  1.  Export all        - ./zusammenMainTool.sh -c EXPORT
#  2.  Export one item   - ./zusammenMainTool.sh -c EXPORT -i ${elementId}
#  3.  Import one item   - ./zusammenMainTool.sh -c IMPORT -f ${fileName} -i ${elementId}
#  4.  Import all        - ./zusammenMainTool.sh -c IMPORT -f ${fileName}
#  5.  Reset old version - ./zusammenMainTool.sh -c RESET_OLD_VERSION -v ${old_version}
#  6.  Set healing flag  - ./zusammenMainTool.sh -c SET_HEAL_BY_ITEM_VERSION -i {item id} -v {version_id} -v ${old_version}
#  7.  Heal all          - ./zusammenMainTool.sh -c HEAL_ALL -t ${thread number}
#  8.  Clean user data:  - ./zusammenMainTool.sh -c CLEAN_USER_DATA -i {item id} -u {user}
#  9.  Delete public version: - ./zusammenMainTool.sh -c DELETE_PUBLIC_VERSION -i {item id} -v {version_id}
# 10.  Add user as contributor: - ./zusammenMainTool.sh -c ADD_CONTRIBUTOR [-p {item id list file path}] -u {user list file path}
#  7. Populate User Permissions - ./zusammenMainTool.sh -c POPULATE_USER_PERMISSIONS
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
mv lib/openecomp-zusammen-tools*.jar openecomp-zusammen-tools.jar  &>/dev/null

java -Dconfig.home=/opt/app/jetty/base/be/config -Dlog.home=/apps/jetty/base/be/logs -Dconfiguration.yaml=${CONF_FILE_LOCATION}  -classpath openecomp-zusammen-tools.jar:lib/* org.openecomp.core.tools.main.ZusammenMainTool ${*}
STATUS="${?}" echo "${STATUS}"
