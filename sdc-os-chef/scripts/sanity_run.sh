#!/bin/bash


function usage {
    echo "usage: docker_run.sh [ -r|--release <RELEASE-NAME> ]  [ -e|--environment <ENV-NAME> ] [ -p|--port <Docker-hub-port>] [ -l|--local <Run-without-pull>] [ -s|--skipTests <Run-without-sanityDocker>] [ -h|--help ]"
}


function cleanup {
	echo "performing old dockers cleanup"
	docker_ids=`docker ps -a | egrep -v "openecomp/sdc-simulator" | egrep "ecomp-nexus:${PORT}/sdc-sanity|sdc-sanity|Exit" | awk '{print $1}'`
	for X in ${docker_ids}
	do
	   docker rm -f ${X}
	done
}


function dir_perms {
	mkdir -p /data/logs/BE/SDC/SDC-BE
	mkdir -p /data/logs/FE/SDC/SDC-FE
	chmod -R 777 /data/logs
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

if [ -n "$MATCH" ]
 then
    echo DOCKER start finished in $TIME seconds
    break
  fi

  echo Sleep: $INTERVAL seconds before testing if $1 DOCKER is up. Total wait time up now is: $TIME seconds. Timeout is: $TIME_OUT seconds
  sleep $INTERVAL
  TIME=$(($TIME+$INTERVAL))
done

if [ "$TIME" -ge "$TIME_OUT" ]
 then
   echo -e "\e[1;31mTIME OUT: DOCKER was NOT fully started in $TIME_OUT seconds... Could cause problems ...\e[0m"
fi


}


RELEASE=latest
LOCAL=false
SKIPTESTS=false
DEBUG_PORT="--publish 4000:4000"

[ -f /opt/config/env_name.txt ] && DEP_ENV=$(cat /opt/config/env_name.txt) || DEP_ENV=__ENV-NAME__
[ -f /opt/config/nexus_username.txt ] && NEXUS_USERNAME=$(cat /opt/config/nexus_username.txt)    || NEXUS_USERNAME=release
[ -f /opt/config/nexus_password.txt ] && NEXUS_PASSWD=$(cat /opt/config/nexus_password.txt)      || NEXUS_PASSWD=sfWU3DFVdBr7GVxB85mTYgAW
[ -f /opt/config/nexus_docker_repo.txt ] && NEXUS_DOCKER_REPO=$(cat /opt/config/nexus_docker_repo.txt) || NEXUS_DOCKER_REPO=ecomp-nexus:${PORT}

while [ "$1" != "" ]; do
    case $1 in
        -r | --release )
            shift
            RELEASE=${1}
            ;;
        -e | --environment )
			shift
            DEP_ENV=${1}
            ;;
		-p | --port )
            shift
            PORT=${1}
			;;
		-l | --local )
		shift
		LOCAL=true
		;;
		-s | --skipTests )
		shift
		SKIPTESTS=true
		;;
        -h | --help )
			usage
            exit
            ;;
        * )
    		usage
            exit 1
    esac
    shift
done

[ -f /opt/config/nexus_username.txt ] && docker login -u $NEXUS_USERNAME -p $NEXUS_PASSWD $NEXUS_DOCKER_REPO

export IP=`ifconfig eth0 | awk -F: '/inet addr/ {gsub(/ .*/,"",$2); print $2}'`
export PREFIX=${NEXUS_DOCKER_REPO}'/openecomp'

if [ ${LOCAL} = true ]; then
	PREFIX='openecomp'
fi

## sanityDocker
echo "docker run sdc-sanity..."
if [ ${SKIPTESTS} = false ]; then
echo "Triger sanity docker, please wait..."
    if [ ${LOCAL} = false ]; then
	   docker pull ${PREFIX}/sdc-sanity:${RELEASE}
    fi
	docker run --detach --name sdc-sanity --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env http_proxy=${http_proxy} --env https_proxy=${https_proxy} --env no_proxy=${no_proxy} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 1g --memory-swap=1g --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/logs/sdc-sanity/target:/var/lib/tests/target --volume /data/logs/sdc-sanity/ExtentReport:/var/lib/tests/ExtentReport --volume /data/logs/sdc-sanity/outputCsar:/var/lib/tests/outputCsar --volume /data/environments:/root/chef-solo/environments --publish 9560:9560 ${PREFIX}/sdc-sanity:${RELEASE}
fi