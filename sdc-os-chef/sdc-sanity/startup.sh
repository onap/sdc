#!/bin/bash

export CHEFNAME=${ENVNAME}
cd /root/chef-solo
chef-solo -c solo.rb -E ${CHEFNAME}