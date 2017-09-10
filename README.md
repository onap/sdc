# OpenECOMP SDC

---
---


# Introduction

OpenECOMP SDC is delivered with 5 Docker containers:
1. sdc-FE	- frontend SDC application running on jetty server
2. sdc-BE	- backend SDC application running on jetty server
3. sdc-kbn	- hosting kibana application
4. sdc-cs	- hosting cassandra
5. sdc-es	- hosting elastic search

All containers runs on the same machine and can be started by runnin the command:
/data/scripts/docker_run.sh -e <environment name> -r <release> -p <docker-hub-port>
Example: /data/scripts/docker_run.sh -e OS-ETE-DFW -p 51220


# Compiling SDC

SDC is built from several projects, while "sdc-main" contains the main pom.xml for all project:
- catalog-be		- backend code
- catalog-fe		- frontend java code (servlet, proxy)
- catalog-dao		- database layer
- catalog-model		- data model of the application
- catalog-ui		- front end code (javascript, html, css)
- common-app-api	- common code for frontend and backend
- common-be			- utilities, datatypes and enums
- security-utils	- handle encryption/decryption of passwords

SDC projects can be compiled easily using maven command: `mvn clean install`. 
In order to build all projects, enter to sdc-main project and run the command: `mvn clean install`.
By default unit test will run when compiling

** igor **
Docker containers are build with the following profile 
`-P docker -Ddocker.buildArg.chef_repo_branch_name=bugfix/external_adress -Ddocker.buildArg.chef_repo_git_username=git -Ddocker.buildArg.chef_repo_address=23.253.149.175/SDC -Ddocker.buildArg.chef_repo_git_name=chef-repo`


# Getting the containers

***to be changed for rrelease*** OpenECOMP SDC containers are stored on the Rackspace Nexus Docker Registry

The following Docker images are the actual deployment images used for running SDC

| Name    | Tag       | Description                                                                                                                   |
|---------|-----------|-------------------------------------------------------------------------------------------------------------------------------|
| sdc-FE  | 1610.2.16 | Contains Jetty + OpenJDK + SDC frontend code + **3rd party jars**                                                             |
| sdc-BE  | 1610.2.16 | Contains Jetty + OpenJDK + SDC backend code + **3rd party jars**                                                              |
| sdc-kbn | 1610.2.16 | Contains nodeJs + Kibana application                                                                                          |
| sdc-cs  | 1610.2.16 | OpenJDK + Contains cassandra application                                                                                      |
| sdc-es  | 1610.2.16 | Elastic search application                                                                                                    |


*********************** Israel ************************
# Starting SDC
There are several ways to start OpenECOMP SDC:
TBD - Israel

# Accessing SDC
SDC UI can be accessed from:

### Ecomp portal
Login to ecomp portal URL with user that has permission for SDC application.
Define in your hosts file the following:
<ip address of SDC application> sdc.api.simpledemo.openecomp.org
<ip address of Ecomp portal URL> portal.api.simpledemo.openecomp.org
Open browser and navigate to: http://portal.api.simpledemo.openecomp.org:8989/ECOMPPORTAL/login.htm

### Webseal/SDC simulator
This options is for developers to run locally SDC

# SDC Simulator

This options is for developers to run locally SDC
SDC Simulator is a project that enables emulation of web server that provides security policy and sign-on to the SDC component in dev environments.

  - Provides sign on to the basic user roles/functionalities
  - Creation of basic user accounts

# Docker compilation - Docker Maven Build Profile (io.fabric8 maven Plugin)

If you are using onap vagrant you can deploy the simulator by:

Set up the DOCKER_HOST environmental variable

To set environmental variable in Windows (the docker engine environment):
- Run `cmd`
-- Issue command `set NAME=VAL
Example: set DOCKER_HOST=tcp://127.0.0.1:2375
--To check if the variable set succeeded issue `echo %DOCKER_HOST%`

- To compile sdc-simulator docker:
1. Run `mvn clean install -Ddocker.buildArg.http_proxy=<http_proxy> -Ddocker.buildArg.https_proxy=<https_proxy> -P docker`
-- The proxy arguments are passed and used as environmental variables in Dockerfiles 
2. Copy the script /webseal-simulator/scripts/simulator_docker_run.sh to the docker engine environment and run:
`simulator_docker_run.sh -r 1.1-STAGING-latest`
3. Run `docker ps` to verify that sdc-simulator docker is up and running.
4. Enter to UI: `http://<ip address>:8285/login`

# Docker compilation - Docker Engine

1. Build web simulator WAR file: run `mvn clean install` on project â€œwebseal simulatorâ€�. This will generate war file (WSSimulator.war) in the target folder.
2. Ftp war file: webseal-simulator/sdc-simulator folder to your localhost vagrant machine which runs docker engine daemon.
-- Check that WSSimulator.war exists after first step No.1 in webseal-simulator/sdc-simulator folder.
3. Run `docker build -t openecomp/sdc-simulator:1.1-STAGING-latest <PATH/sdc-simulator>`
Example: docker build -t openecomp/sdc-simulator:1.1-STAGING-latest /tmp/docker/sdc-simulator/
-- If running behind a proxy:
`docker build --build-arg http_proxy=http://URL:PORT --build-arg https_proxy=http://URL:PORT -t openecomp/sdc-simulator:1.1-STAGING-latest /tmp/docker/sdc-simulator/`
4. Validate that images pushed to the local repo by executing `docker images`
5. Copy the script /webseal-simulator/scripts/simulator_docker_run.sh to the docker engine environment and run: `simulator_docker_run.sh -r 1.1-STAGING-latest`
6. Run `docker ps` to verify that sdc-simulator docker is up and running.
7. Enter to UI: `http://<ip address>:8285/login`

# WAR compilation

  - To compile WSSimulator.war:
1. Build web simulator WAR file: run `mvn clean install` on project "webseal simulator". This will generate war file (WSSimulator.war) in the target folder.
2. Ftp war file: webseal-simulator\target\WSSimulator.war to your localhost vagrant machine: /home/vagrant/webseal-simulator/webapps folder
3. Ftp configuration file: webseal-simulator\src\main\resources\webseal.conf to your localhost vagrant machine: /home/vagrant/webseal-simulator/config
4. Add users to simulator: open configuration file - webseal.conf and add new user to the user list.
   Note: You need to define the user in the SDC as well.
5. To run the simulator, enter to your local vagrant and run: startWebsealSimulator.sh
-- Restart the simulator:
   Stop the simulator: stopWebsealSimulator.sh
   Start the simulator: startWebsealSimulator.sh
6. Enter to UI: http://<ip address>:8285/login


### SDC import normatives from CLI
SDC needs to work with predefined basic normatives, in order to update the database with the normatives need to:
1. From catalog-be project copy:
   src\main\resources\import\tosca -> to <machine ip address>:catalog-be/import/tosca
   src\main\resources\scripts\import\tosca ->to <machine ip address>:catalog-be/scripts/import/tosca
2. cd catalog-be/scripts/import/tosca
3. Run: python importNormativeAll.py
4. Wait until all normatives are loaded to the database


### SDC APIs
TBD

##### Main API endpoints in the first open source release 

- ***to be completed*** APIHandler health checks
TBD

# Configuration of SDC
TBD

Here are the main parameters you could change: 
TBD

The credentials are defined in 2 places:
TBD

# Logging
TBD

### Jetty
TBD

### Debuging
TBD

# Testing SDC Functionalities
TBD

### Frontend Local Env - onboarding

Steps:
------
Install nodejs & gulp
1. download nodejs from here: https://nodejs.org/en/ (take the "current" version with latest features) & install it.
2. install gulp by running the following command: npm install --global gulp-cli

Install DOX-UI a:
-----------------
1. pull for latest changes
2. go to folder dox-sequence-diagram-ui
3. run npm install
4. wait for it...
5. go to folder dox-ui
6. run npm install
7. create a copy of devConfig.defaults.json file and name it devConfig.json (we already configured git to ignore it so it will not be pushed)
8. in that file, populate the fields of the IP addresses of your BE machine you'd like to connect (pay attention, it is a JSON file): For example http://<host>:<port>
9. after everything was successful, run gulp
10. after server was up, your favorite UI will wait for you at: http://localhost:9000/sdc1/proxy-designer1#/onboardVendor

Troubleshooting:
----------------
| Problem                       |   Why is this happening | Solution                                                                                   |
--------------------------------------------------------------------------------------------------------------------------------------------------------
| npm cannot reach destination  | onboarding proxy        | When within onboarding network, you should set onboarding proxy to NPM as the following:   |
|                               |                         | npm config set proxy http://genproxy:8080                                                  |
|                               |                         | npm config set https-proxy http://genproxy:8080                                            |
|                               |                         |                                                                                            |
| git protocol is blocked       | onboarding network      | When within onboarding network, you should set globally that when git                      |
| and cannot connect            | rules for protocols     | protocol is used, then it will be replaced with "https"                                    |
|                               |                         | git config --global url."https://".insteadOf git://                                        |
--------------------------------------------------------------------------------------------------------------------------------------------------------

# Getting Help

*** to be completed on rrelease ***

SDC@lists.openecomp.org

SDC Javadoc and Maven site
 
*** to be completed on rrelease ***

