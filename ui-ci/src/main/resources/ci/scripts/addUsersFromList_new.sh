#!/bin/bash

function help_usage ()
{
	echo "$0 -ip <ip> -f <userToAdd_file>"
	echo "for example: -ip 127.0.0.1 -f /var/tmp/userToAdd.txt"
	exit
}

function check_file_existance ()
{
echo "check_file_existance $1"
if [ $1 == "" ]; then
        echo "Please provide full path to user file"
        exit;
elif [ -f $1 ]; then
        source $1
	USERS=("${USER_LIST[@]}")
        echo "file exist" $1
else
        echo "Provided user file does not exist"
        exit
fi
}

function check_ip_existance ()
{
if [ $1 == "" ]; then
        echo "Please provide ip address"
        exit;
fi
}

function addUser ()
{
	#for user in "${USER_LIST[@]}"; do
	for user in "${USERS[@]}"; do
		PING=`ping -c 1 $IP  > /var/tmp/ping.log`
		pattern1='100% packet loss'
		pattern2='Host Unreachable'
		COUNT=`egrep -c "$pattern1|$pattern2" /var/tmp/ping.log`
		if [ $COUNT -eq 0 ]; then
		#	curl -i -X post -d '{ "userId" : "kk1123", "role" : "ADMIN" }'  -H "Content-Type: application/json" -H "USER_ID: jh0003" http://192.168.111.9:8080/sdc2/rest/v1/user
			userId=`echo $user|awk '{print $1}'`
			role=`echo $user|awk '{print $2}'`
            firstName=`echo $user|awk '{print $3}'`
            lastName=`echo $user|awk '{print $4}'`
            email=`echo $user|awk '{print $5}'`
			curl -i -X post -d '{ "userId" : "'${userId}'", "role" : "'${role}'", "firstName" : "'${firstName}'", "lastName" : "'${lastName}'", "email" : "'${email}'" }'  -H "Content-Type: application/json" -H "USER_ID: jh0003" http://${IP}:8080/sdc2/rest/v1/user
		else
			echo "Host" $IP "Is Unreachable"
		fi
	done
curl -i -X post -d '{"consumerName": "ci","consumerSalt": "2a1f887d607d4515d4066fe0f5452a50","consumerPassword": "0a0dc557c3bf594b1a48030e3e99227580168b21f44e285c69740b8d5b13e33b"}'  -H "Content-Type: application/json" -H "USER_ID: jh0003" -H "Authorization:Basic Y2k6MTIzNDU2" http://${IP}:8080/sdc2/rest/v1/consumers
	
}

#main
[ $# -eq 0 ] && help_usage
while [ $# -ne 0 ]; do
	case $1 in
		"-f")
			USER_FILE=$2
			shift 1
			shift 1
		;;	
		-ip)
			IP=$2
			shift 1
			shift 1
		;;
		*)
#			help_usage
		;;
	esac
done

check_file_existance $USER_FILE
check_ip_existance $IP
addUser
