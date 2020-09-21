#!/bin/sh

##############################
# Data Migration: Upgrade Post Migration 1710
##############################

CURRENT_DIR=`pwd`
BASEDIR=$(dirname $0)

if [ `echo ${BASEDIR} | cut -c1-1` = "/" ]
then
                FULL_PATH=$BASEDIR
else
                FULL_PATH=$CURRENT_DIR/$BASEDIR
fi

. ${FULL_PATH}/baseOperation.sh

mainClass="org.openecomp.sdc.asdctool.migration.main.MigrationMenu"

command="java $JVM_LOG_FILE -cp $JARS $mainClass -c $@"
echo $command

$command
result=$?

echo "***********************************"
echo "***** $result *********************"
echo "***********************************"

exit $result


