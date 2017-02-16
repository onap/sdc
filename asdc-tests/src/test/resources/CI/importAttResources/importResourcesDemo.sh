#!/bin/bash

function usage {
	echo "Usage: $0 <hostIp> <hostPort>"
}

function addResource() {
	
	ELEMENT_NAME=$1
	echo "###################### Adding Element ${ELEMENT_NAME} Start ######################"
	CURRENT_ZIP_FILE=./${ELEMENT_NAME}/${ELEMENT_NAME}.zip
	CURRENT_JSON_FILE=./${ELEMENT_NAME}/${ELEMENT_NAME}.json
	JSON_CONTENT=`paste -s ${CURRENT_JSON_FILE}`
	curl -v -F resourceMetadata="${JSON_CONTENT}" -F resourceZip=@${CURRENT_ZIP_FILE} -H "USER_ID: jh0003" ${HOST_IP}:${HOST_PORT}/sdc2/rest/v1/catalog/upload/multipart
	echo ""
	echo "###################### Adding Element ${ELEMENT_NAME} End ########################"
	echo ""
	echo ""
	echo ""
}
if [ $# -lt 2 ]
then
	usage
	exit 2
fi

HOST_IP=$1
HOST_PORT=$2

addResource "root"
addResource "compute"
addResource "softwareComponent"
addResource "loadBalancer"
addResource "HSS"
addResource "ICSCF"
addResource "PCSCF"
addResource "SCSCF"


