#!/bin/bash


function usage
{
    echo "usage: docker_run.sh [ -r|--release <RELEASE-NAME> ]  [ -e|--environment <ENV-NAME> ] [ -p|--port <Docker-hub-port>] [ -h|--help ]"
}


RELEASE=__SDC-RELEASE__
[ -f /opt/config/env_name.txt ] && DEP_ENV=$(cat /opt/config/env_name.txt) || DEP_ENV=__ENV-NAME__
PORT=51212

while [ "$1" != "" ]; do
    case $1 in
	    -d | --docker )
		    shift
			DOCKER=${1}
			;;
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

[ -f /opt/config/nexus_username.txt ] && NEXUS_USERNAME=$(cat /opt/config/nexus_username.txt)    || NEXUS_USERNAME=release
[ -f /opt/config/nexus_password.txt ] && NEXUS_PASSWD=$(cat /opt/config/nexus_password.txt)      || NEXUS_PASSWD=sfWU3DFVdBr7GVxB85mTYgAW
[ -f /opt/config/nexus_docker_repo.txt ] && NEXUS_DOCKER_REPO=$(cat /opt/config/nexus_docker_repo.txt) || NEXUS_DOCKER_REPO=ecomp-nexus:${PORT}

[ -f /opt/config/nexus_username.txt ] && docker login -u $NEXUS_USERNAME -p $NEXUS_PASSWD $NEXUS_DOCKER_REPO


# cleanup
echo "performing old dockers cleanup"
docker_ids=`docker ps -a | egrep "${DOCKER}|Exit" | awk '{print $1}'`
for X in ${docker_ids}
do
   docker rm -f ${X}
done

export IP=`ifconfig eth0 | awk -F: '/inet addr/ {gsub(/ .*/,"",$2); print $2}'`

echo ""

case ${DOCKER} in
    elastic | es )
		# Elastic-Search
		echo "docker run sdc-elasticsearch..."
		docker pull ecomp-nexus:${PORT}/ecomp/sdc-elasticsearch:${RELEASE}
		docker run --detach --name sdc-es --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --memory 1g --memory-swap=1g --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro -e ES_HEAP_SIZE=1024M --volume /data/ES:/usr/share/elasticsearch/data --volume /data/environments:/root/chef-solo/environments --publish 9200:9200 --publish 9300:9300 ecomp-nexus:${PORT}/ecomp/sdc-elasticsearch:${RELEASE}
		;;

	cassandra | cs )
		# cassandra
		echo "docker run sdc-cassandra..."
		docker pull ecomp-nexus:${PORT}/ecomp/sdc-cassandra:${RELEASE}
		docker run --detach --name sdc-cs --env ENVNAME="${DEP_ENV}" --env HOST_IP=${IP} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/CS:/var/lib/cassandra --volume /data/environments:/root/chef-solo/environments --publish 9042:9042 --publish 9160:9160 ecomp-nexus:${PORT}/ecomp/sdc-cassandra:${RELEASE}
		;;
		
	kibana | kbn )
		# kibana
		echo "docker run sdc-kibana..."
		docker pull ecomp-nexus:${PORT}/ecomp/sdc-kibana:${RELEASE}
		docker run --detach --name sdc-kbn --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 1g --memory-swap=1g --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/environments:/root/chef-solo/environments --publish 5601:5601 ecomp-nexus:${PORT}/ecomp/sdc-kibana:${RELEASE}
		;;
		
	backend | be )
		# Back-End
		echo "docker run sdc-backend..."
		docker pull ecomp-nexus:${PORT}/ecomp/sdc-backend:${RELEASE}
		docker run --detach --name sdc-BE --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 4g --memory-swap=4g --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/logs/BE/:/var/lib/jetty/logs  --volume /data/environments:/root/chef-solo/environments --publish 8443:8443 --publish 8080:8080 ecomp-nexus:${PORT}/ecomp/sdc-backend:${RELEASE}
		;;
		
	frontend | fe )
		# Front-End
		echo "docker run sdc-frontend..."
		docker pull ecomp-nexus:${PORT}/ecomp/sdc-frontend:${RELEASE}
		docker run --detach --name sdc-FE --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 2g --memory-swap=2g --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro  --volume /data/logs/FE/:/var/lib/jetty/logs --volume /data/environments:/root/chef-solo/environments --publish 9443:9443 --publish 8181:8181 ecomp-nexus:${PORT}/ecomp/sdc-frontend:${RELEASE}
		;;
esac

