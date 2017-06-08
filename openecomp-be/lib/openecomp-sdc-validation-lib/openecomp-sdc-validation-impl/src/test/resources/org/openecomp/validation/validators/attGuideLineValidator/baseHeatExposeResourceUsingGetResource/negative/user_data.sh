#!/bin/bash

set -x
exec >> /root/user_data.out
exec 2>&1

MOUNT_POINT=${1:-'/opt/app/virc'}
LABEL=${2:-'VIRC_DATA'}
DESCRIPTION=${3:-'vIRC data volume'}

TAG=VIRC_PROVISIONING

DISK_ID=$(ls -1 /dev/disk/by-id | tail -n1)
DISK_NAME=$(readlink -f /dev/disk/by-id/${DISK_ID})
FSTYPE=$(lsblk -o FSTYPE -n ${DISK_NAME})
DISK_LABEL=$(lsblk -o LABEL -n ${DISK_NAME})

# Exit with message if not root
[[ $UID -ne 0 ]] && logger -t $TAG "Not root. Exiting." && exit 2

# Create filesystem if none
if [[ -z ${FSTYPE} ]] ; then
	mkfs.xfs ${DISK_NAME}
	if [[ $? -eq 0 ]] ; then
		logger -t $TAG "Created xfs filesystem on $DISK_NAME."
	else
		logger -t $TAG "ERROR: Could not create xfs on $DISK_NAME. Exiting."
		exit 90
	fi
fi
sleep 0.5
DISK_UUID=$(lsblk -no UUID ${DISK_NAME})

# Create label if none
[[ -z ${DISK_LABEL} ]] && xfs_admin -L ${LABEL} ${DISK_NAME}

# Create mount point if it does not exist
if [[ ! -d ${MOUNT_POINT} ]] ; then
	mkdir -p ${MOUNT_POINT}
	if [[ $? -eq 0 ]] ; then
		logger -t $TAG "Created mount point at $MOUNT_POINT."
	else
		logger -t $TAG "ERROR: Could not create mount point at $MOUNT_POINT. Exiting"
		exit 80
	fi
fi

# Only add to /etc/fstab if not already there
grep -q ${DISK_UUID} /etc/fstab
if [[ $? -ne 0 ]] ; then
	echo "# Following mount for ${DESCRIPTION}" >> /etc/fstab
	echo "UUID=${DISK_UUID}	${MOUNT_POINT}	xfs	defaults	0 0" >> /etc/fstab
	mount -a
	mount_check_1=$?
	mount | grep ${DISK_NAME} | grep ${MOUNT_POINT}
	mount_check_2=$?
	if [[ ${mount_check_1} -eq 0 ]] && [[ ${mount_check_2} -eq 0 ]]; then
		logger -t $TAG "Successfully mounted $DISK_NAME at $MOUNT_POINT."
	else
		logger -t $TAG "ERROR: Could not mount $DISK_NAME at $MOUNT_POINT. Exiting."
		exit 70
	fi
fi

###
### Configure network
###

hostname='__hostname__'
domain='__domain__'
dns1='__dns1__'
dns2='__dns2__'
default_gateway='__default_gateway__'

# 1 disable, 0 enable
ipv6_enable=1

port_mac[0]='__port_mac_0__'
port_ip[0]='__port_ip_0__'
port_netmask[0]='__port_netmask_0__'
port_gateway[0]='__port_gateway_0__'
port_def_route[0]='__port_def_route_0__'
port_dhcp[0]='__port_dhcp_0__'

port_mac[1]='__port_mac_1__'
port_ip[1]='__port_ip_1__'
port_netmask[1]='__port_netmask_1__'
port_gateway[1]='__port_gateway_1__'
port_def_route[1]='__port_def_route_1__'
port_dhcp[1]='__port_dhcp_1__'

port_mac[2]='__port_mac_2__'
port_ip[2]='__port_ip_2__'
port_netmask[2]='__port_netmask_2__'
port_gateway[2]='__port_gateway_2__'
port_def_route[2]='__port_def_route_2__'
port_dhcp[2]='__port_dhcp_2__'

# function to add underscore
add_underscore(){
  echo "__${1}__"
}

# filenames
net_scripts=/etc/sysconfig/network-scripts

# update network scripts with static ips and gateways
nic_count=($(ls -1d /sys/class/net/eth* | wc -l))
for i in {0..2} ; do
  if [[ ${port_mac[i]} != "__port_mac_${i}__" && \
    ( ${port_ip[i]} != "__port_ip_${i}__" || ${port_dhcp[i]} != "__port_dhcp_${i}__" ) ]] ; then
    for (( j=0 ; j<${nic_count} ; j++ )) ; do
      nic_mac=$(cat /sys/class/net/eth${j}/address) 
      if [[ ${port_mac[i]} == ${nic_mac} ]] ; then
        echo "NAME=eth${j}" > ${net_scripts}/ifcfg-eth${j}
        echo "DEVICE=eth${j}" >> ${net_scripts}/ifcfg-eth${j}
        if [[ ${port_dhcp[i]} =~ (yes|Yes|True|true) ]] ; then
          echo "BOOTPROTO=dhcp" >> ${net_scripts}/ifcfg-eth${j}
        elif [[ ${port_ip[i]} != "__port_ip_${i}__" ]] && [[ ${port_ip[i]} =~ .*:.* ]] ; then
          [[ ${ipv6_enable} -eq 1 ]] && ipv6_enable=0
          echo "BOOTPROTO=none" >> ${net_scripts}/ifcfg-eth${j}
          echo "IPV6INIT=yes" >> ${net_scripts}/ifcfg-eth${j}
          echo "IPV6ADDR=${port_ip[i]}" >> ${net_scripts}/ifcfg-eth${j}
          if [[ ${port_gateway[i]} != "__port_gateway_${i}__" ]] ; then
            echo "IPV6_DEFAULTGW=${port_gateway[i]}" >> ${net_scripts}/ifcfg-eth${j}
          elif [[ ${port_gateway[i]} == $(add_underscore 'port_gateway_0') ]] && [[ ${default_gateway} != $(add_underscore 'default_gateway') ]] ; then
            echo "IPV6_DEFAULTGW=${default_gateway}" >> ${net_scripts}/ifcfg-eth${j}
          fi
        elif [[ ${port_ip[i]} != "__port_ip_${i}__" ]] ; then
          echo "BOOTPROTO=none" >> ${net_scripts}/ifcfg-eth${j}
          echo "IPADDR=${port_ip[i]}" >> ${net_scripts}/ifcfg-eth${j}
          # Set gateway if provided. If not set, set eth0 to default
          if [[ ${port_gateway[i]} != "__port_gateway_${i}__" ]] ; then
            echo "GATEWAY=${port_gateway[i]}" >> ${net_scripts}/ifcfg-eth${j}
          elif [[ ${port_gateway[i]} == $(add_underscore 'port_gateway_0') ]] && [[ ${default_gateway} != $(add_underscore 'default_gateway') ]] ; then
            echo "GATEWAY=${default_gateway}" >> ${net_scripts}/ifcfg-eth${j}
          fi
          # Set netmask if provided. Else set netmask to 255.255.255.0
          if [[ ${port_netmask[i]} != "__port_netmask_${i}__" ]] ; then
            echo "NETMASK=${port_netmask[i]}" >> ${net_scripts}/ifcfg-eth${j}
          else
            echo 'NETMASK=255.255.255.0' >> ${net_scripts}/ifcfg-eth${j}
          fi
        fi
        echo "ONBOOT=yes" >> ${net_scripts}/ifcfg-eth${j}
        echo "HWADDR=${nic_mac}" >> ${net_scripts}/ifcfg-eth${j}
        # Set to DEFROUTE to no, unless otherwise stated. If not stated set to yes on eth0
        if [[ ${port_def_route[i]} =~ (yes|Yes|True|true) ]] ; then
          echo "DEFROUTE=yes" >> ${net_scripts}/ifcfg-eth${j}
        elif [[ ${port_def_route[i]} == $(add_underscore 'port_def_route_0') ]] ; then
          echo "DEFROUTE=yes" >> ${net_scripts}/ifcfg-eth${j}
        else
          echo "DEFROUTE=no" >> ${net_scripts}/ifcfg-eth${j}
        fi
      fi
    done
  fi
done

# Set DNS
if [[ ${dns1} != $(add_underscore 'dns1') ]] ; then
  echo "PEERDNS=yes" >> ${net_scripts}/ifcfg-eth0
  echo "DNS1=${dns1}" >> ${net_scripts}/ifcfg-eth0
  [[ ${dns2} != $(add_underscore 'dns2') ]] && echo "DNS2=${dns2}" >> ${net_scripts}/ifcfg-eth0
fi
# Set default gateway
[[ ${default_gateway} != $(add_underscore 'default_gateway') ]] && echo GATEWAY=${default_gateway} >> ${net_scripts}/ifcfg-eth0
# Set domain
if [[ ${domain} != $(add_underscore 'domain') ]] ; then
  echo DOMAIN=${domain} >> ${net_scripts}/ifcfg-eth0
  echo kernel.domainname=${domain} >> /etc/sysctl.conf
  sysctl -p
fi
# Set hostname
if [[ ${hostname} != $(add_underscore 'hostname') ]] ; then
  hostnamectl set-hostname ${hostname}
  sed -i "s/\(^127\.0\.0\.1 .*\)/\1 ${hostname}/" /etc/hosts
  sed -i "s/\(^::1 .*\)/\1 ${hostname}/" /etc/hosts
  sed -i "s/\(^127\.0\.0\.1 .*\)/\1 ${hostname}.${domain}/" /etc/hosts
  sed -i "s/\(^::1 .*\)/\1 ${hostname}.${domain}/" /etc/hosts
fi

# Enable ipv6 if there is an ipv6 address supplied in env
if [[ ${ipv6_enable} -eq 0 ]] ; then
  echo net.ipv6.conf.all.disable_ipv6 = 0 >> /etc/sysctl.conf
  echo net.ipv6.conf.default.disable_ipv6 = 0 >> /etc/sysctl.conf
  echo net.ipv6.conf.lo.disable_ipv6 = 0 >> /etc/sysctl.conf
  sysctl -p
fi

service network restart

## Disable Password Login for MechID group
echo "Match Group mechid" >> /etc/ssh/sshd_config
echo -e "\tPasswordAuthentication no" >> /etc/ssh/sshd_config
systemctl restart sshd

###
# Install SWM
###

## SWM variables
virc_cc_environment='__virc_cc_environment__'
virc_cc_version='__virc_cc_version__'
virc_cc_version_file='__virc_cc_version_file__'


## Add MechID user
mechid_user_name=$(grep 'SWM_AUTOUSER=' /tmp/input.env | cut -f 2 -d '=')
useradd -g mechid -p 'pahfhrkSZmUs.' ${mechid_user_name}

### Workaround ### REMOVE WHEN BUG FIXED ###
#mkdir -p /etc/chef/trusted_certs/

# Get packages to install from input.env, then delete from input.env
. /tmp/input.env
swm_install_pkgs=(${SWM_INIT_PACKAGES})
swm_install_pkg_deps=(${SWM_INIT_PACKAGE_DEPS})
sed -i '/SWM_INIT_PACKAGES="/,/\"/d' /tmp/input.env

./platform-init-1.5.5.sh /tmp/input.env

## Install SWM packages after SWM installation
export AFTSWM_USERNAME=${mechid_user_name}
mechid_user_enc_passwd=$(grep 'SWM_AUTOCRED=' /tmp/input.env | cut -f 2 -d '=')
export AFTSWM_PASSWORD=${mechid_user_enc_passwd}
export HOSTNAME=$(hostname)
#export HOME=/root

#/opt/app/aft/aftswmcli/bin/swmcli component pkginstall -c ${swm_install_pkgs} -n $(hostname).$(domainname) -w -fi -fs
#sleep 5
#cd
echo $SHELL
whoami
env
pwd

# install swm packages one at a time
for package in ${swm_install_pkg_deps[@]} ; do
  /opt/app/aft/aftswmcli/bin/swmcli component pkginstall -c ${package} -n $(hostname).$(domainname) -w -fi
done

for package in ${swm_install_pkgs[@]} ; do
  /opt/app/aft/aftswmcli/bin/swmcli component pkginstall -c ${package} -n $(hostname).$(domainname) -w -fi
done

### Run Chef Prep Scripts ###
USER=${mechid_user_name}
COOKBOOK_NAME='virc_cc'
VERSION=${virc_cc_version}
ENV=${virc_cc_environment}
VERSION_FILE=${virc_cc_version_file}

COOKBOOK_VERSION=""

for v in $(echo ${VERSION} | tr "." "\n")
do
    if [ "$v" -ge 0 -a "$v" -le 9 ]; then
        COOKBOOK_VERSION=${COOKBOOK_VERSION}0$v
    else
        COOKBOOK_VERSION=${COOKBOOK_VERSION}$v
    fi
done

COOKBOOK_VERSION="${COOKBOOK_VERSION:0:4}.1${COOKBOOK_VERSION:4:4}.1${COOKBOOK_VERSION:8:4}"

#cd /home/$USER/chef-repo
mkdir -p /home/$USER/scripts/$ENV
chown -R ${mechid_user_name}:mechid /home/$USER

chef_config_path="/home/${mechid_user_name}/chef-repo/.chef/knife.rb"

su - -c "/usr/bin/knife client delete $(hostname).$(domainname) -y -c ${chef_config_path}" ${mechid_user_name}
su - -c "/usr/bin/knife node delete $(hostname).$(domainname) -y -c ${chef_config_path}" ${mechid_user_name}

su - -c "/usr/bin/knife cookbook show $COOKBOOK_NAME $COOKBOOK_VERSION files Pyswm.py -c ${chef_config_path} > /home/${mechid_user_name}/scripts/$ENV/Pyswm.py" ${mechid_user_name}
#/usr/bin/knife cookbook show $COOKBOOK_NAME $COOKBOOK_VERSION files Pyswm.pyc > /home/$USER/scripts/$ENV/Pyswm.pyc
su - -c "/usr/bin/knife cookbook show $COOKBOOK_NAME $COOKBOOK_VERSION files install_swm.py -c ${chef_config_path} > /home/$USER/scripts/$ENV/install_swm.py" ${mechid_user_name}
su - -c "/usr/bin/knife cookbook show $COOKBOOK_NAME $COOKBOOK_VERSION files swm-installer-config.json -c ${chef_config_path} > /home/$USER/scripts/$ENV/swm-installer-config.json" ${mechid_user_name}

#cd /home/$USER/scripts/$ENV
su - -c "chmod 755 /home/$USER/scripts/$ENV/install_swm.py" ${mechid_user_name}
su - -c "cd /home/$USER/scripts/$ENV; ./install_swm.py $VERSION $ENV --components-nodes=\"vIRC-cc:$(hostname).$(domainname)\" --version-file=${VERSION_FILE}" ${mechid_user_name}
#./install_swm.py $VERSION $ENV --components-nodes="<<<ComponentName.FQDN>>>"

