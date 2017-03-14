#!/bin/bash

curl localhost:9200/_cluster/health?pretty=true

echo "BE health-Check:"
curl http://localhost:8080/sdc2/rest/healthCheck

echo ""
echo ""
echo "FE health-Check:"
curl http://localhost:8181/sdc1/rest/healthCheck


echo ""
echo ""
res=`curl -s -X GET -H "Accept: application/json" -H "Content-Type: application/json" -H "USER_ID: jh0003" "http://localhost:8080/sdc2/rest/v1/user/demo" | wc -l`
if [[ ! ${res} -eq 0 ]]
then
    echo "Error [${res}] while user existance check"
    exit ${res}
fi
echo "check user existance: OK"

