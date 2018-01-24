#!/bin/sh

cd /root/chef-solo/
chef-solo -c solo.rb -E ${ENVNAME}

/docker-entrypoint.sh elasticsearch &

while true; do sleep 2; done

