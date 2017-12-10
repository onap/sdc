#!/bin/bash


function usage {
    echo "usage: docker_run.sh [ -r|--release <RELEASE-NAME> ]  [ -e|--environment <ENV-NAME> ] [ -p|--port <Docker-hub-port>] [ -l|--local <Run-without-pull>] [ -s|--skipTests <Run-without-sanityDocker>] [ -h|--help ]"
}


function cleanup {
	echo "performing old dockers cleanup"
	docker_ids=`docker ps -a | egrep -v "openecomp/sdc-simulator" | egrep "ecomp-nexus:${PORT}/sdc|sdc|Exit" | awk '{print $1}'`
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
RUNTESTS=false
DEBUG_PORT="--publish 4000:4000"

while [ $# -gt 0 ]
do
    case $1 in
    -r | --release )
          shift 1 ;
          RELEASE=$1;
          shift 1;;
    -e | --environment )
          shift 1;
          DEP_ENV=$1;
          shift 1 ;;
    -p | --port )
          shift 1 ;
          PORT=$1;
          shift 1 ;;
    -l | --local )
          LOCAL=true;
          shift 1;;
    -t | --runTests )
          RUNTESTS=true;
          shift 1 ;;
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


export IP=`ifconfig eth0 | awk -F: '/inet addr/ {gsub(/ .*/,"",$2); print $2}'`
export PREFIX=${NEXUS_DOCKER_REPO}'/openecomp'

if [ ${LOCAL} = true ]; then
	PREFIX='openecomp'
fi

echo ""

# Elastic-Search
echo "docker run sdc-elasticsearch..."
if [ ${LOCAL} = false ]; then
	echo "pulling code"
	docker pull ${PREFIX}/sdc-elasticsearch:${RELEASE}
fi
docker run --detach --name sdc-es --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --memory 750m --memory-swap=750m -e ES_JAVA_OPTS="-Xms512m -Xmx512m" --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro -e ES_HEAP_SIZE=1024M --volume /data/ES:/usr/share/elasticsearch/data --volume /data/environments:/root/chef-solo/environments --publish 9200:9200 --publish 9300:9300 ${PREFIX}/sdc-elasticsearch:${RELEASE}


# cassandra
echo "docker run sdc-cassandra..."
if [ ${LOCAL} = false ]; then
	docker pull ${PREFIX}/sdc-cassandra:${RELEASE}
fi
docker run --detach --name sdc-cs --env RELEASE="${RELEASE}" --env ENVNAME="${DEP_ENV}" --env HOST_IP=${IP} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/CS:/var/lib/cassandra --volume /data/environments:/root/chef-solo/environments --publish 9042:9042 --publish 9160:9160 ${PREFIX}/sdc-cassandra:${RELEASE}


echo "please wait while CS is starting..."
monitor_docker sdc-cs
#echo ""
#c=120 # seconds to wait
#REWRITE="\e[25D\e[1A\e[K"
#while [ $c -gt 0 ]; do
#    c=$((c-1))
#    sleep 1
#    echo -e "${REWRITE}$c"
#done
#echo -e "

# kibana
echo "docker run sdc-kibana..."
if [ ${LOCAL} = false ]; then
	docker pull ${PREFIX}/sdc-kibana:${RELEASE}
fi
docker run --detach --name sdc-kbn --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 2g --memory-swap=2g --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/environments:/root/chef-solo/environments --publish 5601:5601 ${PREFIX}/sdc-kibana:${RELEASE}

dir_perms

# Back-End
echo "docker run sdc-backend..."
if [ ${LOCAL} = false ]; then
	docker pull ${PREFIX}/sdc-backend:${RELEASE}
else
	ADDITIONAL_ARGUMENTS=${DEBUG_PORT}
fi
docker run --detach --name sdc-BE --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env http_proxy=${http_proxy} --env https_proxy=${https_proxy} --env no_proxy=${no_proxy} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 4g --memory-swap=4g --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/logs/BE/:/var/lib/jetty/logs  --volume /data/environments:/root/chef-solo/environments --publish 8443:8443 --publish 8080:8080 ${ADDITIONAL_ARGUMENTS} ${PREFIX}/sdc-backend:${RELEASE}

echo "please wait while BE is starting..."
monitor_docker sdc-BE
#echo ""
#c=45 # seconds to wait
#REWRITE="\e[45D\e[1A\e[K"
#while [ $c -gt 0 ]; do
#    c=$((c-1))
#    sleep 1
#    echo -e "${REWRITE}$c"
#done
#echo -e ""




# Front-End
echo "docker run sdc-frontend..."
if [ ${LOCAL} = false ]; then
	docker pull ${PREFIX}/sdc-frontend:${RELEASE}
fi
docker run --detach --name sdc-FE --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env http_proxy=${http_proxy} --env https_proxy=${https_proxy} --env no_proxy=${no_proxy} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro  --volume /data/logs/FE/:/var/lib/jetty/logs --volume /data/environments:/root/chef-solo/environments --publish 9443:9443 --publish 8181:8181 ${PREFIX}/sdc-frontend:${RELEASE}

echo "docker run sdc-frontend..."
monitor_docker sdc-FE




# running healthCheck scripts
echo "Running health checks, please wait..."
echo ""
c=30 # seconds to wait
REWRITE="\e[45D\e[1A\e[K"
while [ $c -gt 0 ]; do
    c=$((c-1))
    sleep 1
    echo -e "${REWRITE}$c"
done
echo -e ""

/data/scripts/docker_health.sh

#if [ $? -ne 0 ]; then
#    exit 1
#fi

# sanityDocker
echo "docker run sdc-sanity..."
if [ ${RUNTESTS} = true ]; then
echo "Triger sanity docker, please wait..."
    if [ ${LOCAL} = false ]; then
	   docker pull ${PREFIX}/sdc-sanity:${RELEASE}
    fi

docker run --detach --name sdc-sanity --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --env http_proxy=${http_proxy} --env https_proxy=${https_proxy} --env no_proxy=${no_proxy} --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 1500m --memory-swap=1500m --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/logs/sdc-sanity/target:/var/lib/tests/target --volume /data/logs/sdc-sanity/ExtentReport:/var/lib/tests/ExtentReport --volume /data/logs/sdc-sanity/outputCsar:/var/lib/tests/outputCsar --volume /data/environments:/root/chef-solo/environments --publish 9560:9560 ${PREFIX}/sdc-sanity:${RELEASE}
fi
