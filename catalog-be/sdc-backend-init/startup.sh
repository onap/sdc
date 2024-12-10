#!/bin/sh

set -e


log_script() {
    script_name=$1
    sh "$script_name" 2>&1 | sed "s|^|[$script_name] |" || \
    echo "$script_name failed. Continuing with other scripts..." >&2
}

log_script "/home/onap/create_consumer_and_user.sh"
log_script "/home/onap/check_backend.sh"
log_script "/home/onap/import_normatives.sh"

echo "Chef Client finished"
