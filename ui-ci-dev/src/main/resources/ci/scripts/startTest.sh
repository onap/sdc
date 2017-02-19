#!/bin/bash

TOMCAT_DIR=/home/apache-tomcat-7.0.41/webapps/sdc-ci

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

LOGS_PROP_FILE=file:${FULL_PATH}/../conf/log4j.properties
#############################################
TARGET_DIR=${FULL_PATH}/../target
TARGET_LOG_DIR="${TARGET_DIR}/"
CONF_FILE=${FULL_PATH}/../conf/sdc.yaml
DEBUG=true
MainClass=org.openecomp.sdc.ci.tests.run.StartTest

JAR_FILE=$1

#TARGET_DIR=`echo ${TARGET_DIR} | sed 's/\//\//g'`
#echo $TARGET_DIR

TESTS_DIR=/opt/app/sdc/ci/resources/tests
COMPONENTS_DIR=/opt/app/sdc/ci/resources/components


sed -i 's#\(outputFolder:\).*#\1 '${TARGET_DIR}'#g' $CONF_FILE
sed -i 's#\(resourceConfigDir:\).*#\1 '${TESTS_DIR}'#g' $CONF_FILE
sed -i 's#\(componentsConfigDir:\).*#\1 '${COMPONENTS_DIR}'#g' $CONF_FILE



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

#cmd="java $JAVA_OPTION -Dconfig.resource=attodlit.conf -Dlog4j.configuration=file:./conf/log4j.properties -cp #att-odl-it_0.0.1-SNAPSHOT-jar-with-dependencies.jar org.openecomp.d2.it.StartTest"

#cmd="java $JAVA_OPTION -Dconfig.resource=attsdc.conf -Ddebug=true -Dlog4j.configuration=file:./conf/log4j.properties -cp uber-ci-1.0.0-SNAPSHOT.jar org.openecomp.sdc.ci.tests.run.StartTest"


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

#echo "console=$console"
#echo "status=$status"
#tomcat=`ps -ef | grep tomcat | grep java | wc -l`

#if [ $tomcat == 0 ]; then
#	echo "Bring tomcat up"
#	apache-tomcat-7.0.41/bin/startup.sh
#fi

#`rm -rf ./html/*.html`
#`mv *.html ./html/`


if [ -d ${TOMCAT_DIR} ]
then

	cp ${TARGET_DIR}/*.html ${TOMCAT_DIR}
	mv ${TOMCAT_DIR}/SDC-testReport.html ${TOMCAT_DIR}/index.html
fi

#echo "tomcat=$tomcat"
#ip=`ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | grep 172.20`

#echo "Report url: http://$ip:8090/att-odl-it/"

echo "##################################################"
echo "################# status is $status " 
echo "##################################################"

exit $status 

