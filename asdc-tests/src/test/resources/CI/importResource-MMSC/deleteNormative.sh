#!/bin/bash

function usage {
	echo "Usage: $0 <hostIp> <hostPort> <resourceName>"
}

function deleteResource() {

	ELEMENT_NAME=$1
	echo -e "############### Removing Element ${ELEMENT_NAME} Start ######################"
	http_code=$(curl -s -o /dev/null -w "%{http_code}" -X "DELETE" -H "USER_ID: jh0003" ${HOST_IP}:${HOST_PORT}/sdc2/rest/v1/catalog/resources/res_${ELEMENT_NAME}".1.0")
	if [ ${http_code} -eq 204  ]; then
		echo -e "\n############### Removing Element ${ELEMENT_NAME} status code:${http_code} End #######\n\n\n"
	elif [ ${http_code} -eq 500 ]; then
		echo -e "\n############### Failed to remove Element ${ELEMENT_NAME} status code:${http_code} End #######\n\n\n"
		exit 1
	elif [ ${http_code} -eq 404 ]; then
		echo -e "\n############### Element ${ELEMENT_NAME} not found status code:${http_code} End #######\n\n\n"
	else
		echo -e "\n############### Failed to remove Element ${ELEMENT_NAME} status code:${http_code} End #######\n\n\n"
		exit 1
	fi
}
if [ $# -lt 3 ]
then
	usage
	exit 2
fi

HOST_IP=$1
HOST_PORT=$2

deleteResource $3 

exit 0
