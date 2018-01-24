#!/bin/sh

cd /root/chef-solo/
chef-solo -c solo.rb -E ${ENVNAME}

#for debug purposes keeps the docker alive
#while true; do sleep 2; done