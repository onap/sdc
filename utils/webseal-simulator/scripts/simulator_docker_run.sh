#!/bin/bash

export IP=`ifconfig eth0 | awk -F: '/inet addr/ {gsub(/ .*/,"",$2); print $2}'`
export PREFIX=${NEXUS_DOCKER_REPO}'/onap'

PREFIX='onap'
RELEASE=latest
LOCAL=true
[ -f /opt/config/env_name.txt ] && DEP_ENV=$(cat /opt/config/env_name.txt) || DEP_ENV=__ENV-NAME__
[ -f /opt/config/nexus_username.txt ] && NEXUS_USERNAME=$(cat /opt/config/nexus_username.txt)    || NEXUS_USERNAME=release
[ -f /opt/config/nexus_password.txt ] && NEXUS_PASSWD=$(cat /opt/config/nexus_password.txt)      || NEXUS_PASSWD=sfWU3DFVdBr7GVxB85mTYgAW
[ -f /opt/config/nexus_docker_repo.txt ] && NEXUS_DOCKER_REPO=$(cat /opt/config/nexus_docker_repo.txt) || NEXUS_DOCKER_REPO=ecomp-nexus:${PORT}

function usage {
    cat <<EOF
usage: simulator_docker_run.sh [-e <ENV-NAME>] [-r <RELEASE-NAME>] [-p <PORT>] [-h]
Optional arguments:
    -e, --environment
        Environment Name
    -r, --release
        Release Name
    -p, --port
        Docker Hub port number
    -h, --help
        Help
EOF
}

function cleanup {
    echo "performing old dockers cleanup"
    for old_container in $(docker ps -a | egrep "onap/sdc-simulator|Exit" | awk '{print $1}'); do
       docker rm -f ${old_container}
    done
}

function dir_perms {
    mkdir -p /data/logs/WS/
    chmod -R 777 /data/logs
}

while getopts "r:e:p:" OPTION "${@}"; do
    case $OPTION in
        -r | --release)
            shift
            RELEASE=${1}
            ;;
        -e | --environment)
            shift
            DEP_ENV=${1}
            ;;
        -p | --port)
            shift
            PORT=${1}
            ;;
        -h | --help)
            usage
            exit
            ;;
        *)
            usage
            exit 1
    esac
    shift
done

[ -f /opt/config/nexus_username.txt ] && docker login -u $NEXUS_USERNAME -p $NEXUS_PASSWD $NEXUS_DOCKER_REPO

cleanup

echo "\n${PREFIX}"

dir_perms

# SDC-Simulator
docker run --detach --name sdc-sim \
    --env HOST_IP=${IP} \
    --env ENVNAME="${DEP_ENV}" \
    --env http_proxy=${http_proxy} \
    --env https_proxy=${https_proxy} \
    --env no_proxy=${no_proxy} \
    --log-driver=json-file \
    --log-opt max-size=100m \
    --log-opt max-file=10 \
    --ulimit memlock=-1:-1 \
    --memory 256m \
    --memory-swap=256m \
    --ulimit nofile=4096:100000 \
    --volume /etc/localtime:/etc/localtime:ro \
    --volume /data/logs/WS/:/var/lib/jetty/logs \
    --volume /data/environments:/root/chef-solo/environments \
    --publish 8285:8080 \
        ${PREFIX}/sdc-simulator:${RELEASE}


if [ $? -ne 0 ]; then
    exit 1
fi
