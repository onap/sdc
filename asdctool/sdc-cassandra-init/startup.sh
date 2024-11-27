#!/bin/sh

cd /home/sdc/chef-solo
chef-solo -c solo.rb  -E ${ENVNAME}
rc=$?
if [ $rc -ne 0 ]; then exit $rc; fi