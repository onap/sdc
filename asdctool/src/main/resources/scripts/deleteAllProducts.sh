#!/bin/bash

#####################################
# delete all products from JanusGraph
#####################################

CURRENT_DIR=`pwd`
BASEDIR=$(dirname $0)

if [ ${BASEDIR:0:1} = "/" ]
then
                FULL_PATH=$BASEDIR
else
                FULL_PATH=$CURRENT_DIR/$BASEDIR
fi

source ${FULL_PATH}/baseOperation.sh

mainClass="org.openecomp.sdc.asdctool.main.RemoveUtils"

command="java $JVM_LOG_FILE -cp $JARS $mainClass remove-products $@"
echo $command

$command
result=$?

echo "***********************************"
echo "***** $result *********************"
echo "***********************************"

exit $result


