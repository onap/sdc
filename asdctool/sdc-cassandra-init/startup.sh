#!/bin/bash

export CHEFNAME=${ENVNAME}
cd /root/chef-solo
chef-solo -c solo.rb  -E ${CHEFNAME}
rc=$?
if [[ $rc != 0 ]]; then exit $rc; fi
#while true; do sleep 2; done

