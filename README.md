# ONAP SDC

## Introduction

SDC is the ONAP visual modeling and design tool. It creates internal metadata that describes assets used by all ONAP components, both at design time and run time.

The SDC manages the content of a catalog, and logical assemblies of selected catalog items to completely define how and when VNFs are realized in a target environment. 
A complete virtual assembly of specific catalog items, together with selected workflows and instance configuration data, completely defines how the deployment, activation, and life-cycle management of VNFs are accomplished.

SDC manages four levels of assets:

* Resource - A fundamental capability, implemented either entirely in software, or as software that interacts with a hardware device. 
Each Resource is a combination of one or more Virtual Function Components (VFCs), along with all the information necessary to instantiate, update, delete, and manage the Resource. 
* Service - A well formed object comprising one or more Resources. Service Designers create Services from Resources, and include all of the information about the Service needed to instantiate, update, delete, and manage the Service

The key output of SDC is a set of models containing descriptions of asset capabilities and instructions to manage them. These models are stored in the SDC Master Reference Catalog for the entire enterprise to use.

There are four major components of SDC:

* Catalog - The repository for assets at the Resource, Service and Product levels. Assets are added to the Catalog using the Design Studio.
* Design Studio - Used to create, modify, and add Resource, Service, and Product definitions in the Catalog.
* Certification Studio - Available in a future release, is used to test new assets at all levels. It will be used for sandbox experimentation, and will include support for automated testing.
* Distribution Studio - Used to deploy certified assets. From the Distribution studio, new Product assets, including their underlying Resources and Services, are deployed into lab environments for testing purposes, and into production after certification is complete. In a future release, there will be a way to export Product information to external Business Support Systems for customer ordering and billing.

## Compiling the Project

SDC is built from several projects, while "sdc-main" contains the main pom.xml for all project:
- asdctool          - set of utilities used for scheme creation and data migration in SDC
- catalog-be		- backend code
- catalog-fe		- frontend java code (servlet, proxy)
- catalog-dao		- database layer
- catalog-model		- data model of the application
- catalog-ui		- front end code (javascript, html, css)
- common            - set of utilities used by the onboarding project
- common-app-api	- common code for frontend and backend
- common-be			- utilities, datatypes and enums
- security-utils	- handle encryption/decryption of passwords
- onboarding-be     - onboarding backend code
- onboarding-ui     - onboarding frontend code
- test-apis-ci      - the automation framework used by SDC for API based testing
- ui-ci             - the automation framework used by SDC for UI based testing, Based on selenium
- sdc-os-chef       - chefs scripts used for docker creation and startup
- utils             - set of dev utils used for working with the project locally

In order to build all the projects, go to sdc-main project and run the command: `mvn clean install`

Currently SDC build process also supports docker building.
In order to build and upload local dockers to a local environment you'll need to define an environment variable on your machine with key: `DOCKER_HOST` and value: `tcp://<vagrant_ip_address>:2375`
For the dockers to be built during the build process use the "docker" profile by adding this: `-P docker` to the `mvn clean install` command

More flags to use in the build process are:
* -DskipTests - Skips unit tests execution
* -DskipUICleanup=true - Skips deleting the UI folders 
* -Djacoco.skip=true - Skips running jacoco tests
* -DskipPMD - Skips creating a PMD report

**using those flags will speed up the building process of the project**

## Accessing SDC

In order to access the sdc from you're local vagrant environment you'll need to run the webseal_simulator docker.
This can be achieved by using the command: `/data/scripts/simulator_docker_run.sh`

to Access the simulator just go to this url: `http://<vagrant_ip_address>:8285/login`

For more information regarding using the webseal_simulator please refer to the following guide: [SDC Simulator](https://wiki.onap.org/display/DW/SDC+Simulator)

## Testing the Project

When building SDC dockers part of the dockers that will be built are the dockers that responsible for running automation tests in the local environment.

In order to run the automation tests when starting the dockers on the machine just add the flag "-t" to the docker run command.

You can go to this link to view all the commands in the vagrant-onap: [Vagrant Common Commands](https://wiki.onap.org/display/DW/SDC+Vagrant+Common+Commands)

And to this guide regarding using the docker run script: [SDC docker_run Script Usage](https://wiki.onap.org/display/DW/SDC+docker_run+Script+Usage)

For more information regarding testing the project please refer to the following guide: [SDC Sanity](https://wiki.onap.org/display/DW/SDC+Sanity)

For more information regarding SDC Troubleshooting please refer to the following guide: [SDC Troubleshooting](https://wiki.onap.org/display/DW/SDC+Troubleshooting)

## SDC on OOM

For more information regarding SDC on OOM please refer to the following page: [SDC on OOM](https://wiki.onap.org/display/DW/SDC+on+OOM)

## Troubleshooting

In order to check the life state of SDC you can run the command `health` from inside the vagrant.
Alternatively you can run the following commands to check the FE and BE status:

FE - `curl http://localhost:8181/sdc1/rest/healthCheck`

BE - `curl http://localhost:8080/sdc2/rest/healthCheck`

Another method to check about problems in SDC in be looking in the log files.
The log files of the SDC can be found in the `/data/logs` folder

The docker logs are found under the directory `/docker_logs`.

The jetty(Applicative) are found in the respective folder according to the wanted section
For Example, the BE logs will found under the directory `/BE`.

## Getting Help

#####  [Mailing list](mailto:onap-sdc@lists.onap.org)

##### [JIRA](http://jira.onap.org)

##### [WIKI](https://wiki.onap.org/display/DW/Service+Design+and+Creation+%28SDC%29+Portal)