#!/bin/sh

cd /root/chef-solo/
chef-solo -c solo.rb -E ${ENVNAME}

chef_status=$?

/docker-entrypoint.sh elasticsearch &

exec "$@";