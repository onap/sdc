#!/bin/bash


function usage {
    echo "usage: docker_run.sh [ -r|--release <RELEASE-NAME> ]  [ -e|--environment <ENV-NAME> ] [ -p|--port <Docker-hub-port>] [ -h|--help ]"
}


function cleanup {
	echo "performing old dockers cleanup"
	docker_ids=`docker ps -a | egrep "ecomp-nexus:${PORT}/sdc|sdc|Exit" | awk '{print $1}'`
	for X in ${docker_ids}
	do
	   docker rm -f ${X}
	done
}


function dir_perms {
	mkdir -p /data/logs/BE/ASDC/ASDC-BE
	mkdir -p /data/logs/FE/ASDC/ASDC-FE
	chmod -R 777 /data/logs
}


RELEASE=latest
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

echo ""

# Elastic-Search
echo "docker run sdc-elasticsearch..."
docker pull ${NEXUS_DOCKER_REPO}/openecomp/sdc-elasticsearch:${RELEASE}
docker run --detach --name sdc-es --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --memory 1g --memory-swap=1g --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro -e ES_HEAP_SIZE=1024M --volume /data/ES:/usr/share/elasticsearch/data --volume /data/environments:/root/chef-solo/environments --publish 9200:9200 --publish 9300:9300 ${NEXUS_DOCKER_REPO}/openecomp/sdc-elasticsearch:${RELEASE}


# cassandra
echo "docker run sdc-cassandra..."
docker pull ${NEXUS_DOCKER_REPO}/openecomp/sdc-cassandra:${RELEASE}
docker run --detach --name sdc-cs --env RELEASE="${RELEASE}" --env ENVNAME="${DEP_ENV}" --env HOST_IP=${IP} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/CS:/var/lib/cassandra --volume /data/environments:/root/chef-solo/environments --publish 9042:9042 --publish 9160:9160 ${NEXUS_DOCKER_REPO}/openecomp/sdc-cassandra:${RELEASE}


echo "please wait while CS is starting..."
echo ""
c=120 # seconds to wait
REWRITE="\e[25D\e[1A\e[K"
while [ $c -gt 0 ]; do
    c=$((c-1))
    sleep 1
    echo -e "${REWRITE}$c"
done
echo -e ""


# kibana
echo "docker run sdc-kibana..."
docker pull ${NEXUS_DOCKER_REPO}/openecomp/sdc-kibana:${RELEASE}
docker run --detach --name sdc-kbn --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 2g --memory-swap=2g --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/environments:/root/chef-solo/environments --publish 5601:5601 ${NEXUS_DOCKER_REPO}/openecomp/sdc-kibana:${RELEASE}

dir_perms

# Back-End
echo "docker run sdc-backend..."
docker pull ${NEXUS_DOCKER_REPO}/openecomp/sdc-backend:${RELEASE}
docker run --detach --name sdc-BE --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 4g --memory-swap=4g --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/logs/BE/:/var/lib/jetty/logs  --volume /data/environments:/root/chef-solo/environments --publish 8443:8443 --publish 8080:8080 ${NEXUS_DOCKER_REPO}/openecomp/sdc-backend:${RELEASE}

echo "please wait while BE is starting..."
echo ""
c=45 # seconds to wait
REWRITE="\e[45D\e[1A\e[K"
while [ $c -gt 0 ]; do
    c=$((c-1))
    sleep 1
    echo -e "${REWRITE}$c"
done
echo -e ""


# Front-End
echo "docker run sdc-frontend..."
docker pull ${NEXUS_DOCKER_REPO}/openecomp/sdc-frontend:${RELEASE}
docker run --detach --name sdc-FE --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 2g --memory-swap=2g --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro  --volume /data/logs/FE/:/var/lib/jetty/logs --volume /data/environments:/root/chef-solo/environments --publish 9443:9443 --publish 8181:8181 ${NEXUS_DOCKER_REPO}/openecomp/sdc-frontend:${RELEASE}



# running healthCheck scripts
echo "Running health checks, please wait..."
echo ""
c=25 # seconds to wait
REWRITE="\e[45D\e[1A\e[K"
while [ $c -gt 0 ]; do
    c=$((c-1))
    sleep 1
    echo -e "${REWRITE}$c"
done
echo -e ""

/data/scripts/docker_health.sh

if [ $? -ne 0 ]; then
    exit 1
fi

