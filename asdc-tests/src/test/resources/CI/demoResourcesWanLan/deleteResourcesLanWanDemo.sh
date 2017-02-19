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


deleteResource "org.openecomp.asdc.nodes.Connector.LAN_Connector"
deleteResource "org.openecomp.asdc.nodes.Connector.WAN_Connector"
deleteResource "org.openecomp.asdc.nodes.Connector"
deleteResource "org.openecomp.asdc.nodes.VNF"
deleteResource "org.openecomp.asdc.nodes.VNF_Container"
deleteResource "org.openecomp.asdc.nodes.Router"
deleteResource "org.openecomp.asdc.nodes.Root"


