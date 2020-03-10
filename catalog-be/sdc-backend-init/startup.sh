#!/bin/sh

cd /home/sdc/chef-solo || exit $?
chef-solo -c solo.rb -E "${ENVNAME}"
rc=$?
if [ $rc != 0 ]; then exit $rc; fi
