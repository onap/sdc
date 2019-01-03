#!/bin/bash


TESTDIR=`pwd`
TESTDIR=$TESTDIR/target/ciTest

# checking some stuff for local run
if [ -z "$BUILD_NUMBER" ]
then
    if [ -z "$WORKSPACE" ]
    then
        currentDir=`pwd`
        cd ../
        WORKSPACE=`pwd`
        cd $currentDir
    fi
    if [ -z "$IMAGES_TAG" ]
    then
        IMAGES_TAG=1.4-STAGING-latest
    fi
    if [ -z "$LOGS_LOCATION" ]
    then
        LOGS_LOCATION=$TESTDIR
    fi
fi
echo $WORKSPACE

# add dynamic ports from kubernetes master
if [ -z "$TEST_CI_BE_HOST" ]
then
    TEST_CI_BE_HOST="$(ifconfig  'eth0' | sed -n '2p' | awk '{print $2}' | sed 's/addr://g')"
    TEST_CI_BE_PORT=8285
    TEST_CI_CATALOG_HOST=$TEST_CI_BE_HOST
    TEST_CI_CATALOG_PORT=$TEST_CI_BE_PORT
    TEST_CI_AP_HOST=$TEST_CI_BE_HOST
    TEST_CI_AP_PORT=8080
fi


echo "host $TEST_CI_BE_HOST"

if [ -e "./target/ciTest" ]
then
	rm -Rf ./target/ciTest
fi


mkdir -p $TESTDIR
echo "running from $TESTDIR"

mkdir -p $TESTDIR/data/logs/cucumber
mkdir -p $TESTDIR/data/scripts
mkdir -p $TESTDIR/data/environments

cp $WORKSPACE/sdc-os-chef/environments/Template.json $TESTDIR/TEST_CI.json
cp $WORKSPACE/sdc-os-chef/scripts/docker_run.sh $TESTDIR/data/scripts
cp $WORKSPACE/cucumber-js-test-apis-ci/config.json $TESTDIR/data/environments/dockerConfig.json


chmod 777 $TESTDIR/data/scripts/docker_run.sh

sed -i "s/xxx/TEST_CI/g" $TESTDIR/TEST_CI.json
sed -i "s/yyy/$TEST_CI_BE_HOST/g" $TESTDIR/TEST_CI.json
sed -i "s/8080/$TEST_CI_BE_PORT/g" $TESTDIR/TEST_CI.json
mv $TESTDIR/TEST_CI.json $TESTDIR/data/environments

echo "getting images for $IMAGES_TAG"
OLD_WORKSPACE=$WORKSPACE
export WORKSPACE=$TESTDIR
$WORKSPACE/data/scripts/docker_run.sh -e TEST_CI -r $IMAGES_TAG -sim -p 10001

echo "setting configuration"

sed -i "s/onboarding.port/$TEST_CI_BE_PORT/g" $TESTDIR/data/environments/dockerConfig.json
sed -i "s/onboarding.server/$TEST_CI_BE_HOST/g" $TESTDIR/data/environments/dockerConfig.json
sed -i "s/onboarding.user/cs0008/g" $TESTDIR/data/environments/dockerConfig.json
sed -i "s/CatalogBE.port/$TEST_CI_CATALOG_PORT/g" $TESTDIR/data/environments/dockerConfig.json
sed -i "s/CatalogBE.server/$TEST_CI_CATALOG_HOST/g" $TESTDIR/data/environments/dockerConfig.json
sed -i "s/CatalogBE.user/cs0008/g" $TESTDIR/data/environments/dockerConfig.json
sed -i "s/activity_spec.port/$TEST_CI_AP_PORT/g" $TESTDIR/data/environments/dockerConfig.json
sed -i "s/activity_spec.server/$TEST_CI_AP_HOST/g" $TESTDIR/data/environments/dockerConfig.json
sed -i "s/activity_spec.user/cs0008/g" $TESTDIR/data/environments/dockerConfig.json

cat $TESTDIR/data/environments/dockerConfig.json

echo "time to run the cucumber"

# TODO check status and tar stuff
docker run --name cucumber-sdc-api-tests --volume $WORKSPACE/data/environments:/var/lib/tests/environments --volume $WORKSPACE/data/logs/cucumber:/var/lib/tests/report onap/cucumber-sdc-api-tests:latest
DOCKER_ID=`docker ps -a --filter name="cucumber-sdc-api-test" --format "{{.ID}}"`
EXIT_STATUS=`docker inspect $DOCKER_ID --format='{{.State.ExitCode}}'`
echo "cucumber docker exited with status $EXIT_STATUS"

tar -czf $TESTDIR/logs_$BUILD_NUMBER.tar.gz $TESTDIR/data/logs $TESTDIR/data/environments
## Move to NFS location
echo "Logs can be found at: $LOGS_LOCATION/logs_$BUILD_NUMBER.tar.gz"
if [ ! -d "$LOGS_LOCATION" ]; then
  sudo mkdir -p $LOGS_LOCATION
fi

sudo cp $TESTDIR/logs_$BUILD_NUMBER.tar.gz $LOG_LOCATION_SERVER_PREFIX$LOGS_LOCATION/logs_$BUILD_NUMBER.tar.gz
sudo cp $TESTDIR/data/cucumber/report.html $LOGS_LOCATION/emailable-report-$BUILD_NUMBER.html
WORKSPACE=$OLD_WORKSPACE

echo "removing all the dockers"
docker ps -a --filter "name=sdc-" | grep $IMAGES_TAG | cut -d " " -f1 | xargs docker rm -f

exit $EXIT_STATUS
