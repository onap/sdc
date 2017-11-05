#!/bin/sh

# Save cs_cacert file
cs_cacert="__cs_cacert__"
[[ -n $cs_cacert ]] && {
    echo "$cs_cacert" > /etc/ieccf/conf/cs_cacert
    chmod 400 /etc/ieccf/conf/cs_cacert
}
      
typeset -l ENABLE_DEBUG="__ieccf_debug__"
[[ "$ENABLE_DEBUG" == "t" || "$ENABLE_DEBUG" == "y" ]] && export DEBUG=YES

# Store ieccf config url in /etc/ieccf/conf/ieccf_config_url
ic_url="__ieccf_config_url__"
[[ -n $ic_url ]] && echo "$ic_url" > /etc/ieccf/conf/ieccf_config_url

mkdir -p /etc/psp

#
# Common code for all nodes
#

# store VMID in /etc/psp/vmid
cat /var/lib/cloud/data/instance-id > /etc/psp/vmid 2>/dev/null

# Store siteinfo url in /etc/psp/siteinfo_url
stack_name="__heat_stack_name__"
[[ -n $stack_name ]] && echo "$stack_name" > /etc/psp/heat_stack_name

# Store siteinfo url in /etc/psp/siteinfo_url
si_url="__node_siteinfo_url__"
[[ -n $si_url ]] && echo "$si_url" > /etc/psp/siteinfo_url

# Store ActivePilot IP in /etc/psp/ActivePilot
activepilot_ip=__activepilot_ip__
[[ -n $activepilot_ip ]] && echo $activepilot_ip > /etc/psp/ActivePilot

# Inject an ActivePilot route
echo "$(</etc/psp/ActivePilot) dev eth0" > /etc/sysconfig/network-scripts/route-cloud0
/etc/sysconfig/network-scripts/ifup-routes cloud0

# Store host security key in /etc/psp/host_key
host_key="__host_key__"
[[ -n $host_key ]] && {
echo "$host_key" > /etc/psp/host_key
chmod 400 /etc/psp/host_key
}

# Store shared volume ID in /etc/psp/shared_volid1
shared_volid1="__shared_volid1__"
[[ $shared_volid1 != __*__  ]] && {
echo "$shared_volid1" > /etc/psp/shared_volid1
chmod 400 /etc/psp/shared_volid1
}

#
# Pilot code
#

typeset -u pilot="__pilot__"
[[ -n $pilot ]] || exit 0

# Set I_am file
rm -f /root/.I_am_[AB]
touch /root/.I_am_$pilot

# Allow SSH via Password
sed -i 's/^#PasswordAuthentication.*/PasswordAuthentication yes/' /etc/ssh/sshd_config
sed -i 's/^PasswordAuthentication.*/PasswordAuthentication yes/' /etc/ssh/sshd_config
service sshd restart

# Pass ignore_audit through template
typeset -u ignore_audit="__ignore_siteinfo_audit__"
case $ignore_audit in
    1|T|TRUE|ON|Y|YES) touch /root/ignore_audit;;
esac
    
# Save os_cacert file
os_cacert="__os_cacert__"
[[ -n $os_cacert ]] && {
    echo "$os_cacert" > /etc/psp/os_cacert
    chmod 400 /etc/psp/os_cacert
}

# Update resolv.conf based on passed nameserver and domain
nameserver1="__nameserver1__"
nameserver2="__nameserver2__"
domain="__domain__"
[[ -n $nameserver1 ]] && {
  # Need to modify the ifup-dhcp script so that ifup LSN doesn't append
  # unwanted entries to resolv.conf
  # This is necessary to support releases where udhcpc client is still used for internal
  sed -i '/RESOLV_CONF=/a RESOLV_CONF="/tmp/resolv.overridden"' /etc/sysconfig/network-scripts/ifup-dhcp
  
  # Kill off any running DHCP clients as they may re-write resolv.conf
  pkill -f /sbin/dhclient
  
  > /etc/resolv.conf
  echo "; set by MCAS userdata script DHCP override" >> /etc/resolv.conf
  [[ -n $domain ]] && echo search $domain >> /etc/resolv.conf
  echo nameserver $nameserver1 >> /etc/resolv.conf
  [[ -n $nameserver2 ]] && echo nameserver $nameserver2 >> /etc/resolv.conf
  
  # Need to set PEERDNS in network for RHEL6 dhclient support as
  # dhclient-script doesn't read ifcfg files correctly
  grep -qs PEERDNS=no /etc/sysconfig/network || echo PEERDNS=no >> /etc/sysconfig/network
  
  
  # Update ifcfg files to prevent DHCP client resolv.conf changes
  for f in /etc/sysconfig/network-scripts/ifcfg-cloud*; do
    ifup ${f##ifcfg-} &
  done
}
      
# Localize exists, kick that off and exit
if [[ -f /opt/config/bin/Localize ]]; then
    rm -f /install/fresh.install
    typeset -A args=([A]=install)
    /opt/config/bin/Localize ${args[$pilot]} &
else
    # No Localize, Staging install, just setup and autoinstall
    # will kick off newinstall1
    mkdir -p /install
    touch /install/fresh.install
fi

script_url="__script_url__"
script_args="__script_args__"

if [[ -n $script_url ]]; then
tmp_script=/tmp/pilot${pilot}_startup
# Delete any pre-existing script, we don't want to execute an old, leftover script
rm -f $tmp_script
while true; do
    curl --connect-timeout 5 -o $tmp_script -gf $script_url 2>> ${tmp_script}.curl.out
    [[ -f $tmp_script ]] && break
    # Break for a detected SU/CPR in progress
    [[ -f /root/.SU_inprog ]] && echo "SU detected" && exit 0
    [[ -f /root/.CPR_inprog ]] && echo "CPR detected" && exit 0
    echo "Download of $script_url failed, and no SU/CPR detected.  Waiting to try again."
    sleep 3
done

chmod +x $tmp_script
$tmp_script $script_args > ${tmp_script}.out; rc=$?
(( rc == 0 )) || exit $rc
fi

post_exec="__post_exec__"
eval $post_exec &

exit 0