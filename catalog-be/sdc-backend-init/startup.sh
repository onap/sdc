#!/bin/sh

set -e


log_script() {
    script_name=$1
    echo "Running $script_name..."
    sh "$script_name" || {
        echo "$script_name failed." >&2
        exit 1
    }
}

log_script "/home/onap/create_consumer_and_user.sh"
log_script "/home/onap/check_backend.sh"
log_script "/home/onap/import_normatives.sh"

echo "Done"
