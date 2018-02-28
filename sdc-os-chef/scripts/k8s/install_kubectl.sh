#!/bin/sh

kubectl_version=$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)

echo "[INFO] kubectl version - ${kubectl_version}"

curl_status=$(curl -w '%{http_code}\n' -LO https://storage.googleapis.com/kubernetes-release/release/${kubectl_version}/bin/linux/amd64/kubectl)

if [ $curl_status != 200 ] ; then
  echo "[ERROR] Download kubectl failed - $curl_status"
  exit -1
fi

chmod +x ./kubectl

sudo mv ./kubectl /usr/local/bin/kubectl

echo "source <(kubectl completion bash)" >> ~/.bashrc

