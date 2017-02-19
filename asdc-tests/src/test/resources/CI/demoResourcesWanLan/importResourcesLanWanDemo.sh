#!/bin/bash

function usage {
	echo "Usage: $0 <hostIp> <hostPort>"
}

function addResource() {
	
	ELEMENT_NAME=$1
	echo -e "###################### Adding Element ${ELEMENT_NAME} Start ######################"
	CURRENT_ZIP_FILE=./${ELEMENT_NAME}/${ELEMENT_NAME}.zip
	CURRENT_JSON_FILE=./${ELEMENT_NAME}/${ELEMENT_NAME}.json
	JSON_CONTENT=`paste -s ${CURRENT_JSON_FILE}`
	curl -v -F resourceMetadata="${JSON_CONTENT}" -F resourceZip=@${CURRENT_ZIP_FILE} -H "USER_ID: jh0003" ${HOST_IP}:${HOST_PORT}/sdc2/rest/v1/catalog/upload/multipart
	echo  ""
	echo -e "###################### Adding Element ${ELEMENT_NAME} End ########################"

}
if [ $# -lt 2 ]
then
	usage
	exit 2
fi

HOST_IP=$1
HOST_PORT=$2

#Add The CapabilityTypes
http_code=$(curl -s -o /dev/null -w "%{http_code}" -v -F capabilityTypeZip=@capabilityTypesWanLan.zip -H "USER_ID: jh0003" ${HOST_IP}:${HOST_PORT}/sdc2/rest/v1/catalog/uploadType/capability)
if [ ${http_code} -eq 201  ]; then
	echo -e "\n###################### Adding The CapabilityTypes status code:${http_code} End ########################\n\n\n"
elif [ ${http_code} -eq 500 ]; then
	echo -e "\n###################### Failed to add CapabilityTypes status code:${http_code} End ########################\n\n\n"
	exit 1
else
	echo -e "\n###################### Failed to add CapabilityTypes status code:${http_code} End ########################\n\n\n"
	exit 1
fi

addResource "root"
addResource "router"
addResource "VNF_Container"
addResource "VNF"
addResource "connector"
addResource "WAN_Connector"
addResource "LAN_Connector"



