#!/bin/bash
REMOTE_DEBUG=false
RERUN=false
JAVA_OPTION=""
debug_port=8000
TEST_SUITES=testSuites
fileName=testng-failed.xml

function help_usage ()
{
	echo
	echo "$0 (<jar_file_name> <suite file name>) [-r/rerun <true/false> -d/debug <true/false>]"
	echo "nohup ./startTest.sh ui-ci-1707.0.5-SNAPSHOT-jar-with-dependencies.jar extendedSanity.xml -r false -d true &"
	echo "by default rerun is true and remote debug is false."
	echo
	exit 2
}

function isBoolean ()
{
	PARAM_NAME=$1
	VALUE=$2
	if [[ ${VALUE} != "true" ]] && [[ ${VALUE} != "false" ]]; then
		echo "Valid parameter" ${PARAM_NAME} "values are: true/false"
	        help_usage
	fi
}

function prepareFailedXmlFile ()
{
	echo "1="$1 "2="$2 "fileName="${fileName}
	PATTERN=`grep -w "test name=" ${FULL_PATH}/${TEST_SUITES}/$2 | awk -F'"' '{print $2}'`
	sed '/<test name="'${PATTERN}'"/,/<!-- '${PATTERN}' --/d' $1 > ${FULL_PATH}/${TEST_SUITES}/${fileName}
    sed -i 's/thread-count="[0-9]\+"/thread-count="1"/g' ${FULL_PATH}/${TEST_SUITES}/${fileName}
}

#main
[ $# -lt 2 ] && help_usage

JAR_FILE=$1
SUITE_FILE=$2

while [ $# -ne 0 ]; do
	case $1 in
		-r|rerun)
			RERUN=$2
            		isBoolean $1 ${RERUN}
			shift 1
			shift 1
		;;
		-d|debug)
			REMOTE_DEBUG=$2
           		isBoolean $1 ${REMOTE_DEBUG}
			shift 1
			shift 1
		;;
		*)
			shift 1
		;;
	esac
done

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

TESTS_DIR=/opt/app/sdc/ci/resources/tests
COMPONENTS_DIR=/opt/app/sdc/ci/resources/components


TARGET_LOG_DIR="${TARGET_DIR}/"

######ADD USERS################

BE_IP=`cat conf/attsdc.yaml | grep catalogBeHost| awk '{print $2}'`


if [ ${REMOTE_DEBUG} == "true" ]; then
    echo "Debug mode, Listen on port $debug_port";
    JAVA_OPTION="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=${debug_port}" ;
fi

cmd="java $JAVA_OPTION -Xms1024m -Xmx1024m -DdisplayException=true -Dtargetlog=${TARGET_LOG_DIR} -Dfilepath=${FILES_TEST} -Dconfig.resource=${CONF_FILE} -Ddebug=${DEBUG} -Dlog4j.configuration=${LOGS_PROP_FILE} -cp $JAR_FILE ${MainClass} $SUITE_FILE &"


if [ $DEBUG == "true" ]
then
	$cmd
else
	$cmd >> /dev/null
fi

if [ ${RERUN} == "true" ]; then
    if [ -f ${TARGET_DIR}/${fileName} ]; then
        echo "Prepare" ${TARGET_DIR}/${fileName} "file to rerun all failed tests ...";
        prepareFailedXmlFile ${TARGET_DIR}/${fileName} $SUITE_FILE;
        SUITE_FILE=${fileName};
	cmd="java $JAVA_OPTION -Xms1024m -Xmx1024m -DdisplayException=true -Dtargetlog=${TARGET_LOG_DIR} -Dfilepath=${FILES_TEST} -Dconfig.resource=${CONF_FILE} -Ddebug=${DEBUG} -Dlog4j.configuration=${LOGS_PROP_FILE} -cp $JAR_FILE ${MainClass} $SUITE_FILE &"
        $cmd;
    fi
fi

status=`echo $?`

source ExtentReport/versions.info
now=$(date +'%Y-%m-%d_%H_%M')
REPORT_NAME=${now}
VERSION=${osVersion}



echo "##################################################"
echo "################# status is ${status} #################"
echo "##################################################"

exit $status