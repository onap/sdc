#!/bin/sh 

cass_user=asdc_user
cass_pass='Aa1234%^!'
sdcReleaseNum=$1 
conformanceLevel=$2

CQLSH="/home/vagrant/cassandra/apache-cassandra-2.1.9/bin/cqlsh"


 ### Get payload
 
 select_payload="select payload from sdcartifact.sdcschemafiles where conformanceLevel='$conformanceLevel' and sdcReleaseNum='$sdcReleaseNum'"
 
 echo "Run: [$select_payload]"

$CQLSH -e "$select_payload" |grep "0x" |xxd -r -p > SDC-downloaded.zip

res=$?
echo "After select payload, res=[$res]"