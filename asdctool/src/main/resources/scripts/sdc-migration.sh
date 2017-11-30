#!/bin/bash

##############################
# Data Migration
##############################

# in 1802E we do not want to execute automatic post process 
exit 0

CURRENT_DIR=`pwd`
BASEDIR=$(dirname $0)

if [ ${BASEDIR:0:1} = "/" ]
then
                FULL_PATH=$BASEDIR
else
                FULL_PATH=$CURRENT_DIR/$BASEDIR
fi

source ${FULL_PATH}/baseOperation.sh

mainClass="org.openecomp.sdc.asdctool.migration.main.MigrationMenu"

command="java $JVM_LOG_FILE -Xmx6000M -cp $JARS $mainClass $@"
echo $command

$command
result=$?



echo "***********************************"
echo "***** $result *********************"
echo "***********************************"

exit $result



