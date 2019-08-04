#!/bin/bash

cd /var/lib/tests

rm devConfig.json

mkdir resources/downloads

npm run test-and-report
