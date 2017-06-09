#!/bin/bash
#set -x


function usage
{
    echo "usage: docker_run.sh [ -r|--release <RELEASE-NAME> ]  [ -e|--environment <ENV-NAME> ] [ -p|--port <Docker-hub-port>] [ -h|--help ]"
}


function print_log () {
    funcname=$1
    lineno=$2
    sev=$3
    msg=$4
    dd=`date +"%Y-%m-%d %H:%M:%S"`
    case ${sev} in
       "INFO")
          COLOR="\033[1;32m"             # GREEN
          ;;
       "ERROR")
          COLOR="\033[1;31m"             # RED
          ;;
    esac
    echo -e "$dd: $fname, $funcname:$lineno --- ${COLOR} $msg\e[0m" |tee -a $LOG

}



function conf_proxy () {
   grep http_proxy Dockerfile || /bin/sed -i '/FROM/a ARG http_proxy=http://one.proxy.att.com:8080\nARG https_proxy=http://one.proxy.att.com:8080' Dockerfile
}

###################################
#########   Parameters    #########
###################################

WORKSPACE=$1
WORK_DIR=/data/sdc-os-chef
LOGFILE=`basename $0|awk -F. '{print $1".log"}'`
LOG=$WORK_DIR/$LOGFILE
DOCKER_REP=dockercentral.it.att.com:5100
ECOMP_REP=${DOCKER_REP}/com.att.sdc/openecomp
REL=`/usr/bin/xml_grep --text_only parent/version /data/sdc-os-chef/pom.xml`
DOX_VER=` /bin/grep AMDOCS /data/sdc-os-chef/versions.properties | awk '{print $2}' | awk -F"." '{print $1}'`
DOX_NUM=` /bin/grep AMDOCS /data/sdc-os-chef/versions.properties | awk '{print $2}' | awk -F"." '{print substr($NF,1,4)}' `
VERSION=` /bin/grep ASDC /data/sdc-os-chef/versions.properties | awk '{print $2}' `
MAVEN_REPO=mavencentral.it.att.com:8084/nexus/content/repositories
ONBOARD_GR=com/att/asdc/onboarding/${DOX_VER}/${DOX_NUM}


[ -f /opt/config/env_name.txt ] && DEP_ENV=$(cat /opt/config/env_name.txt) || DEP_ENV=__ENV-NAME__
[ -f /opt/config/nexus_username.txt ] && NEXUS_USERNAME=$(cat /opt/config/nexus_username.txt)    || NEXUS_USERNAME=release
[ -f /opt/config/nexus_password.txt ] && NEXUS_PASSWD=$(cat /opt/config/nexus_password.txt)      || NEXUS_PASSWD=sfWU3DFVdBr7GVxB85mTYgAW
[ -f /opt/config/nexus_docker_repo.txt ] && NEXUS_DOCKER_REPO=$(cat /opt/config/nexus_docker_repo.txt) || NEXUS_DOCKER_REPO=ecomp-nexus:${PORT}

[ -f /opt/config/nexus_username.txt ] && docker login -u $NEXUS_USERNAME -p $NEXUS_PASSWD $NEXUS_DOCKER_REPO
#docker login -u m09875@sdc.att.com -p Aa1234%^! -e mg877n@att.com dockercentral.it.att.com:5100


###################################
########       MAIN        ########
###################################
print_log Main $LINENO INFO "##### $0 completed #####"

###################################
######## sdc-elasticsearch ########
###################################
cd /data/sdc-os-chef/sdc-elasticsearch
print_log Main $LINENO INFO "start creating sdc-elasticsearch ..."
conf_proxy

docker build -t ${ECOMP_REP}/sdc-elasticsearch:${VERSION} .
docker tag  ${ECOMP_REP}/sdc-elasticsearch:${VERSION} ${ECOMP_REP}/sdc-elasticsearch
docker push ${ECOMP_REP}/sdc-elasticsearch:${VERSION}
res=$?
if [ ${res} -eq 0 ]; then
   print_log Main $LINENO INFO "${ECOMP_REP}/sdc-elasticsearch:${VERSION} pushed successfully"
else
   print_log Main $LINENO ERROR "Error pushing ${ECOMP_REP}/sdc-elasticsearch:${VERSION}"
fi


###################################
########   sdc-cassandra   ########
###################################
cd /data/sdc-os-chef/sdc-cassandra
print_log Main $LINENO INFO "start creating sdc-cassandra ..."

conf_proxy

docker build -t ${ECOMP_REP}/sdc-cassandra:${VERSION} .
docker tag  ${ECOMP_REP}/sdc-cassandra:$VERSION ${ECOMP_REP}/sdc-cassandra
docker push ${ECOMP_REP}/sdc-cassandra:$VERSION
res=$?
if [ ${res} -eq 0 ]; then
   print_log Main $LINENO INFO "${ECOMP_REP}/sdc-cassandra:${VERSION} pushed successfully"
else
   print_log Main $LINENO ERROR "Error pushing ${ECOMP_REP}/sdc-cassandra:${VERSION}"
fi



###################################
########    sdc-kibana     ########
###################################
cd /data/sdc-os-chef/sdc-kibana
print_log Main $LINENO INFO "start creating sdc-kibana ..."

conf_proxy

docker build -t ${ECOMP_REP}/sdc-kibana:$VERSION .
docker tag  ${ECOMP_REP}/sdc-kibana:$VERSION   ${ECOMP_REP}/sdc-kibana
docker push ${ECOMP_REP}/sdc-kibana:$VERSION
res=$?
if [ ${res} -eq 0 ]; then
   print_log Main $LINENO INFO "${ECOMP_REP}/sdc-kibana:${VERSION} pushed successfully"
else
   print_log Main $LINENO ERROR "Error pushing ${ECOMP_REP}/sdc-kibana:${VERSION}"
fi


###################################
########    sdc-sanity     ########
###################################
cd /data/sdc-os-chef/sdc-sanity
print_log Main $LINENO INFO "start creating sdc-sanity ..."

conf_proxy

docker build -t ${ECOMP_REP}/sdc-sanity:$VERSION .
docker tag  ${ECOMP_REP}/sdc-sanity:$VERSION   ${ECOMP_REP}/sdc-sanity
docker push ${ECOMP_REP}/sdc-sanity:$VERSION
res=$?
if [ ${res} -eq 0 ]; then
   print_log Main $LINENO INFO "${ECOMP_REP}/sdc-sanity:${VERSION} pushed successfully"
else
   print_log Main $LINENO ERROR "Error pushing ${ECOMP_REP}/sdc-sanity:${VERSION}"
fi


###################################
########   sdc-backend     ########
###################################
cd /data/sdc-os-chef/sdc-backend
print_log Main $LINENO INFO "start creating sdc-backend ..."

conf_proxy
/bin/sed -i "s/__SDC-RELEASE__/${REL}/g" Dockerfile

wget -q -nd -r --no-parent -A 'onboard-main*.tar' http://${MAVEN_REPO}/att-repository-snapshots/${ONBOARD_GR}/onboard-main
if [ $? -ne 0 ] ; then
   wget -q -nd -r --no-parent -A 'onboard-main*.tar' http://${MAVEN_REPO}/att-repository-releases/${ONBOARD_GR}/onboard-main
fi
/bin/tar -xf onboard-main-${DOX_VER}.*.tar --wildcards --no-anchored "onboarding-be-${DOX_VER}.*.war"
rm onboard-main*.tar

docker build -t ${ECOMP_REP}/sdc-backend:$VERSION .
docker tag  ${ECOMP_REP}/sdc-backend:$VERSION   ${ECOMP_REP}/sdc-backend
docker push ${ECOMP_REP}/sdc-backend:$VERSION
res=$?
if [ ${res} -eq 0 ]; then
   print_log Main $LINENO INFO "${ECOMP_REP}/sdc-backend:${VERSION} pushed successfully"
else
   print_log Main $LINENO ERROR "Error pushing ${ECOMP_REP}/sdc-backend:${VERSION}"
fi


###################################
########   sdc-frontend    ########
###################################
cd /data/sdc-os-chef/sdc-frontend
print_log Main $LINENO INFO "start creating sdc-frontend ..."

conf_proxy

/bin/sed -i "s/__SDC-RELEASE__/${REL}/g" Dockerfile
wget -q -nd -r --no-parent -A 'onboard-main*.tar' http://${MAVEN_REPO}/att-repository-snapshots/${ONBOARD_GR}/onboard-main
if [ $? -ne 0 ] ; then
   wget -q -nd -r --no-parent -A 'onboard-main*.tar' http://${MAVEN_REPO}/att-repository-releases/${ONBOARD_GR}/onboard-main
fi
/bin/tar -xf onboard-main-${DOX_VER}.*.tar --wildcards --no-anchored "onboarding-fe-${DOX_VER}.*.war"
rm onboard-main*.tar

docker build -t ${ECOMP_REP}/sdc-frontend:$VERSION .
docker tag  ${ECOMP_REP}/sdc-frontend:$VERSION   ${ECOMP_REP}/sdc-frontend
docker push ${ECOMP_REP}/sdc-frontend:$VERSION
res=$?
if [ ${res} -eq 0 ]; then
   print_log Main $LINENO INFO "${ECOMP_REP}/sdc-frontend:${VERSION} pushed successfully"
else
   print_log Main $LINENO ERROR "Error pushing ${ECOMP_REP}/sdc-frontend:${VERSION}"
fi

print_log Main $LINENO INFO "##### $0 completed #####"
