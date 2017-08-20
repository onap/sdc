#!/bin/bash


function usage {
    echo "usage: simulator_docker_run.sh [ -r|--release <RELEASE-NAME> ]  [ -e|--environment <ENV-NAME> ] [ -p|--port <Docker-hub-port>] [ -h|--help ]"
}


function cleanup {
	echo "performing old dockers cleanup"
	docker_ids=`docker ps -a | egrep "openecomp/sdc-simulator|Exit" | awk '{print $1}'`
	for X in ${docker_ids}
	do
	   docker rm -f ${X}
	done
}


function dir_perms {
	mkdir -p /data/logs/WS/
	chmod -R 777 /data/logs
}


RELEASE=latest
LOCAL=true
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


cleanup


export IP=`ifconfig eth0 | awk -F: '/inet addr/ {gsub(/ .*/,"",$2); print $2}'`
export PREFIX=${NEXUS_DOCKER_REPO}'/openecomp'

PREFIX='openecomp'

echo ""
echo "${PREFIX}"

dir_perms

# SDC-Simulator
docker run --detach --name sdc-simulator --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env http_proxy=${http_proxy} --env https_proxy=${https_proxy} --env no_proxy=${no_proxy} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 256m --memory-swap=256m --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/logs/WS/:/var/lib/jetty/logs --volume /data/environments:/root/chef-solo/environments --publish 8285:8080 ${PREFIX}/sdc-simulator:${RELEASE}


if [ $? -ne 0 ]; then
    exit 1
fi

