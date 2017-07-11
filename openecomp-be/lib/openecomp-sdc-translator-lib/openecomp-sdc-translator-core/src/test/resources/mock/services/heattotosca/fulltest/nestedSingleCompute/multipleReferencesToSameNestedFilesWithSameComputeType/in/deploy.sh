#!/bin/bash 
#####################################################
# In case a volume should be attached to the instance
# 1) partitioning cinder volume
# 2) creating XFS on the volume
# 3) labeling the volume
# 4) mounting the volume
# 5) adding appropriate entry to the /etc/fstab file
#####################################################
# Configure Salt-minion
#####################################################
# Deploy ,provision and configure vSON components 
#####################################################
# Author: Dmitry Orzhehovsky
# Email: dorzheho@cisco.com
#####################################################
#                METADATA                          
#  Should be modified in case the version has changed
CLM_PKG_VERSION=1.0
#####################################################
DEBUG_LOG=/tmp/minion_debug-$$.log
vfc_role=$HOT_VFC_ROLE
this_instance_index=$HOT_INSTANCE_INDEX
IPv4="inet"
IPv6="inet6"
IP_INVALID="invalid"
 
failure() 
{
 local msg=$1
 local signal=$2

 $HOT_WC_NOTIFY -H "X-Auth-Token: $HOT_SWIFT_AUTH_TOKEN" --data-binary "{\"status\": \"FAILURE\", \"reason\": \"$signal\", \"data\": \"$msg\"}"
 exit $signal
}

_get_ip_family()
{ 
 local ip=$1
 local ip_family=$(python -c "

import socket

family = \"$IPv4\"
try:
   socket.inet_pton(socket.AF_INET, \"$ip\")
except:
   family = \"$IPv6\"
   try:
      socket.inet_pton(socket.AF_INET6, \"$ip\")
   except:
      family = \"$IP_INVALID\"
print family")

 [ "$ip_family" == "$IP_INVALID" ] && failure "Invalid IP $ip." 100
 echo $ip_family
}

_volume_persist_config_intucell()
{
 mkdir -p /intucell
 mount -L INTUCELL /intucell
 chown intucell:intucell /intucell
 echo "LABEL=INTUCELL /intucell xfs rw,noatime,attr2,noquota 0 2" >> /etc/fstab
}

set_static_ip() 
{
 local ip=$1
 local prefix_length=$2
 local interface=$3
 local ip_family=$4
 local gw=$5

 local netmask=$prefix_length

 if [ "$ip_family" == "$IPv4" ];then
    netmask=$(python -c "

import struct
import socket
print socket.inet_ntoa(struct.pack('>L', (1<<32) - (1<<32>>$prefix_length)))"
)
 fi
 
 if grep manual /etc/network/interfaces.d/$interface;then
    echo "auto $interface" > /etc/network/interfaces.d/$interface
 fi
 cat << EOF >> /etc/network/interfaces.d/$interface
iface $interface $ip_family static
     address $ip
     netmask $netmask
EOF
 
 if [ "X$gw" != "X" ];then
    echo "     gateway $gw" >> /etc/network/interfaces.d/$interface
 fi 

 ifdown $interface --force || true 
 ifup $interface 
}

set_dns_servers()
{

 for dns in $(echo $HOT_DNS_SERVERS|tr ',' ' ');do
   if [ "X$dns" != "X" ];then
      echo "nameserver $dns" >> /etc/resolv.conf
   fi
 done
}

set_ntp_servers()
{
 cat << EOF > /etc/systemd/timesyncd.conf
[Time]
Servers=$HOT_NTP_SERVERS
EOF
}

dns_lookup()
{
 server_ip=$1

 host $server_ip
 (( $? == 0 )) || return 1 
}

volume_main()
{
 set -e 

 if $(blkid  -L INTUCELL &> /dev/null);then
     _volume_persist_config_intucell
     mount -a
     return 
 fi
  
 volume_id=$HOT_VOLUME_ID

 disk_id=${volume_id:0:20}
 for device in /dev/disk/by-id/*;do
     [[ $device =~ part ]] && continue
     id=${device##*/virtio-}
     id=${id:0:20}
     if [[ "$id" == "$disk_id" ]];then
         (echo o; echo n; echo p; echo 1; echo; echo; echo w;) | fdisk $device
         partition=${device}-part1
         while true;do
              partprobe
              sleep 2
              [[ -L ${device}-part1 ]] && break
         done
         mkfs.xfs -f -L INTUCELL ${device}-part1
         _volume_persist_config_intucell
         mount -a
      fi
 done

 set +e
}

init_main() 
{
 local ip_family=$1

 ## SaltStack minion configuration
 if [ -z "$HOT_CLM_SERVER_IP" ];then
     clm_primary=1
     clm_server_ip=$HOT_THIS_INSTANCE_OAM_NET_IP
 else
     clm_server_ip=$HOT_CLM_SERVER_IP
 fi

 minion_conf=/etc/salt/minion.d/vson-minion.conf

 cat << EOF > $minion_conf
environment: prod
hash_type: sha256
mine_interval: 5

EOF
 [ "$ip_family" == "$IPv6" ] && echo "ipv6: True" >> $minion_conf
 host $clm_server_ip
 if (($? != 0 ));then 
     echo "$clm_server_ip clm0" >> /etc/hosts
     clm_server_ip=clm0
 fi

 if (( $clm_primary ));then
      cat << EOF >> $minion_conf
file_roots:
  prod:
    - /srv/cisco/salt/prod

pillar_roots:
  prod:
    - /srv/cisco/pillar/prod

EOF
 fi
 
  cat << EOF > /etc/salt/minion.d/vson-minion-mc.conf
master: 
  - $clm_server_ip
EOF

 minion_grains_conf=/etc/salt/grains
 cat << EOF > $minion_grains_conf
vson.environment: prod
vson.vfc_role: $vfc_role
vson.this_vfc_instance_index: $this_instance_index
EOF

 if [ -n "$HOT_DC_NAME" ];then
    echo "vson.dc_name: $HOT_DC_NAME" >> $minion_grains_conf
 fi
 if [ -n "$HOT_CLUSTER_NAME" ];then
    echo "vson.cluster_name: $HOT_CLUSTER_NAME" >> $minion_grains_conf
 fi
 if [ -n "$HOT_VSON_JOIN_CLUSTER_AUTH_TOKEN" ];then
    echo "vson.join_cluster_auth_token: $HOT_VSON_JOIN_CLUSTER_AUTH_TOKEN" >> $minion_grains_conf
 fi
 if [ -n "$HOT_SWIFT_CONTAINER_NAME" ];then
    echo "swift.container_name: $HOT_SWIFT_CONTAINER_NAME" >> $minion_grains_conf
 fi
 if [ -n "$HOT_SWIFT_STORAGE_URL" ];then
    echo "swift.storage_url: $HOT_SWIFT_STORAGE_URL" >> $minion_grains_conf
 fi
 if [ -n "$HOT_SWIFT_AUTH_TOKEN" ];then
    echo "swift.auth_token: $HOT_SWIFT_AUTH_TOKEN" >> $minion_grains_conf
 fi
 
  cat << EOF > /etc/salt/minion.d/vson-minion-mc.conf
master: 
  - $clm_server_ip
EOF

# Remove old minion_id file
 rm -f /etc/salt/minion_id
 rm -rf /var/cache/salt/minion/*
# Start Salt minion service
 systemctl start salt-minion
# Enable Salt minion service
 systemctl enable salt-minion
 sleep 5
}

clm_main()
{

 cd /srv; curl -s -H  "X-Auth-Token: $HOT_SWIFT_AUTH_TOKEN" $HOT_SWIFT_STORAGE_URL/$HOT_SWIFT_CONTAINER_NAME/vson-clm-${CLM_PKG_VERSION}.tar| tar xv

 if (( $clm_primary ));then
    salt-call --local state.apply vson.deploy -l debug --log-file-level=debug --log-file=$DEBUG_LOG
 else
    salt-call state.apply vson.deploy -l debug --log-file-level=debug --log-file=$DEBUG_LOG
    salt-call state.apply vson.fire_events.reconfigure_minions -l debug --log-file-level=debug --log-file=$DEBUG_LOG
 fi
 if grep "ERROR" $DEBUG_LOG;then 
    return 1
 fi
}

vfc_main()
{
 salt-call saltutil.sync_all
 salt-call state.apply vson.deploy -l debug --log-file-level=debug --log-file=$DEBUG_LOG
 if [ "$vfc_role" == "mon" ];then
    salt-call state.apply vson.vfc-mon.fire_events.configure_multisite -l debug --log-file-level=debug --log-file=$DEBUG_LOG
 fi
 if grep "ERROR" $DEBUG_LOG;then 
    return 1
 fi
}

###### For testing only #######
echo -e "cisco\ncisco"|passwd
sed -i "s#\(PermitRootLogin\) without-password#\1 yes#" /etc/ssh/sshd_config
systemctl restart ssh
###############################

echo "LANG=en_US.utf-8" > /etc/environment
echo "LC_ALL=en_US.utf-8" >> /etc/environment
touch /var/lib/cloud/instance/locale-check.skip

rm -rf /etc/apt/sources.list.d/*

## Let rsyslog reread new name of the instance 
systemctl restart rsyslog

set_static_ip "$HOT_THIS_INSTANCE_OAM_NET_IP" "$HOT_THIS_INSTANCE_OAM_NET_PREFIX" "eth0" $(_get_ip_family $HOT_THIS_INSTANCE_OAM_NET_IP) $HOT_THIS_INSTANCE_DEFAULT_GATEWAY
if [ "X$HOT_THIS_INSTANCE_OAM_V6_NET_IP" != "X" ];then 
    set_static_ip "$HOT_THIS_INSTANCE_OAM_V6_NET_IP" "$HOT_THIS_INSTANCE_OAM_V6_NET_PREFIX" "eth0" $(_get_ip_family $HOT_THIS_INSTANCE_OAM_V6_NET_IP)
fi

set_dns_servers
set_ntp_servers
systemctl restart systemd-timesyncd

if [ "$HOT_VOLUME_ATTACH" == "True" ];then
   volume_main || failure "Cinder volume configuration." 110
fi

init_main $IPv4 || failure "VFC initial configuration" 120

if [ "$vfc_role" == "clm" ];then 
   clm_main || failure "vSON CLM deployment." 130
else
   mkdir /intucell
   chown -R intucell:intucell /intucell
   vfc_main || failure "vSON $vfc_role deployment." 140
fi

 $HOT_WC_NOTIFY -H "X-Auth-Token: $HOT_SWIFT_AUTH_TOKEN" --data-binary '{"status": "SUCCESS"}'
 rm -f $DEBUG_LOG

