#!/bin/sh

cd /root/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}

while true; do sleep 2; done
#rc=$?
#if [[ $rc != 0 ]]; then exit $rc; fi
