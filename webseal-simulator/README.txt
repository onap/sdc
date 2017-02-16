------------------------
|                      |
|  webseal simulator   |
|                      |
------------------------

Working with webseal simulator:
-------------------------------

1. Build the project using: mvn clean install
2. Ftp war file: webseal-simulator\target\WSSimulator.war to your localhost vagrant machine: /home/vagrant/webseal-simulator/webapps folder
3. Ftp configuration file: webseal-simulator\src\main\resources\webseal.conf to your localhost vagrant machine: /home/vagrant/webseal-simulator/config
4. To run the simulator, enter to your local vagrant and run: startWebsealSimulator.sh
5. Open browser and navigate to: http://localhost:8285/sdc1.login

Note: the user in webseal configuration file will appear in the login screen. Pressing on user link will perform authentication and redirect to SDC.
The users should be predefined in SDC

