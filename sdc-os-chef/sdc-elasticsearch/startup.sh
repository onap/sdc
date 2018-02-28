#!/bin/sh

cd /root/chef-solo/
chef-solo -c solo.rb -E ${ENVNAME}

rc=$?
if [[ $rc != 0 ]]; then exit $rc; fi

/docker-entrypoint.sh elasticsearch &

while true; do sleep 30; done