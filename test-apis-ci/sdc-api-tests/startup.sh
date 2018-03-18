#!/bin/bash

cd /root/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}

rc=$?

if [[ $rc != 0 ]]; then
   echo "Sanity failed !!!"
   exit $rc
else
   echo "completed successfully :-)"
   exit 0
fi