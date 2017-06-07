#!/bin/bash

##############################
# Data Migration
##############################

CURRENT_DIR=`pwd`
BASEDIR=$(dirname $1)

if [ ${BASEDIR:0:1} = "/" ]
then
                FULL_PATH=$BASEDIR
else
                FULL_PATH=$CURRENT_DIR/$BASEDIR
fi

source ${FULL_PATH}/baseOperation.sh

mainClass="org.openecomp.sdc.asdctool.main.MigrationMenu"

case  $1 in
	1604) 
		command="java $JVM_LOG_FILE -cp $JARS $mainClass migrate-1602-1604 $@"
		echo $command
		;;
	1607)
		command="sh ./dataMigration1607.sh $@"
		echo $command
		;;
	1610)
		command="sh ./dataMigration1610.sh $@"
		echo $command
		;;
	1702)
		command="sh ./dataMigration1702.sh $@"
		echo $command
		;;
	1707)
    	command="sh ./dataMigration1707.sh $@"
        echo $command
        ;;
	*)
		echo "No migration for this version $1"
		;;
esac

$command
result=$?

echo "***********************************"
echo "***** $result *********************"
echo "***********************************"

exit $result


