# ONAP SDC

## Introduction

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

Before cloning the sdc source code it's important to enable long paths on your Windows machine, otherwise git won't be able to handle some files

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
- common		- set of utilities used by the onboarding project
- common-app-api	- common code for frontend and backend
- common-be		- utilities, datatypes and enums
- security-utils	- handle encryption/decryption of passwords
- onboarding-be		- onboarding backend code
- onboarding-ui		- onboarding frontend code
- test-apis-ci		- the automation framework used by SDC for API based testing
- ui-ci			- the automation framework used by SDC for UI based testing, based on Selenium
- sdc-os-chef		- chefs scripts used for docker creation and startup
- utils			- set of dev utils used for working with the project locally

In order to build all the projects, go to sdc project and run the command: `mvn clean install`

Currently SDC build process also supports docker building.
In order to build and upload local dockers to a local environment you'll need to define an environment variable on your machine with key: `DOCKER_HOST` and value: `tcp://<ip_address>:2375`
For the dockers to be built during the build process use the "docker" profile by adding this: `-P docker` to the `mvn clean install` command

More flags to use in the build process are:
* -DskipTests - Skips unit tests execution
* -DskipUICleanup=true - Skips deleting the UI folders
* -Djacoco.skip=true - Skips running jacoco tests
* -DskipPMD - Skips creating a PMD report

**using those flags will speed up the building process of the project**

## Accessing SDC

In order to access the sdc from your local vagrant environment you'll need to run the webseal_simulator docker.
This can be achieved by using the command: `/data/scripts/simulator_docker_run.sh`

To access the simulator just go to this url: `http://<ip_address>:8285/login`

For more information regarding using the webseal_simulator please refer to the following guide: [SDC Simulator](https://wiki.onap.org/display/DW/SDC+Simulator)

## Accessing SDC UI in Dev Mode

In order to access the SDC UI from your dev environment you need to do the following:

1. Go to file `webpack.server.js` found under the catalog-ui folder in the main sdc project and update the "localhost" variable to be the ip of your local vagrant machine.
2. Navigate to the catalog-ui folder and run the command: `npm start -- --env.role <wanted_role>` with the wanted role to login to SDC as.

## SDC Containers

The following table shows the SDC containers found on the vagrant after a successful build:

| Name                | Description                                                                                                                                   |
|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| sdc-cs              | The Docker contains our Cassandra server. On docker startup the Cassandra server is started.                                                  |
| sdc-cs-init         | The docker contains the logic for creating the needed schemas for SDC catalog server,  On docker startup, the schemes are created.            |
| sdc-cs-onboard-init | The docker contains the logic for creating the needed schemas for SDC onboarding server, On docker startup, the schemes are created.          |
| sdc-es              | The Docker contains Elastic Search server. On docker startup, Elastic Search server is started.                                               |
| sdc-init-es         | The Docker contains the logic for creating the needed mapping for SDC and the views for kibana. On docker startup, the mapping is created.    |
| sdc-onboard-BE      | The Docker contains the onboarding Backend Jetty server. On docker startup, the Jetty server is started with the application.                 |
| sdc-BE              | The Docker contains the catalog Backend Jetty server. On docker startup, the Jetty server is started with the application.                    |
| sdc-BE-init         | The docker contains the logic for importing the SDC Tosca normative types and the logic for configuring external users for SDC external APIs. |
|                     | On startup, the docker executes the rest calls to the catalog server.                                                                         |
| sdc-FE              | The Docker contains the SDC Fronted Jetty server. On docker startup, the Jetty server is started with our application.                        |

For further information and an image explaining the containers dependency map please refer to the following page: [SDC Docker Diagram](https://wiki.onap.org/display/DW/SDC+Troubleshooting)

## Testing the Project

The dockers that are responsible for running automation tests in the local environment are built as part of SDC docker profile build.

In order to run the automation tests when starting the dockers on the machine, there are 2 flags to use:

* -tad - Use this flag to run the default suite of API tests
* -tud - Use this flag to run the default suite of UI tests 

This link lists all the commands in the vagrant-onap: [Vagrant Common Commands](https://wiki.onap.org/display/DW/SDC+Vagrant+Common+Commands)

SDC docker_run script is documented here: [SDC docker_run Script Usage](https://wiki.onap.org/display/DW/SDC+docker_run+Script+Usage)

For more information regarding testing the project please refer to the following guide: [SDC Sanity](https://wiki.onap.org/display/DW/SDC+Sanity)

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
The log files of the SDC can be found in the `/data/logs` folder

The docker logs are found under the directory `/docker_logs`.

The jetty(Applicative) are found in the respective folder according to the wanted section
For example, the BE logs will found under the directory `/BE`.

For more information regarding SDC Troubleshooting please refer to the following guide: [SDC Troubleshooting](https://wiki.onap.org/display/DW/SDC+Troubleshooting)

## Getting Help

#####  [Mailing list](mailto:onap-sdc@lists.onap.org)

##### [JIRA](http://jira.onap.org)

##### [WIKI](https://wiki.onap.org/display/DW/Service+Design+and+Creation+%28SDC%29+Portal)
