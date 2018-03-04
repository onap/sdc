#!/bin/bash


function usage {
    echo "usage: sanity_run.sh [ -r|--release <RELEASE-NAME> ]  [ -e|--environment <ENV-NAME> ] [ -p|--port <Docker-hub-port>] [ -l|--local <Run-without-pull>] [ -t|--runTests <Run-with-sanityDocker>] [ -h|--help ]"
}

function cleanup {
    echo "performing old dockers cleanup"
    docker_ids=`docker ps -a | egrep -v "onap/sdc-simulator" | egrep "ecomp-nexus:${PORT}/sdc|sdc-sanity|Exit" | awk '{print $1}'`
    for X in ${docker_ids}
    do
        docker rm -f ${X}
    done
}


function dir_perms {
    mkdir -p ${WORKSPACE}/data/logs/BE/SDC/SDC-BE
    mkdir -p ${WORKSPACE}/data/logs/FE/SDC/SDC-FE
    chmod -R 777 ${WORKSPACE}/data/logs
}

function monitor_docker {

    echo monitor $1 Docker
    sleep 5
    TIME_OUT=900
    INTERVAL=20
    TIME=0
    while [ "$TIME" -lt "$TIME_OUT" ]; do

        MATCH=`docker logs --tail 30 $1 | grep "DOCKER STARTED"`
        echo MATCH is -- $MATCH

        if [ -n "$MATCH" ]; then
            echo DOCKER start finished in $TIME seconds
            break
        fi

        echo Sleep: $INTERVAL seconds before testing if $1 DOCKER is up. Total wait time up now is: $TIME seconds. Timeout is: $TIME_OUT seconds
        sleep $INTERVAL
        TIME=$(($TIME+$INTERVAL))
    done

    if [ "$TIME" -ge "$TIME_OUT" ]; then
        echo -e "\e[1;31mTIME OUT: DOCKER was NOT fully started in $TIME_OUT seconds... Could cause problems ...\e[0m"
    fi

}

function healthCheck {
	curl localhost:9200/_cluster/health?pretty=true

	echo "BE health-Check:"
	curl http://localhost:8080/sdc2/rest/healthCheck

	echo ""
	echo ""
	echo "FE health-Check:"
	curl http://localhost:8181/sdc1/rest/healthCheck


	echo ""
	echo ""
	healthCheck_http_code=$(curl -o /dev/null -w '%{http_code}' -H "Accept: application/json" -H "Content-Type: application/json" -H "USER_ID: jh0003" http://localhost:8080/sdc2/rest/v1/user/demo;)
	if [[ ${healthCheck_http_code} != 200 ]]
	then
		echo "Error [${healthCheck_http_code}] while user existance check"
		return ${healthCheck_http_code}
	fi
	echo "check user existance: OK"
	return ${healthCheck_http_code}
}

function elasticHealthCheck {
	echo "Elastic Health-Check:"
	
	COUNTER=0
    while [  $COUNTER -lt 20 ]; do
    	echo "Waiting ES docker to start"
  		health_Check_http_code=$(curl -o /dev/null -w '%{http_code}' http://localhost:9200/_cluster/health?wait_for_status=yellow&timeout=120s)
  		if [[ "$health_Check_http_code" -eq 200 ]]
		then
			break
		fi
		let COUNTER=COUNTER+1 
		sleep 4
	done
	
	healthCheck_http_code=$(curl -o /dev/null -w '%{http_code}' http://localhost:9200/_cluster/health?wait_for_status=yellow&timeout=120s)
	if [[ "$health_Check_http_code" != 200 ]]
	then
		echo "Error [${healthCheck_http_code}] ES NOT started correctly"
		exit ${healthCheck_http_code}
	fi
	echo "ES started correctly"
	curl localhost:9200/_cluster/health?pretty=true
	return ${healthCheck_http_code}
}

RELEASE=latest
LOCAL=false
RUNTESTS=true
DEBUG_PORT="--publish 4000:4000"

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
	# -t | --runTests - Use this for running the sanity tests docker after all other dockers have been deployed
    -t | --runTests )
          RUNTESTS=true;
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


[ -f /opt/config/env_name.txt ] && DEP_ENV=$(cat /opt/config/env_name.txt) || DEP_ENV=__ENV-NAME__
[ -f /opt/config/nexus_username.txt ] && NEXUS_USERNAME=$(cat /opt/config/nexus_username.txt)    || NEXUS_USERNAME=release
[ -f /opt/config/nexus_password.txt ] && NEXUS_PASSWD=$(cat /opt/config/nexus_password.txt)      || NEXUS_PASSWD=sfWU3DFVdBr7GVxB85mTYgAW
[ -f /opt/config/nexus_docker_repo.txt ] && NEXUS_DOCKER_REPO=$(cat /opt/config/nexus_docker_repo.txt) || NEXUS_DOCKER_REPO=nexus3.onap.org:${PORT}
[ -f /opt/config/nexus_username.txt ] && docker login -u $NEXUS_USERNAME -p $NEXUS_PASSWD $NEXUS_DOCKER_REPO


cleanup


export IP=`ip route get 8.8.8.8 | awk '/src/{ print $7 }'`
export PREFIX=${NEXUS_DOCKER_REPO}'/onap'

if [ ${LOCAL} = true ]; then
	PREFIX='onap'
fi

echo ""

healthCheck

# sanityDocker
if [[ (${RUNTESTS} = true) && (${healthCheck_http_code} == 200) ]]; then
    echo "docker run sdc-sanity..."
    echo "Triger sanity docker, please wait..."
	
    if [ ${LOCAL} = false ]; then
        docker pull ${PREFIX}/sdc-sanity:${RELEASE}
    fi

docker run --detach --name sdc-sanity --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env http_proxy=${http_proxy} --env https_proxy=${https_proxy} --env no_proxy=${no_proxy} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 1500m --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume ${WORKSPACE}/data/logs/sdc-sanity/target:/var/lib/tests/target --volume ${WORKSPACE}/data/logs/sdc-sanity/ExtentReport:/var/lib/tests/ExtentReport --volume ${WORKSPACE}/data/logs/sdc-sanity/outputCsar:/var/lib/tests/outputCsar --volume ${WORKSPACE}/data/environments:/root/chef-solo/environments --publish 9560:9560 ${PREFIX}/sdc-sanity:${RELEASE}
echo "please wait while SANITY is starting....."
monitor_docker sdc-sanity

fi
