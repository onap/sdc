#!/bin/bash

REPORT_NAME=$1
VERSION=$2
ENV=$3
IP=$4

if [ -z "$REPORT_NAME" ]
 then
        source ExtentReport/versions.info
        now=$(date -d '+7 hour' "+%Y-%m-%d_%H_%M")
        REPORT_NAME="${now}"
        VERSION="${osVersion}"
                if [[ $env == *"DEV20"* ]]
                        then
                                ENV="Nightly"
                        else
                                ENV=""
                        fi

 fi

/usr/bin/expect  << EOF
spawn ssh admin@${IP} mkdir -p -m 775 /home/admin/reports/${ENV}/${VERSION}/APIs/

expect {
  -re ".*es.*o.*" {
    exp_send "yes\r"
    exp_continue
  }
  -re ".*sword.*" {
    exp_send "Aa123456\r"
  }
}

expect eof

spawn scp -r ExtentReport admin@${IP}:/home/admin/reports/${ENV}/${VERSION}/APIs/${REPORT_NAME}/

expect {
  -re ".*es.*o.*" {
    exp_send "yes\r"
    exp_continue
  }
  -re ".*sword.*" {
    exp_send "Aa123456\r"
  }
}

expect eof

spawn ssh admin@${IP} chmod -R 775 /home/admin/reports/${ENV}/${VERSION}/APIs/${REPORT_NAME}/

expect {
  -re ".*es.*o.*" {
    exp_send "yes\r"
    exp_continue
  }
  -re ".*sword.*" {
    exp_send "Aa123456\r"
  }
}

expect eof

EOF
