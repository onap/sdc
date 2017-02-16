#!/bin/bash

##############################
# Data Migration 1610
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

mainClass="org.openecomp.sdc.asdctool.main.MigrationMenu"

command="java $JVM_LOG_FILE -cp $JARS $mainClass fix-properties $@"
echo $command

$command
result=$?

if [ $result -eq 0 ]
then
  command="java $JVM_LOG_FILE -cp $JARS $mainClass align-tosca-artifacts $@"
  echo $command
  $command
  result=$?
fi

echo "***********************************"
echo "***** $result *********************"
echo "***********************************"

exit $result


