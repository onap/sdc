#!/bin/sh

cd /home/"${user}"/chef-solo || exit $?
chef-solo -c solo.rb -E "${ENVNAME}"