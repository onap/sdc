# OpenECOMP SDC(UI)

---
---

# Steps

### Install nodejs & gulp

#### Download nodejs from here : https://nodejs.org/en/ (take the "current" version with latest features) & install it.
#### Install gulp by running the following command : npm install --global gulp-cli

### Install DOX-UI a

#### Pull for latest changes.
#### Go to folder dox-sequence-diagram-ui
#### Give the following command : run npm install
#### Wait for it..
#### Go to folder dox-ui
#### run npm install
#### Create a copy of devConfig.defaults.json file and name it devConfig.json (we already configured git to ignore it so it will not be pushed).
#### In that file, populate the fields of the IP addresses of your BE machine you'd like to connect (pay attention, it is a JSON file): For example, http://<host>:<port>
#### After everything was successful, run gulp.
#### After server was up, your favourite UI will wait for you at : http://localhost:9000/sdc1/proxy-designer1#/onboardVendor

