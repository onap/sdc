#!/bin/bash

if [ $# -eq 0 ]; then
   echo "No arguments supplied"
   exit 1
fi


RELEASE=$1
DEP_ENV=$2


docker_ids=`docker ps -a | egrep "Exit" | awk '{print $1}'`
for X in ${docker_ids}
do
   docker rm -f ${X}
done

export IP=`ifconfig eth0 | awk -F: '/inet addr/ {gsub(/ .*/,"",$2); print $2}'`

echo ""

# Back-End
my_cnt=`docker ps|egrep -c "sdc-backend:${RELEASE}"`
if [ "${my_cnt}" -eq "1" ]; then
#    echo "`date` - BE is running" >> /data/ASDC/logs/watchdog.log
    echo "`date` - BE is running" > /dev/null
else
    echo "`date` - BE was down" >> /data/ASDC/logs/watchdog.log
    docker pull ecomp-nexus:51212/ecomp/sdc-backend:${RELEASE}
    docker run --detach --name sdc-BE --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 3g --memory-swap=3g --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro --volume /data/ASDC/logs/BE/:/var/lib/jetty/logs  --volume /data/ASDC/environments:/root/chef-solo/environments --publish 8443:8443 --publish 8080:8080 ecomp-nexus:51212/ecomp/sdc-backend:${RELEASE}
fi

# Front-End
my_cnt=`docker ps|egrep -c "sdc-frontend:${RELEASE}"`
if [ "${my_cnt}" -eq "1" ]; then
#    echo "`date` - FE is running" >> /data/ASDC/logs/watchdog.log
    echo "`date` - FE is running" >> /dev/null
else
    echo "`date` - FE was down" >> /data/ASDC/logs/watchdog.log
    docker pull ecomp-nexus:51212/ecomp/sdc-frontend:${RELEASE}
	docker run --detach --name sdc-FE --env HOST_IP=${IP} --env ENVNAME="${DEP_ENV}" --log-driver=json-file --log-opt max-size=100m --log-opt max-file=10 --ulimit memlock=-1:-1 --memory 2g --memory-swap=2g --ulimit nofile=4096:100000 --volume /etc/localtime:/etc/localtime:ro  --volume /data/ASDC/logs/FE/:/var/lib/jetty/logs --volume /data/ASDC/environments:/root/chef-solo/environments --publish 9443:9443 --publish 8181:8181 ecomp-nexus:51212/ecomp/sdc-frontend:${RELEASE}
fi

