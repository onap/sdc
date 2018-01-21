#!/bin/sh

curl_status=$(curl -w '%{http_code}\n' https://raw.githubusercontent.com/kubernetes/helm/master/scripts/get -o get_helm.sh)

echo $curl_status

if [ ${curl_status} != 200 ]; then
  echo "[ERROR] Download get_helm failed - $curl_status"
  exit -1
fi

chmod 700 get_helm.sh

echo "[INFO] Running get helm"
./get_helm.sh

if [ $? != 0 ]; then
  echo "[ERROR] failed to run get_helm"
fi

