#!/bin/bash

#
# Constants:
#

WORKSPACE="${WORKSPACE:-}"
SUCCESS=0
FAILURE=1

CS_PASSWORD="onap123#@!"
SDC_USER="asdc_user"
SDC_PASSWORD="Aa1234%^!"

JETTY_BASE="/var/lib/jetty"

RELEASE=latest
LOCAL=false
RUNTESTS=false
BE_DEBUG_PORT="--publish 4000:4000"
FE_DEBUG_PORT="--publish 6000:6000"
ONBOARD_DEBUG_PORT="--publish 4001:4001"


# Java Options:
BE_JAVA_OPTIONS="-Xdebug -agentlib:jdwp=transport=dt_socket,address=4000,server=y,suspend=n -Xmx1536m -Xms1536m"
FE_JAVA_OPTIONS="-Xdebug -agentlib:jdwp=transport=dt_socket,address=6000,server=y,suspend=n -Xmx256m -Xms256m"
ONBOARD_BE_JAVA_OPTIONS="-Xdebug -agentlib:jdwp=transport=dt_socket,address=4001,server=y,suspend=n -Xmx1g -Xms1g"
DCAE_BE_JAVA_OPTIONS="-XX:MaxPermSize=256m -Xmx1024m -Dconfig.home=config -Dlog.home=/var/lib/jetty/logs/ -Dlogging.config=config/dcae-be/logback-spring.xml"
DCAE_FE_JAVA_OPTIONS="-XX:MaxPermSize=256m -Xmx1024m -Dconfig.home=config -Dlog.home=/var/lib/jetty/logs/ -Dlogging.config=config/dcae-fe/logback-spring.xml"
SIM_JAVA_OPTIONS=" -Xmx128m -Xms128m -Xss1m -Dlog4j.configuration=file:///${JETTY_BASE}/config/sdc-simulator/log4j.properties"
API_TESTS_JAVA_OPTIONS="-Xmx512m -Xms512m"
UI_TESTS_JAVA_OPTIONS="-Xmx1024m -Xms1024m"
#Define this as variable, so it can be excluded in run commands on Docker for OSX, as /etc/localtime cant be mounted there.
LOCAL_TIME_MOUNT_CMD="--volume /etc/localtime:/etc/localtime:ro"
# If os is OSX, unset this, so /etc/localtime is not mounted, otherwise leave it be
if [[ "$OSTYPE" == "darwin"* ]]; then
  LOCAL_TIME_MOUNT_CMD=""
fi


#
# Functions:
#


function usage {
    echo "usage: docker_run.sh [ -r|--release <RELEASE-NAME> ] [ -e|--environment <ENV-NAME> ] [ -p|--port <Docker-hub-port>] [ -l|--local <Run-without-pull>] [ -sim|--simulator <Run-with-simulator>] [ -ta <run api tests with the supplied test suit>] [ -tu <run ui tests with the supplied test suit>] [ -ta <run api tests with the supplied test suit>] [ -tu <run ui tests with the supplied test suit>] [ -tad <run api tests with the default test suit>] [ -tu <run ui tests with the default test suit>] [ -dcae|--dcae <Run-with-DCAE>][ -h|--help ]"
    echo "start dockers built locally example: docker_run.sh -l"
    echo "start dockers built locally and simulator example: docker_run.sh -l -sim"
    echo "start dockers, pull from onap nexus according to release and simulator example: docker_run.sh -r 1.3-STAGING-latest -sim"
    echo "start dockers built locally and run api tests docker example: docker_run.sh -l -tad"
    echo "start dockers built locally and run only the catalog be example: docker_run.sh -l -d sdc-BE "
}
#


function cleanup {
    echo "Performing old dockers cleanup"

	if [ "$1" == "all" ] ; then
		docker_ids=`docker ps -a | egrep -v "onap/sdc-simulator" | egrep "ecomp-nexus:${PORT}/sdc|sdc|Exit}|dcae" | awk '{print $1}'`
		for X in ${docker_ids}
		do
			docker rm -f ${X}
		done
	else
	    echo "performing $1 docker cleanup"
	    tmp=`docker ps -a -q --filter="name=$1"`
	    if [[ ! -z "$tmp" ]]; then
    		docker rm -f ${tmp}
        fi
	fi
}
#


function dir_perms {
    mkdir -p ${WORKSPACE}/data/logs/BE/SDC/SDC-BE
    mkdir -p ${WORKSPACE}/data/logs/FE/SDC/SDC-FE

    mkdir -p ${WORKSPACE}/data/logs/DCAE-BE/DCAE
    mkdir -p ${WORKSPACE}/data/logs/DCAE-FE/DCAE

    mkdir -p ${WORKSPACE}/data/logs/sdc-api-tests/ExtentReport
    mkdir -p ${WORKSPACE}/data/logs/ONBOARD/SDC/ONBOARD-BE
	mkdir -p ${WORKSPACE}/data/logs/sdc-api-tests/target
	mkdir -p ${WORKSPACE}/data/logs/sdc-ui-tests/ExtentReport
	mkdir -p ${WORKSPACE}/data/logs/sdc-ui-tests/target
	mkdir -p ${WORKSPACE}/data/logs/docker_logs
	mkdir -p ${WORKSPACE}/data/logs/WS
    chmod -R 777 ${WORKSPACE}/data/logs
}
#


function docker_logs {
    docker logs $1 > ${WORKSPACE}/data/logs/docker_logs/$1_docker.log
}
#


#
# Readiness Prob
#

function ready_probe {
    docker exec $1 /var/lib/ready-probe.sh > /dev/null 2>&1
    rc=$?
    if [[ ${rc} == 0 ]]; then
        echo DOCKER $1 start finished in $2 seconds
        return ${SUCCESS}
    fi
    return ${FAILURE}
}
#


function probe_docker {
    MATCH=`docker logs --tail 30 $1 | grep "DOCKER STARTED"`
    echo MATCH is -- ${MATCH}

    if [ -n "$MATCH" ] ; then
        echo DOCKER start finished in $2 seconds
        return ${SUCCESS}
    fi
    return ${FAILURE}
}
#


function probe_es {
    health_Check_http_code=$(curl --noproxy "*" -o /dev/null -w '%{http_code}' http://${IP}:9200/_cluster/health?wait_for_status=yellow&timeout=120s)
    if [[ "$health_Check_http_code" -eq 200 ]] ; then
        echo DOCKER start finished in $1 seconds
        return ${SUCCESS}
    fi
    return ${FAILURE}
}
#


function probe_sim {
    if lsof -Pi :8285 -sTCP:LISTEN -t >/dev/null ; then
        echo "Already running"
        return ${SUCCESS}
    else
        echo "Not running"
        return ${FAILURE}
    fi
}
#


function probe_dcae_be {
    health_check_http_code=$(curl -i -o /dev/null -w '%{http_code}' http://${IP}:8082/dcae/conf/composition)
    if [[ "${health_check_http_code}" -eq 200 ]] ; then
        echo DOCKER start finished in $1 seconds
        return ${SUCCESS}
    fi
    return ${FAILURE}
}
#

function probe_dcae_fe {
    health_check_http_code=$(curl -i -o /dev/null -w '%{http_code}' http://${IP}:8183/dcaed/healthCheck)
    if [[ "${health_check_http_code}" -eq 200 ]] ; then
        echo DOCKER start finished in $1 seconds
        return ${SUCCESS}
    fi
    return ${FAILURE}
}
#


# Not applicable for current release. Return Success in any case
function probe_dcae_tools {
   health_check_http_code=$(curl -i -o /dev/null -w '%{http_code}'  http://${IP}:8082/dcae/getResourcesByMonitoringTemplateCategory)
    if [[ "${health_check_http_code}" -eq 200 ]] ; then
        echo DOCKER start finished in $1 seconds
        return ${SUCCESS}
    fi
    return ${SUCCESS}
}
#


function monitor_docker {
    DOCKER_NAME=$1
    echo "Monitor ${DOCKER_NAME} Docker"
    sleep 5
    TIME_OUT=900
    INTERVAL=20
    TIME=0

    while [ "$TIME" -lt "$TIME_OUT" ]; do

        case ${DOCKER_NAME} in

            sdc-cs)
                ready_probe ${DOCKER_NAME} ${TIME} ;
                status=$? ;
            ;;
            sdc-es)
                probe_es ${TIME} ;
                status=$? ;
            ;;
            sdc-BE)
       		    ready_probe ${DOCKER_NAME} ${TIME} ;
                status=$? ;
            ;;
            sdc-FE)
                ready_probe ${DOCKER_NAME} ${TIME} ;
                status=$? ;
            ;;
            sdc-onboard-BE)
                ready_probe ${DOCKER_NAME} ${TIME} ;
                status=$? ;
            ;;
            dcae-be)
                probe_dcae_be ${TIME} ;
                status=$? ;
            ;;
            dcae-fe)
                probe_dcae_fe ${TIME} ;
                status=$? ;
            ;;
            dcae-tools)
                probe_dcae_tools ;
                status=$? ;
            ;;
            *)
                probe_docker ${DOCKER_NAME} ${TIME};
                status=$? ;
            ;;

        esac

        if [ ${status} == ${SUCCESS} ] ; then
            break;
        fi

        echo "Sleep: ${INTERVAL} seconds before testing if ${DOCKER_NAME} DOCKER is up. Total wait time up now is: ${TIME} seconds. Timeout is: ${TIME_OUT} seconds"
        sleep ${INTERVAL}
        TIME=$(($TIME+$INTERVAL))
    done

    docker_logs ${DOCKER_NAME}

    if [ "$TIME" -ge "$TIME_OUT" ]; then
        echo -e "\e[1;31mTIME OUT: DOCKER was NOT fully started in $TIME_OUT seconds... Could cause problems ...\e[0m"
    fi
}
#


function healthCheck {
	curl --noproxy "*" ${IP}:9200/_cluster/health?pretty=true

	echo "BE health-Check:"
	curl --noproxy "*" http://${IP}:8080/sdc2/rest/healthCheck

	echo ""
	echo ""
	echo "FE health-Check:"
	curl --noproxy "*" http://${IP}:8181/sdc1/rest/healthCheck


	echo ""
	echo ""
	healthCheck_http_code=$(curl --noproxy "*" -o /dev/null -w '%{http_code}' -H "Accept: application/json" -H "Content-Type: application/json" -H "USER_ID: jh0003" http://${IP}:8080/sdc2/rest/v1/user/demo;)
	if [[ ${healthCheck_http_code} != 200 ]]; then
		echo "Error [${healthCheck_http_code}] while user existance check"
		return ${healthCheck_http_code}
	fi
	echo "check user existance: OK"
	return ${healthCheck_http_code}
}
#


function command_exit_status {
    status=$1
    docker=$2
    if [ "${status}" != "0" ] ; then
        echo "[  ERROR  ] Docker ${docker} run command exit with status [${status}]"
    fi
}
#


#
# Run Containers
#

#Elastic-Search
function sdc-es {
    DOCKER_NAME="sdc-es"
    echo "docker run sdc-elasticsearch..."
    if [ ${LOCAL} = false ]; then
        echo "pulling code"
        docker pull ${PREFIX}/sdc-elasticsearch:${RELEASE}
    fi
    docker run -dit --name ${DOCKER_NAME} --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --env ES_JAVA_OPTS="-Xms512m -Xmx512m" --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD} --env ES_HEAP_SIZE=1024M --volume ${WORKSPACE}/data/ES:/usr/share/elasticsearch/data --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 9200:9200 --publish 9300:9300 ${PREFIX}/sdc-elasticsearch:${RELEASE} /bin/sh
    command_exit_status $? ${DOCKER_NAME}
    echo "please wait while ES is starting..."
    monitor_docker ${DOCKER_NAME}
}
#


#Init-Elastic-Search
function sdc-init-es {
    DOCKER_NAME="sdc-init-es"
    echo "docker run sdc-init-elasticsearch..."
    if [ ${LOCAL} = false ]; then
        echo "pulling code"
        docker pull ${PREFIX}/sdc-init-elasticsearch:${RELEASE}
    fi
    echo "Running sdc-init-es"
    docker run --name ${DOCKER_NAME} --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD} --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments ${PREFIX}/sdc-init-elasticsearch:${RELEASE} > /dev/null 2>&1
    rc=$?
    docker_logs ${DOCKER_NAME}
    if [[ ${rc} != 0 ]]; then exit ${rc}; fi
}
#


#Cassandra
function sdc-cs {
    DOCKER_NAME="sdc-cs"
    echo "docker run sdc-cassandra..."
    if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-cassandra:${RELEASE}
    fi
    docker run -dit --name ${DOCKER_NAME} --env RELEASE="${RELEASE}" --env CS_PASSWORD="${CS_PASSWORD}" --env ENVNAME="${DEP_ENV}" --env HOST_IP=${IP} --env MAX_HEAP_SIZE="1536M" --env HEAP_NEWSIZE="512M" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD} --volume ${WORKSPACE}/data/CS:/var/lib/cassandra --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 9042:9042 --publish 9160:9160 ${PREFIX}/sdc-cassandra:${RELEASE} /bin/sh
    command_exit_status $? ${DOCKER_NAME}
    echo "please wait while CS is starting..."
    monitor_docker ${DOCKER_NAME}
}
#


#Cassandra-init
function sdc-cs-init {
    DOCKER_NAME="sdc-cs-init"
    echo "docker run sdc-cassandra-init..."
    if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-cassandra-init:${RELEASE}
    fi
    docker run --name ${DOCKER_NAME} --env RELEASE="${RELEASE}" --env SDC_USER="${SDC_USER}" --env SDC_PASSWORD="${SDC_PASSWORD}" --env CS_PASSWORD="${CS_PASSWORD}" --env ENVNAME="${DEP_ENV}" --env HOST_IP=${IP} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD} --volume ${WORKSPACE}/data/CS:/var/lib/cassandra --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --volume ${WORKSPACE}/data/CS-Init:/root/chef-solo/cache ${PREFIX}/sdc-cassandra-init:${RELEASE} > /dev/null 2>&1
    rc=$?
    docker_logs ${DOCKER_NAME}
    if [[ ${rc} != 0 ]]; then exit ${rc}; fi
}
#


#Onboard Cassandra-init
function sdc-cs-onboard-init {
    DOCKER_NAME="sdc-cs-onboard-init"
    echo "docker run sdc-cs-onboard-init..."
    if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-onboard-cassandra-init:${RELEASE}
    fi
    docker run --name ${DOCKER_NAME} --env RELEASE="${RELEASE}" --env CS_HOST_IP=${IP}  --env SDC_USER="${SDC_USER}" --env SDC_PASSWORD="${SDC_PASSWORD}" --env CS_PASSWORD="${CS_PASSWORD}" --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD} --volume ${WORKSPACE}/data/CS:/var/lib/cassandra --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --volume ${WORKSPACE}/data/CS-Init:/root/chef-solo/cache ${PREFIX}/sdc-onboard-cassandra-init:${RELEASE}
    rc=$?
    docker_logs ${DOCKER_NAME}
    if [[ ${rc} != 0 ]]; then exit ${rc}; fi
}
#


#Kibana
function sdc-kbn {
    DOCKER_NAME="sdc-kbn"
    echo "docker run sdc-kibana..."
    if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-kibana:${RELEASE}
        docker run --detach --name ${DOCKER_NAME} --env ENVNAME="${DEP_ENV}" --env NODE_OPTIONS="--max-old-space-size=200" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD} --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 5601:5601 ${PREFIX}/sdc-kibana:${RELEASE}
        command_exit_status $? ${DOCKER_NAME}
    fi
}
#


#Back-End
function sdc-BE {
    DOCKER_NAME="sdc-BE"
    echo "docker run sdc-backend..."
    if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-backend:${RELEASE}
    else
        ADDITIONAL_ARGUMENTS=${BE_DEBUG_PORT}
    fi
    docker run --detach --name ${DOCKER_NAME} --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env cassandra_ssl_enabled="false" --env JAVA_OPTIONS="${BE_JAVA_OPTIONS}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD} --volume ${WORKSPACE}/data/logs/BE/:/var/lib/jetty/logs  --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 8443:8443 --publish 8080:8080 ${ADDITIONAL_ARGUMENTS} ${PREFIX}/sdc-backend:${RELEASE}
    command_exit_status $? ${DOCKER_NAME}
    echo "please wait while BE is starting..."
    monitor_docker ${DOCKER_NAME}
}
#


# Back-End-Init
function sdc-BE-init {
    DOCKER_NAME="sdc-BE-init"
    echo "docker run sdc-backend-init..."
    if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-backend-init:${RELEASE}
    fi
    docker run --name ${DOCKER_NAME} --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD} --volume ${WORKSPACE}/data/logs/BE/:/var/lib/jetty/logs  --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments ${PREFIX}/sdc-backend-init:${RELEASE} > /dev/null 2>&1
    rc=$?
    docker_logs ${DOCKER_NAME}
    if [[ ${rc} != 0 ]]; then exit ${rc}; fi
}
#


# Onboard Back-End
function sdc-onboard-BE {
    DOCKER_NAME="sdc-onboard-BE"
    echo "docker run  sdc-onboard-BE ..."
#    TODO Check the dir_perms action . do we need it here ??
#    dir_perms
    if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-onboard-backend:${RELEASE}
    else
        ADDITIONAL_ARGUMENTS=${ONBOARD_DEBUG_PORT}
    fi
    docker run --detach --name ${DOCKER_NAME} --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env cassandra_ssl_enabled="false" --env SDC_CLUSTER_NAME="SDC-CS-${DEP_ENV}" --env SDC_USER="${SDC_USER}" --env SDC_PASSWORD="${SDC_PASSWORD}" --env JAVA_OPTIONS="${ONBOARD_BE_JAVA_OPTIONS}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD} --volume ${WORKSPACE}/data/logs/ONBOARD:/var/lib/jetty/logs --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 8445:8445 --publish 8081:8081 ${ADDITIONAL_ARGUMENTS} ${PREFIX}/sdc-onboard-backend:${RELEASE}
    command_exit_status $? ${DOCKER_NAME}
    echo "please wait while sdc-onboard-BE is starting..."
    monitor_docker ${DOCKER_NAME}
}
#


# Front-End
function sdc-FE {
    DOCKER_NAME="sdc-FE"
    echo "docker run sdc-frontend..."
    if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-frontend:${RELEASE}
    else
        ADDITIONAL_ARGUMENTS=${FE_DEBUG_PORT}
    fi
    docker run --detach --name ${DOCKER_NAME} --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env JAVA_OPTIONS="${FE_JAVA_OPTIONS}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD}  --volume ${WORKSPACE}/data/logs/FE/:/var/lib/jetty/logs --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --volume ${WORKSPACE}/data/environments/plugins-configuration.yaml:${JETTY_BASE}/config/catalog-fe/plugins-configuration.yaml --publish 9443:9443 --publish 8181:8181 ${ADDITIONAL_ARGUMENTS} ${PREFIX}/sdc-frontend:${RELEASE}
    command_exit_status $? ${DOCKER_NAME}
    echo "please wait while FE is starting....."
    monitor_docker ${DOCKER_NAME}
}
#


# DCAE BackEnd
function dcae-be {
    if [ ! ${DCAE_ENABLE} ] ; then
        return
    fi
    DOCKER_NAME="dcae-be"
    echo "docker run ${DOCKER_NAME}..."
    if [ ${LOCAL} = false ]; then
	    docker pull ${PREFIX}/${DOCKER_NAME}:${RELEASE}
    fi
    docker run --detach --name ${DOCKER_NAME} --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env JAVA_OPTIONS="${DCAE_BE_JAVA_OPTIONS}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD}  --volume ${WORKSPACE}/data/logs/DCAE-BE/:/var/lib/jetty/logs --volume ${WORKSPACE}/data/environments:/var/opt/dcae-be/chef-solo/environments --publish 8444:8444 --publish 8082:8082 ${PREFIX}/${DOCKER_NAME}:${RELEASE}
    command_exit_status $? ${DOCKER_NAME}
    echo "please wait while ${DOCKER_NAME^^} is starting....."
    monitor_docker ${DOCKER_NAME}
}
#


# DCAE Configuration
function dcae-tools {
    if [ ! ${DCAE_ENABLE} ] ; then
        return
    fi
    DOCKER_NAME="dcae-tools"
    echo "docker run ${DOCKER_NAME}..."
    if [ ${LOCAL} = false ]; then
	    docker pull ${PREFIX}/${DOCKER_NAME}:${RELEASE}
    fi
    docker run --detach --name ${DOCKER_NAME} --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" ${LOCAL_TIME_MOUNT_CMD}  --volume ${WORKSPACE}/data/logs/BE/:/var/lib/jetty/logs --volume ${WORKSPACE}/data/environments:/var/opt/dcae-tools/chef-solo/environments  ${PREFIX}/${DOCKER_NAME}:${RELEASE}
    command_exit_status $? ${DOCKER_NAME}
    echo "please wait while ${DOCKER_NAME^^} is starting....."
    monitor_docker ${DOCKER_NAME}

}
#


# DCAE FrontEnd
function dcae-fe {
    if [ ! ${DCAE_ENABLE} ] ; then
        return
    fi
    DOCKER_NAME="dcae-fe"
    echo "docker run ${DOCKER_NAME}..."
    if [ ${LOCAL} = false ]; then
	    docker pull ${PREFIX}/${DOCKER_NAME}:${RELEASE}
    fi
    docker run --detach --name ${DOCKER_NAME} --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env JAVA_OPTIONS="${DCAE_FE_JAVA_OPTIONS}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD}  --volume ${WORKSPACE}/data/logs/DCAE-FE/:/var/lib/jetty/logs --volume ${WORKSPACE}/data/environments:/var/opt/dcae-fe/chef-solo/environments/ --publish 9444:9444 --publish 8183:8183 ${PREFIX}/${DOCKER_NAME}:${RELEASE}
    command_exit_status $? ${DOCKER_NAME}
    echo "please wait while ${DOCKER_NAME^^} is starting....."
    monitor_docker ${DOCKER_NAME}

}
#


# apis-sanity
function sdc-api-tests {
    if [[ ${RUN_API_TESTS} = true ]] ; then
        healthCheck
        healthCheck_http_code=$?
        if [[ ${healthCheck_http_code} == 200 ]] ; then
            echo "docker run sdc-api-tests..."
            echo "Trigger sdc-api-tests docker, please wait..."

            if [ ${LOCAL} = false ]; then
                docker pull ${PREFIX}/sdc-api-tests:${RELEASE}
            fi

            docker run --detach --name sdc-api-tests --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env JAVA_OPTIONS="${API_TESTS_JAVA_OPTIONS}" --env SUITE_NAME=${API_SUITE} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD} --volume ${WORKSPACE}/data/logs/sdc-api-tests/target:/var/lib/tests/target --volume ${WORKSPACE}/data/logs/sdc-api-tests/ExtentReport:/var/lib/tests/ExtentReport --volume ${WORKSPACE}/data/logs/sdc-api-tests/outputCsar:/var/lib/tests/outputCsar --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 9560:9560 ${PREFIX}/sdc-api-tests:${RELEASE} echo "please wait while SDC-API-TESTS is starting....."
            monitor_docker sdc-api-tests
        fi
    fi
}
#


# ui-sanity
function sdc-ui-tests {

    if [[ ${RUN_UI_TESTS} = true ]] ; then
		healthCheck
        healthCheck_http_code=$?
        if [[ ${healthCheck_http_code} == 200 ]]; then
            echo "docker run sdc-ui-tets..."
            echo "Trigger sdc-ui-tests docker, please wait..."

            if [ ${LOCAL} = false ]; then
                docker pull ${PREFIX}/sdc-ui-tests:${RELEASE}
            fi
            RUN_SIMULATOR=true;
            sdc-sim
            docker run --detach --name sdc-ui-tests --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env JAVA_OPTIONS="${UI_TESTS_JAVA_OPTIONS}" --env SUITE_NAME=${UI_SUITE} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 ${LOCAL_TIME_MOUNT_CMD} --volume ${WORKSPACE}/data/logs/sdc-ui-tests/target:/var/lib/tests/target --volume ${WORKSPACE}/data/logs/sdc-ui-tests/ExtentReport:/var/lib/tests/ExtentReport --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 5901:5901 --publish 6901:6901 ${PREFIX}/sdc-ui-tests:${RELEASE}
            echo "please wait while SDC-UI-TESTS is starting....."
            monitor_docker sdc-ui-tests
        fi
    fi
}
#


# SDC-Simulator
function sdc-sim {
    if [ "${RUN_SIMULATOR}" == true ]; then
        echo "docker run sdc-webSimulator..."
        if [ ${LOCAL} = false ]; then
            docker pull ${PREFIX}/sdc-simulator:${RELEASE}
        fi

        probe_sim
        sim_stat=$?
        if [ ${sim_stat} == 1 ]; then
            docker run \
                --detach \
                --name sdc-sim \
                --env FE_URL="${FE_URL}" \
                --env JAVA_OPTIONS="${SIM_JAVA_OPTIONS}" \
                --env ENVNAME="${DEP_ENV}" \
                ${LOCAL_TIME_MOUNT_CMD} \
                --volume ${WORKSPACE}/data/logs/WS/:/var/lib/jetty/logs \
                --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments \
                --publish 8285:8080 \
                --publish 8286:8443 ${PREFIX}/sdc-simulator:${RELEASE}
            echo "please wait while SDC-WEB-SIMULATOR is starting....."
            monitor_docker sdc-sim
        fi
    fi
}
#


#
# Main
#

# Handle command line arguments
while [ $# -gt 0 ]; do
    case $1 in

	# -r | --release - The specific docker version to pull and deploy
    -r | --release )
          shift 1 ;
          RELEASE=$1;
          shift 1;;

	# -e | --environment - The environment name you want to deploy
    -e | --environment )
          shift 1;
          DEP_ENV=$1;
          shift 1 ;;

	# -p | --port - The port from which to connect to the docker nexus
    -p | --port )
          shift 1 ;
          PORT=$1;
          shift 1 ;;

	# -l | --local - Use this for deploying your local dockers without pulling them first
    -l | --local )
          LOCAL=true;
          shift 1;;

	# -ta - Use this for running the APIs sanity docker after all other dockers have been deployed
    -ta  )
          shift 1 ;
          API_SUITE=$1;
          RUN_API_TESTS=true;
          shift 1 ;;

	# -tu - Use this for running the UI sanity docker after all other dockers have been deployed
    -tu  )
          shift 1 ;
	      UI_SUITE=$1;
          RUN_UI_TESTS=true;
          shift 1 ;;

    # -tad - Use this for running the DEFAULT suite of tests in APIs sanity docker after all other dockers have been deployed
    -tad | -t )
          API_SUITE="onapApiSanity";
          RUN_API_TESTS=true;
          shift 1 ;;

	# -tud - Use this for running the DEFAULT suite of tests in UI sanity docker after all other dockers have been deployed
    -tud   )
          UI_SUITE="onapUiSanity";
          RUN_UI_TESTS=true;
          shift 1 ;;

    # -d | --docker - The init specified docker
    -d | --docker )
          shift 1 ;
          DOCKER=$1;
          shift 1 ;;
    # -sim | --simulator run the simulator
    -sim | --simulator )
         RUN_SIMULATOR=true;
         shift 1 ;;
    # -sim | --simulator run the simulator
    -u | --fe_url )
         shift 1 ;
         FE_URL=$1;
         shift 1 ;;
    # -dcae | --dcae - Use this to deploy DCAE upon SDC
    -dcae | --dcae )
         shift 1 ;
         DCAE_ENABLE='True';;

	# -h | --help - Display the help message with all the available run options
    -h | --help )
          usage;
          exit  ${SUCCESS};;

         * )
          usage;
          exit  ${FAILURE};;
    esac
done


#Prefix those with WORKSPACE so it can be set to something other than /opt
[ -f ${WORKSPACE}/opt/config/env_name.txt ] && DEP_ENV=$(cat ${WORKSPACE}/opt/config/env_name.txt) || echo ${DEP_ENV}
[ -f ${WORKSPACE}/opt/config/nexus_username.txt ] && NEXUS_USERNAME=$(cat ${WORKSPACE}/opt/config/nexus_username.txt)    || NEXUS_USERNAME=release
[ -f ${WORKSPACE}/opt/config/nexus_password.txt ] && NEXUS_PASSWD=$(cat ${WORKSPACE}/opt/config/nexus_password.txt)      || NEXUS_PASSWD=sfWU3DFVdBr7GVxB85mTYgAW
[ -f ${WORKSPACE}/opt/config/nexus_docker_repo.txt ] && NEXUS_DOCKER_REPO=$(cat ${WORKSPACE}/opt/config/nexus_docker_repo.txt) || NEXUS_DOCKER_REPO=nexus3.onap.org:${PORT}
[ -f ${WORKSPACE}/opt/config/nexus_username.txt ] && docker login -u $NEXUS_USERNAME -p $NEXUS_PASSWD $NEXUS_DOCKER_REPO


export IP=`ip route get 8.8.8.8 | awk '/src/{ print $7 }'`
#If OSX, then use this to get IP
if [[ "$OSTYPE" == "darwin"* ]]; then
    export IP=$(ipconfig getifaddr en0)
fi
export PREFIX=${NEXUS_DOCKER_REPO}'/onap'

if [ ${LOCAL} = true ]; then
	PREFIX='onap'
fi

echo ""

if [ -z "${DOCKER}" ]; then
    cleanup all
	dir_perms
	sdc-es
	sdc-init-es
	sdc-cs
	sdc-cs-init
#	sdc-kbn
	sdc-cs-onboard-init
	sdc-onboard-BE
	sdc-BE
	sdc-BE-init
	sdc-FE
	dcae-be
	dcae-tools
	dcae-fe
	healthCheck
    sdc-sim
	sdc-api-tests
	sdc-ui-tests
else
	cleanup ${DOCKER}
	dir_perms
	${DOCKER}
    healthCheck
fi
