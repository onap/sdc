#!/bin/bash

/dockerstartup/vnc_startup.sh &

cd /root/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}

rc=$?

if [[ $rc != 0 ]]; then
   echo "Startup failed !!!"
   exit $rc
else
# Note that the output below is monitored in CSIT by
# sdc/sdc-os-chef/scripts/docker_run.sh
# If this text is changed, docker_run.sh check for sdc-ui-tests docker
# startup must be adjusted accordingly!
   echo "Startup completed successfully"
fi
