#!/bin/bash

CS_PASSWORD="onap123#@!"
SDC_USER="asdc_user"
SDC_PASSWORD="Aa1234%^!"
JETTY_BASE="/var/lib/jetty"
BE_JAVA_OPTIONS="-Xdebug -agentlib:jdwp=transport=dt_socket,address=4000,server=y,suspend=n -Xmx1536m -Xms1536m"
FE_JAVA_OPTIONS="-Xdebug -agentlib:jdwp=transport=dt_socket,address=6000,server=y,suspend=n -Xmx256m -Xms256m"
ONBOARD_BE_JAVA_OPTIONS="-Xdebug -agentlib:jdwp=transport=dt_socket,address=4001,server=y,suspend=n -Xmx2g -Xms2g"
SIM_JAVA_OPTIONS=" -Xmx128m -Xms128m -Xss1m"
API_TESTS_JAVA_OPTIONS="-Xmx512m -Xms512m"
UI_TESTS_JAVA_OPTIONS="-Xmx1024m -Xms1024m"

function usage {
    echo "usage: docker_run.sh [ -r|--release <RELEASE-NAME> ]  [ -e|--environment <ENV-NAME> ] [ -p|--port <Docker-hub-port>] [ -l|--local <Run-without-pull>] [ -t|--runTests <Run-with-sanityDocker>] [ -h|--help ]"
}


function cleanup {
    echo "performing old dockers cleanup"

	if [ "$1" == "all" ] ; then
		docker_ids=`docker ps -a | egrep -v "onap/sdc-simulator" | egrep "ecomp-nexus:${PORT}/sdc|sdc|Exit" | awk '{print $1}'`
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


function dir_perms {
    mkdir -p ${WORKSPACE}/data/logs/BE/SDC/SDC-BE
    mkdir -p ${WORKSPACE}/data/logs/FE/SDC/SDC-FE
    mkdir -p ${WORKSPACE}/data/logs/sdc-api-tests/ExtentReport
    mkdir -p ${WORKSPACE}/data/logs/ONBOARD-BE/SDC/SDC-BE
	mkdir -p ${WORKSPACE}/data/logs/sdc-api-tests/target
	mkdir -p ${WORKSPACE}/data/logs/sdc-ui-tests/ExtentReport
	mkdir -p ${WORKSPACE}/data/logs/sdc-ui-tests/target
	mkdir -p ${WORKSPACE}/data/logs/docker_logs
    chmod -R 777 ${WORKSPACE}/data/logs
}

function docker_logs {

docker logs $1 > ${WORKSPACE}/data/logs/docker_logs/$1_docker.log

}

function probe_cs {

cs_stat=false
docker exec $1 /var/lib/ready-probe.sh > /dev/null 2>&1
rc=$?
if [[ $rc == 0 ]]; then
  echo DOCKER start finished in $2 seconds
  cs_stat=true
fi

}

function probe_be {

be_stat=false
docker exec $1 /var/lib/ready-probe.sh > /dev/null 2>&1
rc=$?
if [[ $rc == 200 ]]; then
  echo DOCKER start finished in $2 seconds
  be_stat=true
fi

}

function probe_sdc_onboard_be {

sdc_onboard_be_stat=false
docker exec $1 /var/lib/ready-probe.sh > /dev/null 2>&1
rc=$?
if [[ $rc == 200 ]]; then
  echo DOCKER start finished in $2 seconds
  sdc_onboard_be_stat=true
fi

}

function probe_fe {

fe_stat=false
docker exec $1 /var/lib/ready-probe.sh > /dev/null 2>&1
rc=$?
if [[ $rc == 200 ]]; then
  echo DOCKER start finished in $2 seconds
  fe_stat=true
fi

}

function probe_es {

es_stat=false
health_Check_http_code=$(curl --noproxy "*" -o /dev/null -w '%{http_code}' http://${IP}:9200/_cluster/health?wait_for_status=yellow&timeout=120s)
if [[ "$health_Check_http_code" -eq 200 ]]
 then
   echo DOCKER start finished in $2 seconds
   es_stat=true
 fi

}

function probe_sim {

if lsof -Pi :8285 -sTCP:LISTEN -t >/dev/null ; then
    echo "running"
    sim_stat=true
else
    echo "not running"
    sim_stat=false
fi


}

function probe_docker {

match_result=false
MATCH=`docker logs --tail 30 $1 | grep "DOCKER STARTED"`
echo MATCH is -- $MATCH

if [ -n "$MATCH" ]; then
   echo DOCKER start finished in $2 seconds
   match_result=true
fi
}
function monitor_docker {

    echo monitor $1 Docker
    sleep 5
    TIME_OUT=900
    INTERVAL=20
    TIME=0
    while [ "$TIME" -lt "$TIME_OUT" ]; do
       if [ "$1" == "sdc-cs" ]; then
                    probe_cs $1 $TIME
                if [[ $cs_stat == true ]]; then break; fi
		elif [ "$1" == "sdc-es" ]; then
		    probe_es $1 $TIME
			if [[ $es_stat == true ]]; then break; fi
		elif [ "$1" == "sdc-BE" ]; then
		    probe_be $1 $TIME
			if [[ $be_stat == true ]]; then break; fi
		elif [ "$1" == "sdc-FE" ]; then
		    probe_fe $1 $TIME
			if [[ $fe_stat == true ]]; then break; fi
        elif [ "$1" == "sdc-onboard-BE" ]; then
             probe_sdc_onboard_be $1 $TIME
             if [[ $sdc_onboard_be_stat == true ]]; then break; fi

        else
            probe_docker $1 $TIME
            if [[ $match_result == true ]]; then break; fi
        fi
        echo Sleep: $INTERVAL seconds before testing if $1 DOCKER is up. Total wait time up now is: $TIME seconds. Timeout is: $TIME_OUT seconds
        sleep $INTERVAL
        TIME=$(($TIME+$INTERVAL))
    done

    docker_logs $1

    if [ "$TIME" -ge "$TIME_OUT" ]; then
        echo -e "\e[1;31mTIME OUT: DOCKER was NOT fully started in $TIME_OUT seconds... Could cause problems ...\e[0m"
    fi

}

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
	if [[ ${healthCheck_http_code} != 200 ]]
	then
		echo "Error [${healthCheck_http_code}] while user existance check"
		return ${healthCheck_http_code}
	fi
	echo "check user existance: OK"
	return ${healthCheck_http_code}
}

RELEASE=latest
LOCAL=false
RUNTESTS=false
DEBUG_PORT="--publish 4000:4000"
ONBOARD_DEBUG_PORT="--publish 4001:4000"

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
	# -h | --help - Display the help message with all the available run options
    -h | --help )
          usage;
          exit  0;;
         * )
          usage;
          exit  1;;
    esac
done


[ -f /opt/config/env_name.txt ] && DEP_ENV=$(cat /opt/config/env_name.txt) || echo ${DEP_ENV}
[ -f /opt/config/nexus_username.txt ] && NEXUS_USERNAME=$(cat /opt/config/nexus_username.txt)    || NEXUS_USERNAME=release
[ -f /opt/config/nexus_password.txt ] && NEXUS_PASSWD=$(cat /opt/config/nexus_password.txt)      || NEXUS_PASSWD=sfWU3DFVdBr7GVxB85mTYgAW
[ -f /opt/config/nexus_docker_repo.txt ] && NEXUS_DOCKER_REPO=$(cat /opt/config/nexus_docker_repo.txt) || NEXUS_DOCKER_REPO=nexus3.onap.org:${PORT}
[ -f /opt/config/nexus_username.txt ] && docker login -u $NEXUS_USERNAME -p $NEXUS_PASSWD $NEXUS_DOCKER_REPO

export IP=`ip route get 8.8.8.8 | awk '/src/{ print $7 }'`
export PREFIX=${NEXUS_DOCKER_REPO}'/onap'

if [ ${LOCAL} = true ]; then
	PREFIX='onap'
fi

echo ""


#Elastic-Search
function sdc-es {
echo "docker run sdc-elasticsearch..."
if [ ${LOCAL} = false ]; then
	echo "pulling code"
	docker pull ${PREFIX}/sdc-elasticsearch:${RELEASE}
fi
docker run -dit --name sdc-es --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --env ES_JAVA_OPTS="-Xms512m -Xmx512m" --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --env ES_HEAP_SIZE=1024M --volume ${WORKSPACE}/data/ES:/usr/share/elasticsearch/data --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 9200:9200 --publish 9300:9300 ${PREFIX}/sdc-elasticsearch:${RELEASE} /bin/sh

echo "please wait while ES is starting..."
monitor_docker sdc-es
}


#Init-Elastic-Search
function sdc-init-es {
echo "docker run sdc-init-elasticsearch..."
if [ ${LOCAL} = false ]; then
	echo "pulling code"
	docker pull ${PREFIX}/sdc-init-elasticsearch:${RELEASE}
fi
echo "Running sdc-init-es"
docker run --name sdc-init-es --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments ${PREFIX}/sdc-init-elasticsearch:${RELEASE} > /dev/null 2>&1
rc=$?
docker_logs sdc-init-es
if [[ $rc != 0 ]]; then exit $rc; fi

}

#Cassandra
function sdc-cs {
echo "docker run sdc-cassandra..."
if [ ${LOCAL} = false ]; then
	docker pull ${PREFIX}/sdc-cassandra:${RELEASE}
fi
docker run -dit --name sdc-cs --env RELEASE="${RELEASE}" --env CS_PASSWORD="${CS_PASSWORD}" --env ENVNAME="${DEP_ENV}" --env HOST_IP=${IP} --env MAX_HEAP_SIZE="1536M" --env HEAP_NEWSIZE="512M" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume ${WORKSPACE}/data/CS:/var/lib/cassandra --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 9042:9042 --publish 9160:9160 ${PREFIX}/sdc-cassandra:${RELEASE} /bin/sh


echo "please wait while CS is starting..."
monitor_docker sdc-cs
}

#Cassandra-init
function sdc-cs-init {
echo "docker run sdc-cassandra-init..."
if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-cassandra-init:${RELEASE}
fi
docker run --name sdc-cs-init --env RELEASE="${RELEASE}" --env SDC_USER="${SDC_USER}" --env SDC_PASSWORD="${SDC_PASSWORD}" --env CS_PASSWORD="${CS_PASSWORD}" --env ENVNAME="${DEP_ENV}" --env HOST_IP=${IP} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume ${WORKSPACE}/data/CS:/var/lib/cassandra --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --volume ${WORKSPACE}/data/CS-Init:/root/chef-solo/cache ${PREFIX}/sdc-cassandra-init:${RELEASE} > /dev/null 2>&1
rc=$?
docker_logs sdc-cs-init
if [[ $rc != 0 ]]; then exit $rc; fi
}

#Onboard Cassandra-init
function sdc-cs-onboard-init {
echo "docker run sdc-cs-onboard-init..."
if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-onboard-cassandra-init:${RELEASE}
fi
docker run --name sdc-cs-onboard-init --env RELEASE="${RELEASE}" --env HOST_IP=${IP} --env SDC_USER="${SDC_USER}" --env SDC_PASS="${SDC_PASSWORD}" --env CS_PASSWORD="${CS_PASSWORD}" --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume ${WORKSPACE}/data/CS:/var/lib/cassandra --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --volume ${WORKSPACE}/data/CS-Init:/root/chef-solo/cache ${PREFIX}/sdc-onboard-cassandra-init:${RELEASE}
rc=$?
docker_logs sdc-onboard-cs-init
if [[ $rc != 0 ]]; then exit $rc; fi
}

#Kibana
function sdc-kbn {
echo "docker run sdc-kibana..."
if [ ${LOCAL} = false ]; then
	docker pull ${PREFIX}/sdc-kibana:${RELEASE}
docker run --detach --name sdc-kbn --env ENVNAME="${DEP_ENV}" --env NODE_OPTIONS="--max-old-space-size=200" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 5601:5601 ${PREFIX}/sdc-kibana:${RELEASE}
fi

}

#Back-End
function sdc-BE {
echo "docker run sdc-backend..."
if [ ${LOCAL} = false ]; then
	docker pull ${PREFIX}/sdc-backend:${RELEASE}
else
	ADDITIONAL_ARGUMENTS=${DEBUG_PORT}
fi
docker run --detach --name sdc-BE --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env JAVA_OPTIONS="${BE_JAVA_OPTIONS}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume ${WORKSPACE}/data/logs/BE/:/var/lib/jetty/logs  --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 8443:8443 --publish 8080:8080 ${ADDITIONAL_ARGUMENTS} ${PREFIX}/sdc-backend:${RELEASE}

echo "please wait while BE is starting..."
monitor_docker sdc-BE
}

# Back-End-Init
function sdc-BE-init {
echo "docker run sdc-backend-init..."
if [ ${LOCAL} = false ]; then
	docker pull ${PREFIX}/sdc-backend-init:${RELEASE}
fi
docker run --name sdc-BE-init --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume ${WORKSPACE}/data/logs/BE/:/var/lib/jetty/logs  --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments ${PREFIX}/sdc-backend-init:${RELEASE} > /dev/null 2>&1
rc=$?
docker_logs sdc-BE-init
if [[ $rc != 0 ]]; then exit $rc; fi
}

# Onboard Back-End
function sdc-onboard-BE {

dir_perms
# Back-End
echo "docker run  sdc-onboard-BE ..."
if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-onboard-backend:${RELEASE}
else
        ADDITIONAL_ARGUMENTS=${ONBOARD_DEBUG_PORT}
fi
docker run --detach --name sdc-onboard-BE --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env SDC_CLUSTER_NAME="SDC-CS-${DEP_ENV}" --env SDC_USER="${SDC_USER}" --env SDC_PASSWORD="${SDC_PASSWORD}" --env JAVA_OPTIONS="${ONBOARD_BE_JAVA_OPTIONS}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume ${WORKSPACE}/data/logs/ONBOARD-BE:/var/lib/jetty/logs --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 8444:8443 --publish 8081:8080 ${ADDITIONAL_ARGUMENTS} ${PREFIX}/sdc-onboard-backend:${RELEASE}

echo "please wait while sdc-onboard-BE is starting..."
monitor_docker sdc-onboard-BE
}


# Front-End
function sdc-FE {
echo "docker run sdc-frontend..."
if [ ${LOCAL} = false ]; then
	docker pull ${PREFIX}/sdc-frontend:${RELEASE}
fi
docker run --detach --name sdc-FE --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env JAVA_OPTIONS="${FE_JAVA_OPTIONS}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro  --volume ${WORKSPACE}/data/logs/FE/:/var/lib/jetty/logs --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 9443:9443 --publish 8181:8181 ${PREFIX}/sdc-frontend:${RELEASE}

echo "please wait while FE is starting....."
monitor_docker sdc-FE
}


# apis-sanity
function sdc-api-tests {
healthCheck
if [[ (${RUN_API_TESTS} = true) && (${healthCheck_http_code} == 200) ]]; then
    echo "docker run sdc-api-tests..."
    echo "Triger sdc-api-tests docker, please wait..."

    if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-api-tests:${RELEASE}
    fi

docker run --detach --name sdc-api-tests --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env JAVA_OPTIONS="${API_TESTS_JAVA_OPTIONS}" --env SUITE_NAME=${API_SUITE} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume ${WORKSPACE}/data/logs/sdc-api-tests/target:/var/lib/tests/target --volume ${WORKSPACE}/data/logs/sdc-api-tests/ExtentReport:/var/lib/tests/ExtentReport --volume ${WORKSPACE}/data/logs/sdc-api-tests/outputCsar:/var/lib/tests/outputCsar --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 9560:9560 ${PREFIX}/sdc-api-tests:${RELEASE} echo "please wait while SDC-API-TESTS is starting....."
monitor_docker sdc-api-tests

fi
}

# ui-sanity
function sdc-ui-tests {
healthCheck
if [[ (${RUN_UI_TESTS} = true) && (${healthCheck_http_code} == 200) ]]; then
    echo "docker run sdc-ui-tets..."
    echo "Triger sdc-ui-tests docker, please wait..."

    if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-ui-tests:${RELEASE}
    fi

sdc-sim
docker run --detach --name sdc-ui-tests --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env JAVA_OPTIONS="${UI_TESTS_JAVA_OPTIONS}" --env SUITE_NAME=${UI_SUITE} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume ${WORKSPACE}/data/logs/sdc-ui-tests/target:/var/lib/tests/target --volume ${WORKSPACE}/data/logs/sdc-ui-tests/ExtentReport:/var/lib/tests/ExtentReport --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 5901:5901 --publish 6901:6901 ${PREFIX}/sdc-ui-tests:${RELEASE}
echo "please wait while SDC-UI-TESTS is starting....."
monitor_docker sdc-ui-tests

fi
}


# SDC-Simulator
function sdc-sim {
echo "docker run sdc-webSimulator..."
if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-sim:${RELEASE}
fi

probe_sim
if [ sim_stat=false ]; then

docker run --detach --name sdc-sim  --env JAVA_OPTIONS="${SIM_JAVA_OPTIONS}" --env ENVNAME="${DEP_ENV}" --volume /etc/localtime:/etc/localtime:ro --volume /data/logs/WS/:/var/lib/jetty/logs --volume /data/environments:/root/chef-solo/environments --publish 8285:8080 --publish 8286:8443 ${PREFIX}/sdc-simulator:${RELEASE}
echo "please wait while SDC-WEB-SIMULATOR is starting....."
monitor_docker sdc-sim

fi
}

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
    healthCheck
	sdc-api-tests
        sdc-ui-tests
else
	cleanup ${DOCKER}
	dir_perms
	${DOCKER}
    healthCheck
fi
