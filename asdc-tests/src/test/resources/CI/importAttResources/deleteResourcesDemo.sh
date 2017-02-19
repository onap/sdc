#!/bin/bash

function usage {
	echo "Usage: $0 <hostIp> <hostPort>"
}

function deleteResource() {
	
	ELEMENT_NAME=$1
	echo "###################### Removing Element ${ELEMENT_NAME} Start ######################"
	curl -X "DELETE" -H "USER_ID: jh0003" ${HOST_IP}:${HOST_PORT}/sdc2/rest/v1/catalog/resources/res_${ELEMENT_NAME}".1.0"
	echo ""
	echo "###################### Removing Element ${ELEMENT_NAME} End ########################"
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

deleteResource "tosca.nodes.root"
deleteResource "tosca.nodes.compute"
deleteResource "tosca.nodes.softwarecomponent"
deleteResource "tosca.nodes.loadbalancer"
deleteResource "att.nodes.ims.hss"
deleteResource "att.nodes.ims.icscf"
deleteResource "att.nodes.ims.pcscf"
deleteResource "att.nodes.ims.scscf"


