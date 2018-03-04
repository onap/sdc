#!/bin/bash

##############################
# Get list of SDC consumers
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

mainClass="org.openecomp.sdc.asdctool.main.GetConsumersMenu"

command="java $JVM_LOG_FILE -Xmx1024M -cp $JARS $mainClass $@"
echo $command

$command
result=$?



echo "***********************************"
echo "***** $result *********************"
echo "***********************************"

exit $result



