# ONAP SDC

## Introduction

Dummy change

SDC is the ONAP visual modeling and design tool. It creates internal metadata that describes assets used by all ONAP components, both at design time and run time.

The SDC manages the content of a catalog and logical assemblies of selected catalog items to completely define how and when VNFs are realized in a target environment.
A complete virtual assembly of specific catalog items, together with selected workflows and instance configuration data, completely defines how the deployment, activation, and life-cycle management of VNFs are accomplished.

SDC manages four levels of assets:

* Resource - A fundamental capability, implemented either entirely in software, or as software that interacts with a hardware device.
Each Resource is a combination of one or more Virtual Function Components (VFCs), along with all the information necessary to instantiate, update, delete and manage the Resource.
* Service - A well-formed object comprising one or more Resources. Service Designers create Services from Resources, and include all of the information about the Service needed to instantiate, update, delete and manage the Service.

The key output of SDC is a set of models containing descriptions of asset capabilities and instructions to manage them. These models are stored in the SDC Master Reference Catalog for the entire enterprise to use.

There are four major components of SDC:

* Catalog - The repository for assets at the Resource, Service and Product levels. Assets are added to the Catalog using the Design Studio.
* Design Studio - Used to create, modify and add Resource, Service and Product definitions in the Catalog.
* Certification Studio - Available in a future release, is used to test new assets at all levels. It will be used for sandbox experimentation, and will include support for automated testing.
* Distribution Studio - Used to deploy certified assets. From the Distribution studio, new Product assets, including their underlying Resources and Services, are deployed into lab environments for testing purposes, and into production after certification is complete. In a future release, there will be a way to export Product information to external Business Support Systems for customer ordering and billing.

## Git Configuration

Note that if you're working on Windows, it's important to enable long paths for your machine; otherwise git won't be able to handle some files.

In order to do so just add this section to your global git.config file under the `[core]` key:

    longpaths = true

## Compiling the Project

SDC is built from several projects while the parent "sdc" contains the main pom.xml for all of them:
- asdctool		- set of utilities used for scheme creation and data migration in SDC
- catalog-be		- backend code
- catalog-fe		- frontend java code (servlet, proxy)
- catalog-dao		- database layer
- catalog-model		- data model of the application
- catalog-ui		- front end code (javascript, html, css)
- common		    - set of utilities used by the onboarding project
- common-app-api	- common code for frontend and backend
- common-be		    - utilities, datatypes and enums
- security-utils	- handle encryption/decryption of passwords
- onboarding-be		- onboarding backend code
- onboarding-ui		- onboarding frontend code
- integration-tests - The integration tests using the docker images to validate Backend API calls and FE with Selenium
- sdc-os-chef		- chefs scripts used for docker creation and startup
- utils			    - set of dev utils used for working with the project locally


In order to build all the projects, as mentioned in the onap wiki https://wiki.onap.org/display/DW/Setting+Up+Your+Development+Environment, the settings.xml (https://git.onap.org/oparent/plain/settings.xml) from the oparent project must be installed in your ~/.m2 folder and referenced by your IDE.
Once maven is set up properly, go to sdc project and run the command: `mvn clean install`

By default, the "all" maven profile will be executed but others exist:
* fast-build - A fast build skipping all tests and useless maven plugins (only builds the jars)
* start-sdc - Once docker containers have been build, triggering this profile starts SDC CS, BE, FE, simulator
* stop-sdc - Stop all SDC containers started by using the profile "start-sdc"
* run-integration-tests - This runs only the integration tests against a running SDC started by using "start-sdc" profile.
* docker - This enables the docker images build for each SDC module
    **Note: If you're working on Windows, you'll need to define an environment variable on your machine with key `DOCKER_HOST` and value: `tcp://<ip_address>:2375` in order to build and upload local dockers to a local environment.**

More flags to use in the build process are:
* -DskipITs - Skips integration tests only
* -DskipTests - Skips unit tests execution and integration tests
* -DskipUICleanup=true - Skips deleting the UI folders
* -Djacoco.skip=true - Skips running jacoco tests
* -DskipPMD - Skips creating a PMD report

## Accessing SDC

After having started SDC with the command `mvn clean install -P start-sdc`, you can access it by accessing this URL: `http://<ip_address>:8285/login`
As ONAP AAF is not present, the url provided uses the 8285 simulator ports, just click on the user you want to use for accessing SDC (i.e. Carlos Santana)
For more information regarding using the webseal_simulator please refer to the following guide: [SDC Simulator](https://wiki.onap.org/display/DW/SDC+Simulator)

### SDC Containers

The following table shows the SDC containers found after a maven "start-sdc":

    CONTAINER ID        IMAGE                                    COMMAND                  CREATED             STATUS                      PORTS                                                                              NAMES
    968a8168e412        onap/sdc-backend-init:latest             "/bin/sh -c /home/${…"   9 minutes ago       Exited (0) 54 seconds ago                                                                                      sdc-backend-init-1
    621c0fda1b0f        onap/sdc-backend-all-plugins:latest      "sh -c ${JETTY_BASE}…"   9 minutes ago       Up 9 minutes                0.0.0.0:4000->4000/tcp, 0.0.0.0:8080->8080/tcp, 0.0.0.0:8443->8443/tcp             sdc-backend-all-plugins-1
    d823078776d8        onap/sdc-onboard-backend:latest          "sh -c ${JETTY_BASE}…"   9 minutes ago       Up 9 minutes                0.0.0.0:4001->4001/tcp, 0.0.0.0:8081->8081/tcp, 0.0.0.0:8445->8445/tcp, 8080/tcp   sdc-onboard-backend-1
    4729b0b7f0fe        onap/sdc-simulator:latest                "sh -c ${JETTY_BASE}…"   9 minutes ago       Up 9 minutes                0.0.0.0:8285->8080/tcp, 0.0.0.0:8286->8443/tcp                                     sdc-simulator-1
    583e0d7fa300        onap/sdc-onboard-cassandra-init:latest   "/home/sdc/startup.sh"   9 minutes ago       Exited (0) 9 minutes ago                                                                                       sdc-onboard-cassandra-init-1
    92085524f19f        onap/sdc-cassandra-init:latest           "/home/sdc/startup.sh"   10 minutes ago      Exited (0) 9 minutes ago                                                                                       sdc-cassandra-init-1
    c6e90dd7ddaf        selenium/standalone-firefox:2.53.1       "/opt/bin/entry_poin…"   10 minutes ago      Up 10 minutes               0.0.0.0:4444->4444/tcp                                                             standalone-firefox-1
    e02139c0379b        onap/sdc-frontend:latest                 "sh -c ${JETTY_BASE}…"   10 minutes ago      Up 10 minutes               0.0.0.0:6000->6000/tcp, 0.0.0.0:8181->8181/tcp, 0.0.0.0:9443->9443/tcp, 8080/tcp   sdc-frontend-1
    96843fae9e4c        onap/sdc-cassandra:latest                "/root/startup.sh"       10 minutes ago      Up 10 minutes               7000-7001/tcp, 7199/tcp, 9160/tcp, 0.0.0.0:9042->9042/tcp                          sdc-cassandra-1

For further information and an image explaining the containers dependency map please refer to the following page: [SDC Docker Diagram](https://wiki.onap.org/display/DW/SDC+Troubleshooting)


### Accessing the logs

To access the logs, there are different options:
* Connect to the docker container you want to inspect by doing `docker exec -it -u root sdc-XXXXXXXX-1 sh` 
    Then look at the logs generally in /var/lib/jetty/logs or /var/log/onap (that may differ !)
* A volume is shared between the BE, onboard-BE and FE containers, this volume is mapped to `/tmp/sdc-integration-tests`,
    In that folder you can obtain the logs of the different containers 

### Debugging SDC

After having started SDC with the command `mvn clean install -P start-sdc`, different java remote debug ports are opened by default:
* Onboard Backend - 4001 (jetty)
* Backend - 4000 (jetty)
* Frontend - 6000 (jetty)
It's therefore possible to connect your IDE to those debug ports remotely to walk through the code and add some breakpoints.

**Look at the pom.xml of the integration-tests module to have a better understanding of all the docker settings provided to start SDC.**

### Integration tests
The integration are composed of 2 parts, one to test the BE Apis and another one to test the FE with selenium.
The selenium tests make use of the selenium/standalone-firefox:2.53.1 container.

About BE APIs tests, onboarding E2E flow :
Onboarding E2E flow cover following SDC functionality:

    Onboard of VNF
    Create VF from VSP
    Certify VF 
    Create Service
    Add VF to service

    Certify Service
    Export TOSCA and validate it structure using external TOSCA parser

**as part of execution we open a connection to Titan and perform resources clean up both before and after tests execution (only resource with “ci” prefix will be deleted from the catalog)
List of VNFs/PNFs that proceed by onboarding flow, located in `integration-tests/src/test/resources/Files/`)

    sample-signed-pnf-cms-includes-cert-1.0.1-SNAPSHOT.zip
    sample-signed-pnf-1.0.1-SNAPSHOT.zip
    sample-pnf-1.0.1-SNAPSHOT.csar
    sample-pnf-custom-type.csar
    base_vfw.zi
    base_vvg.zip
    database-substitution-mappings.csar
    helm.zip
    Huawei_vHSS.csar
    Huawei_vMME.csar
    infra.zip
    resource-ZteEpcMmeVf-csar_fix.csar
    vbng.zip
    vbrgemu.zip
    vfw.zip
    vgmux.zip
    vgw.zip
    vLB.zip
    vLBMS.zip
    vSBC_update_v03.csar
    vsp-vgw.csar
    vvg.zip
    ZteEpcMmeVf.csar

#### Start the integration tests manually

Those tests execute the following
There are 2 options to start them:
* After having started SDC with the command `mvn clean install -P start-sdc`, run the command `mvn clean install -P run-integration-tests`
* If you want to debug them and run them from your IDE, you must start them from the testNG Suites files, otherwise this won't work.
  The test suites are located here:
  * BE: `integration-tests/src/test/resources/ci/testSuites/backend`
  * FE: `integration-tests/src/test/resources/ci/testSuites/frontend`

#### Integration tests with Helm Validator

Those tests use container built externally in other ONAP repository: [sdc/sdc-helm-validator](https://gerrit.onap.org/r/admin/repos/sdc/sdc-helm-validator)

You can run those tests same as default integration tests by adding additional profile to maven commands:
`integration-tests-with-helm-validator`
* To start SDC with Helm Validator run: `mvn clean install -P start-sdc,integration-tests-with-helm-validator`
* To execute tests that use Helm Validator use: `mvn clean install -P run-integration-tests,integration-tests-with-helm-validator`
## Accessing SDC UI in Dev Mode (Legacy way)

In order to access the SDC UI from your dev environment you need to do the following:

1. Go to file `webpack.server.js` found under the catalog-ui folder in the main sdc project and update the "localhost" variable to be the ip of your local vagrant machine.
2. Navigate to the catalog-ui folder and run the command: `npm start -- --env.role <wanted_role>` with the wanted role to login to SDC as.

## SDC on OOM

For more information regarding SDC on OOM please refer to the following page: [SDC on OOM](https://wiki.onap.org/display/DW/SDC+on+OOM)

## Frontend Local Env - onboarding

### Steps:

Install nodejs & gulp
1. download nodejs from here: https://nodejs.org/en/ (take the "current" version with latest features) & install it.
2. install gulp by running the following command: npm install --global gulp-cli

### Install DOX-UI a:

1. pull for latest changes
2. go to folder dox-sequence-diagram-ui
3. run npm install
4. wait for it...
5. go to folder dox-ui
6. run npm install
7. create a copy of devConfig.defaults.json file and name it devConfig.json (we already configured git to ignore it so it will not be pushed)
8. in that file, populate the fields of the IP addresses of your BE machine you'd like to connect (pay attention, it is a JSON file): For example http://<host>:<port>
9. after everything is successful, run gulp
10. after server is up, your favorite UI will wait for you at: http://localhost:9000/sdc1/proxy-designer1#/onboardVendor

### Troubleshooting:

| Problem                       |   Why is this happening | Solution                                                                                   |
|-------------------------------|-------------------------|--------------------------------------------------------------------------------------------|
| npm cannot reach destination  | onboarding proxy        | When within onboarding network, you should set onboarding proxy to NPM as the following:   |
|                               |                         | npm config set proxy http://genproxy:8080                                                  |
|                               |                         | npm config set https-proxy http://genproxy:8080                                            |
|                               |                         |                                                                                            |
| git protocol is blocked       | onboarding network      | When within onboarding network, you should set globally that when git                      |
| and cannot connect            | rules for protocols     | protocol is used, it will be replaced with "https"                                         |
|                               |                         | git config --global url."https://".insteadOf git://                                        |
--------------------------------------------------------------------------------------------------------------------------------------------------------

## SDC Troubleshooting

In order to check the life state of SDC you can run the command `health` from inside the vagrant.
Alternatively you can run the following commands to check the FE and BE status:

FE - `curl http://<ip_address>:8181/sdc1/rest/healthCheck`

BE - `curl http://<ip_address>:8080/sdc2/rest/healthCheck`

Another method to check about problems in SDC is to look at the log files.

The jetty(Applicative) are found in the respective folder according to the wanted section
For example, the BE logs will found under the directory `/BE`.

For more information regarding SDC Troubleshooting please refer to the following guide: [SDC Troubleshooting](https://wiki.onap.org/display/DW/SDC+Troubleshooting)

## Getting Help

#####  [Mailing list](mailto:onap-sdc@lists.onap.org)

##### [JIRA](http://jira.onap.org)

##### [WIKI](https://wiki.onap.org/display/DW/Service+Design+and+Creation+%28SDC%29+Portal)
