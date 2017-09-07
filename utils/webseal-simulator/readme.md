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

1. Build web simulator WAR file: run `mvn clean install` on project “webseal simulator”. This will generate war file (WSSimulator.war) in the target folder.
2. Ftp war file: webseal-simulator/sdc-simulator folder to your localhost vagrant machine which runs docker engine daemon.
-- Check that WSSimulator.war exists after first step No.1 in webseal-simulator/sdc-simulator folder.
3. Run `docker build -t openecomp/sdc-simulator:1.1-STAGING-latest <PATH/sdc-simulator>`
Example: docker build -t openecomp/sdc-simulator:1.1-STAGING-latest /tmp/docker/sdc-simulator/
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
