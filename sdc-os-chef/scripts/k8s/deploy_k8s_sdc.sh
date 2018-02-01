#!/bin/sh
set -x

check_status()
{
  local rc=$1
  shift
  local comment="$@"
  if [ ${rc} != 0 ]; then
    echo "[ERR] Failure detected - ${comment}.  Aborting !"
    exit 255
  fi
}


# Should be removed while private dockers (maven build) will be available:
echo "[INFO] ONAP Docker login"
sudo docker login -u docker -p docker nexus3.onap.org:10001
check_status $? "Onap docker registry login"

# Verify the kube-system pods are running:
# kube-addon-manager, kube-dns, kubernetes-dashboard, storage-provisioner, tiller-deploy
echo "[INFO] Wait for Kubernetes Service ..." 
cd ../../kubernetes
status=0
while [ ${status} -ne 5 ]
do 
  status=$(sudo kubectl get pods --namespace kube-system -o json \
  | jq -r '
    .items[]
    | select(.status.phase == "Running" and 
    ([ .status.conditions[] | select(.type == "Ready" and .status == "True") ]
    | length ) == 1 )
    | .metadata.namespace + "/" + .metadata.name
    ' \
  |  wc -l )
  sleep 3
done

# Create namespace 
echo "[INFO] Check Namespace existence"
exist_namespace=$( sudo kubectl get namespaces  | grep onap-sdc | grep Active | wc -l )
if [ ${exist_namespace} -eq 0 ]; then
  sudo kubectl create namespace onap-sdc
  check_status $? "Create namespace"
fi

echo "[INFO] Running helm init"
sudo helm init
check_status $? "Helm init"

set -x

printf "[INFO] Wait for helm to get ready\n"
helm_health=1
while [ ${helm_health} -ne 0 ]
do 
  sudo helm version | grep "Server" >/dev/null 2>&1
  helm_health=$?
  sleep 5
done

# Remove previous chart
exist_chart=$( sudo helm ls onap-sdc -q | wc -l )
if [ ${exist_chart} -ne 0 ];then
  echo "[INFO] Delete the existing onap-sdc chart"
  sudo helm del --purge onap-sdc
  check_status $? "Delete chart"
fi

# Install updated chart
echo "[INFO] Create onap-sdc deployment"
sudo helm install sdc --name onap-sdc
check_status $? "Install chart"
