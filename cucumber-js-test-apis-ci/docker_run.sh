#!/bin/bash


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
fi
echo "Workspace under: $WORKSPACE"
# add dynamic ports from kubernetes master
if [ -z "$TEST_CI_BE_HOST" ]
then
    TEST_CI_BE_HOST="$(ifconfig  'eth0' | sed -n '2p' | awk '{print $2}' | sed 's/addr://g')"
    TEST_CI_BE_PORT=8081
    TEST_CI_CATALOG_HOST=$TEST_CI_BE_HOST
fi
if [ -z "$TEST_CI_CATALOG_PORT" ]
then
    TEST_CI_CATALOG_PORT=8080
fi
echo "host $TEST_CI_BE_HOST"

if [ -e "$WORKSPACE/data" ]
then
	rm -Rf $WORKSPACE/data
fi


mkdir -p $WORKSPACE/data/logs/cucumber
mkdir -p $WORKSPACE/data/scripts
mkdir -p $WORKSPACE/data/environments

cp $WORKSPACE/sdc-os-chef/environments/Template.json $WORKSPACE/TEST_CI.json
cp $WORKSPACE/sdc-os-chef/scripts/docker_run.sh $WORKSPACE/data/scripts
cp $WORKSPACE/cucumber-js-test-apis-ci/config.json $WORKSPACE/data/environments/dockerConfig.json


chmod 777 $WORKSPACE/data/scripts/docker_run.sh

sed -i "s/xxx/TEST_CI/g" $WORKSPACE/TEST_CI.json
sed -i "s/yyy/$TEST_CI_BE_HOST/g" $WORKSPACE/TEST_CI.json
mv $WORKSPACE/TEST_CI.json $WORKSPACE/data/environments

sed -i "s/8080:8080/$TEST_CI_CATALOG_PORT:8080/g" $WORKSPACE/data/scripts/docker_run.sh
sed -i "s/:8080\/sdc/:$TEST_CI_CATALOG_PORT\/sdc/g" $WORKSPACE/data/scripts/docker_run.sh


echo "getting images for $IMAGES_TAG"
$WORKSPACE/data/scripts/docker_run.sh -e TEST_CI -r $IMAGES_TAG -p 10001

echo "setting configuration"

sed -i "s/onboarding.port/$TEST_CI_BE_PORT/g" $WORKSPACE/data/environments/dockerConfig.json
sed -i "s/onboarding.server/$TEST_CI_BE_HOST/g" $WORKSPACE/data/environments/dockerConfig.json
sed -i "s/onboarding.user/cs0008/g" $WORKSPACE/data/environments/dockerConfig.json
sed -i "s/CatalogBE.port/$TEST_CI_CATALOG_PORT/g" $WORKSPACE/data/environments/dockerConfig.json
sed -i "s/CatalogBE.server/$TEST_CI_CATALOG_HOST/g" $WORKSPACE/data/environments/dockerConfig.json
sed -i "s/CatalogBE.user/cs0008/g" $WORKSPACE/data/environments/dockerConfig.json

cat $WORKSPACE/data/environments/dockerConfig.json

echo "time to run the cucumber"

# TODO check status and tar stuff
docker run --name cucumber-sdc-api-tests --volume $WORKSPACE/data/environments:/var/lib/tests/environments --volume $WORKSPACE/data/logs/cucumber:/var/lib/tests/report onap/cucumber-sdc-api-tests:latest
echo "Checking exit status"
DOCKER_ID=`docker ps -a --filter name="cucumber-sdc-api-test" --format "{{.ID}}"`
EXIT_STATUS=`docker inspect  --format='{{.State.ExitCode}}' $DOCKER_ID`
docker rm -f $DOCKER_ID
echo "Exit status: $EXIT_STATUS"
echo " logs can be found under $WORKSPACE/data/logs"

echo "removing all the dockers"
docker ps -a --filter "name=sdc-" | grep $IMAGES_TAG | cut -d " " -f1 | xargs docker rm -f

exit $EXIT_STATUS

