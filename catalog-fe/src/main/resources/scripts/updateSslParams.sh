#!/bin/sh

function usage() {
        echo "$0 <working dir>"
}

function exitOnError() {
        if [ $1 -ne 0 ]
        then
                echo "Failed running task $2"
                exit 2
        fi
}

if [ $# -ne 1 ]
then
        usage
	if [ ${#OLDPWD} -ne 0 ]
	then
		cd -
	fi
        exit 1

fi

WORK_DIR=$1

cd $WORK_DIR

sed -i 's/\(^https.port=\)\(.*\)/\1443/g' start.d/https.ini
exitOnError $? "update_port_in_https_ini"

cd -
