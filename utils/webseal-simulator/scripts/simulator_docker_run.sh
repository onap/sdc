#!/bin/sh

export PREFIX=${NEXUS_DOCKER_REPO}'/onap'
PREFIX='onap'
RELEASE=latest
LOCAL=true
JAVA_OPTIONS=" -Xmx128m -Xms128m -Xss1m"

[ -f ${WORKSPACE}/opt/config/env_name.txt ] && DEP_ENV=$(cat ${WORKSPACE}/opt/config/env_name.txt)
[ -f ${WORKSPACE}/opt/config/nexus_username.txt ] && NEXUS_USERNAME=$(cat ${WORKSPACE}/opt/config/nexus_username.txt)    || NEXUS_USERNAME=release
[ -f ${WORKSPACE}/opt/config/nexus_password.txt ] && NEXUS_PASSWD=$(cat ${WORKSPACE}/opt/config/nexus_password.txt)      || NEXUS_PASSWD=sfWU3DFVdBr7GVxB85mTYgAW
[ -f ${WORKSPACE}/opt/config/nexus_docker_repo.txt ] && NEXUS_DOCKER_REPO=$(cat ${WORKSPACE}/opt/config/nexus_docker_repo.txt)

function usage {
    cat <<EOF
usage: simulator_docker_run.sh [-e <ENV-NAME>] [-r <RELEASE-NAME>] [-u <FE-URL>][-h]
Optional arguments:
    -e, --environment
        Environment Name
    -r, --release
        Release Name
    -u, --fe_url
        frontend server url
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
    mkdir -p ${WORKSPACE}/data/logs/WS/
    chmod -R 777 ${WORKSPACE}/data/logs
}

while getopts "r:e:u:-:" OPTION "${@}"; do
    case $OPTION in
        r)
            shift
            RELEASE=${1}
            ;;
        e)
            shift
            DEP_ENV=${1}
            ;;
        u)
            shift
            FE_URL=${1}
            ;;
        -) # Workaround to support long option names
            case "${OPTARG}" in
                release)
                    shift
                    RELEASE=${1}
                    ;;
                environment)
                    shift
                    DEP_ENV=${1}
                    ;;
                fe_url)
                    shift
                    FE_URL=${1}
                    ;;
            esac
            ;;
        ?)
            usage
            exit
            ;;
        *)
            usage
            exit 1
    esac
    shift
done

[ -f ${WORKSPACE}/opt/config/nexus_username.txt ] && docker login -u $NEXUS_USERNAME -p $NEXUS_PASSWD $NEXUS_DOCKER_REPO

cleanup


dir_perms
echo ${FE_URL}
# SDC-Simulator
docker run --detach --name sdc-sim \
    --env FE_URL="${FE_URL}" \
    --env JAVA_OPTIONS="${JAVA_OPTIONS}" \
    --env http_proxy=${http_proxy} \
    --env https_proxy=${https_proxy} \
    --env no_proxy=${no_proxy} \
    --volume /etc/localtime:/etc/localtime:ro \
    --volume ${WORKSPACE}/data/logs/WS/:/var/lib/jetty/logs \
    --publish 8285:8080 \
    --publish 8286:8443 ${PREFIX}/sdc-simulator:${RELEASE}

if [ $? -ne 0 ]; then
    exit 1
fi
