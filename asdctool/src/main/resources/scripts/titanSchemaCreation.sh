#!/bin/bash

##############################
# Titan Schema Creation
##############################

CURRENT_DIR=`pwd`
BASEDIR=$(dirname $0)

if [ ${BASEDIR:0:1} = "/" ]
then
                FULL_PATH=$BASEDIR
else
                FULL_PATH=$CURRENT_DIR/$BASEDIR
fi

source ${FULL_PATH}/baseOperation.sh

mainClass="org.openecomp.sdc.asdctool.main.DataSchemaMenu"

command="java $JVM_LOG_FILE -cp $JARS $mainClass create-titan-structures $@"
echo $command

$command
result=$?

echo "***********************************"
echo "***** $result *********************"
echo "***********************************"

exit $result


