#!/bin/sh

cd /home/onap/init-script
sh -x /home/onap/check_backend.sh
sh -x /home/onap/import_normatives.sh
