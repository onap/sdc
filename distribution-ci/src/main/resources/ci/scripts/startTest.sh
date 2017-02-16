#!/bin/bash

function usage {
	echo "Usage: $0 <jar file>"
}

function exitOnError() {
        if [ $1 -ne 0 ]
        then
                echo "Failed running task $2"
                exit 2
        fi
}

if [ $# -lt 1 ]
then
	usage
	exit 2
fi

CURRENT_DIR=`pwd`
BASEDIR=$(dirname $0)

if [ ${BASEDIR:0:1} = "/" ]
then
        FULL_PATH=$BASEDIR
else
        FULL_PATH=$CURRENT_DIR/$BASEDIR
fi

LOGS_PROP_FILE=file:${FULL_PATH}/conf/log4j.properties
#############################################
TARGET_DIR=${FULL_PATH}/target
CONF_FILE=${FULL_PATH}/conf/sdc.yaml
DEBUG=true
MainClass=org.openecomp.sdc.ci.tests.run.StartTest

JAR_FILE=$1

#TARGET_DIR=`echo ${TARGET_DIR} | sed 's/\//\//g'`
#echo $TARGET_DIR

TESTS_DIR=/opt/app/sdc/ci/resources/tests
COMPONENTS_DIR=/opt/app/sdc/ci/resources/components

#sed -i 's#\(outputFolder:\).*#\1 '${TARGET_DIR}'#g' $CONF_FILE
#sed -i 's#\(resourceConfigDir:\).*#\1 '${TESTS_DIR}'#g' $CONF_FILE
#sed -i 's#\(componentsConfigDir:\).*#\1 '${COMPONENTS_DIR}'#g' $CONF_FILE
TARGET_LOG_DIR="${TARGET_DIR}/"

mkdir -p ${TARGET_DIR}
if [ -d ${TARGET_DIR} ]
then
    rm -rf ${TARGET_DIR}/*
	exitOnError $? "Failed_to_delete_target_dir"
fi

debug_port=8800
#JAVA_OPTION="-javaagent:/var/tmp/jacoco/lib/jacocoagent.jar=destfile=jacoco-it.exec"
JAVA_OPTION=""
case "$2" in
	-debug) echo "Debug mode, Listen on port $debug_port"; JAVA_OPTION="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=${debug_port}" ;;
	"") echo "Standard mode";;
	*) echo "USAGE: startTest.sh [-debug]";;
esac

cmd="java $JAVA_OPTION -DdisplayException=true -Dtargetlog=${TARGET_LOG_DIR} -Dconfig.resource=${CONF_FILE} -Ddebug=${DEBUG} -Dlog4j.configuration=${LOGS_PROP_FILE} -cp $JAR_FILE ${MainClass}" 

#echo $cmd
#console=`$cmd`

if [ $DEBUG == "true" ]
then
	$cmd
else
	$cmd >> /dev/null
fi 
status=`echo $?`



echo "##################################################"
echo "################# status is ${status} #################" 
echo "##################################################"

exit $status 

