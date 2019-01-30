#!/bin/bash

cd /var/lib/tests
mkdir resources/downloads
yarn install
yarn run test-and-report
