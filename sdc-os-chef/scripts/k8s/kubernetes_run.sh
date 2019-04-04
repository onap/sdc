#!/bin/bash

####################
#    Functions     #
####################

status()
{
  local rc=$1
  if [ ${rc} != 0 ]; then
    echo "[ERR] Failure detected. Aborting !"
    exit 255
  else
    echo "[INFO] Done "
  fi
}

print_header()
{
  header=$*
  echo ""
  echo "-------------------------"
  echo "   ${header}"
  echo "-------------------------"
  echo ""
 }

####################
#      Main        #
####################
clear

####################
# kubectl          #
####################
print_header "Kubelet - Install ..."
sh ./install_kubectl.sh
status $?


####################
# minikube         #
####################
print_header "Minikube - Install ..."
sh ./install_minikube.sh
status $?


####################
# dependencies     #
####################
print_header "Dependency - Install ..."
echo "[INFO]   Install - nsenter"
# Use pre compiled nsenter:
if [ -f bin_nsenter ]; then
    sudo cp bin_nsenter /usr/local/bin/nsenter
else
  echo "[ERROR] File [bin_nsenter] is missing"
fi

if [ -f etc_nsenter ]; then
    sudo cp etc_nsenter /etc/bash_completion.d/nsenter
else
  echo "[ERROR] File [etc_nsenter] is missing"
fi


## In order to build the nsenter use the below instructions:
##./build_nsenter_exec.sh
echo "[INFO]   Install - socat"
sudo apt-get install -y socat jq

####################
# helm             #
####################
print_header "Helm - Install ..."
sh ./install_helm.sh
status $? "$action"


####################
# K8s              #
####################
print_header "SDC - Deploy Pods ..."
sh ./deploy_k8s_sdc.sh
status $?


####################
# SDC LOGS Tree    #
####################
print_header "SDC - Deploy Pods ..."
sudo mkdir -p /dockerdata-nfs/onap/sdc/logs//BE/SDC/SDC-BE
sudo mkdir -p /dockerdata-nfs/onap/sdc/logs/FE/SDC/SDC-FE
sudo mkdir -p /dockerdata-nfs/onap/sdc/logs/ONBOARD/SDC/ONBOARD-BE
sudo mkdir -p /dockerdata-nfs/onap/sdc/cert
sudo chmod -R 777 /dockerdata-nfs/onap/sdc/logs/*
