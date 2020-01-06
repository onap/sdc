#!/bin/bash

/dockerstartup/vnc_startup.sh &

# prepare env for HTTPS if used

is_https=$(cat /root/chef-solo/environments/${ENVNAME}.json | \
    jq -cr '.default_attributes.disableHttp' | \
    tr '[:upper:]' '[:lower:]')

if [ "$is_https" = true ] ; then
    # setup /etc/hosts
    SDC_FE_IP=$(cat /root/chef-solo/environments/${ENVNAME}.json | \
        jq -cr '.default_attributes.Nodes.FE')
    SDC_FE_HOSTNAME=$(cat /root/chef-solo/environments/${ENVNAME}.json | \
        jq -cr '.override_attributes.FE.domain_name')
    if ! grep -q "^[[:space:]]*${SDC_FE_IP}[[:space:]]" ; then
        echo "${SDC_FE_IP}" "${SDC_FE_HOSTNAME}" >> /etc/hosts
    fi
fi

# run tests

cd /root/chef-solo || { rc=$?; echo "Startup failed"; exit $rc; }
chef-solo -c solo.rb -E "${ENVNAME}"

rc=$?

if [[ $rc != 0 ]]; then
   echo "Startup failed"
   exit $rc
else
# Note that the output below is monitored in CSIT by
# sdc/sdc-os-chef/scripts/docker_run.sh
# If this text is changed, docker_run.sh check for sdc-ui-tests docker
# startup must be adjusted accordingly!
   echo "Startup completed successfully"
fi
