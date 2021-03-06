#!/bin/sh

########################################################################
#
#   Example:
#     ./updateIsVnf.sh ../config/janusgraph.properties Bservice,Myservice
#
########################################################################

CURRENT_DIR=`pwd`
BASEDIR=$(dirname $0)

if [ `echo ${BASEDIR} | cut -c1-1` = "/" ]
then
                FULL_PATH=$BASEDIR
else
                FULL_PATH=$CURRENT_DIR/$BASEDIR
fi

. ${FULL_PATH}/baseOperation.sh

mainClass="org.openecomp.sdc.asdctool.main.UpdateIsVnfMenu"

command="java $JVM_LOG_FILE -cp $JARS $mainClass updateIsVnfTrue $@"
echo $command

$command
result=$?

echo "***********************************"
echo "***** $result *********************"
echo "***********************************"

exit $result


