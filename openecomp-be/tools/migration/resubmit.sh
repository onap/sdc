#!/bin/bash

###########################################################################################################
# script name - resubmit.sh
# run script - ./resubmit.sh
# this script performs check out, check in, submit for all submitted Vendor Software Products
# working vs. localhost
###########################################################################################################


# check out, check in, submit for all submitted Vendor Software Products
curl -X PUT --header "Content-Type: application/json" --header "Accept: application/json" "http://localhost:8080/onboarding-api/v1.0/vendor-software-products/reSubmitAll"

