#!/bin/bash

cd /var/lib/tests
yarn install
yarn run test-and-report
