#!/bin/sh

echo "[INFO] minikube version - v0.24.1"

curl_status=$(curl -w '%{http_code}\n' -Lo minikube https://storage.googleapis.com/minikube/releases/v0.24.1/minikube-linux-amd64)

if [ $curl_status != 200 ] ; then
  echo "[ERROR] Download minikube failed - $curl_status"
  exit -1
fi

chmod +x minikube

sudo mv minikube /usr/local/bin/

export CHANGE_MINIKUBE_NONE_USER=true

sudo minikube start --vm-driver=none
