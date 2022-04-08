# ASDC - Amdocs Onboard UI App

## Setup

###Install `npm`

Install npm v8.6.0.

###Install `Node.js`

Install node v14.17.1 (as in the pom.xml <nodeVersion>v14.17.1</nodeVersion>).

* To manage different versions of node, it is recommended to install "n" (https://github.com/tj/n).

### Install `gulp`

install gulp by running the following command `npm install --global gulp-cli`

## Build

### Install DOX-UI
* pull for latest changes
* go to folder `../dox-sequence-diagram-ui`
* run `npm install && npm run build`

#### Install onboarding-fe
* go to the current project folder `openecomp-ui`
* run `npm install`
* create a copy of `devConfig.defaults.json` file and name it `devConfig.json` (it is already configured to gitignore, so it will not be pushed)
* in `devConfig.json`:
  * set "proxyCatalogTarget" to the URL of the sdc-frontend; set "proxyTarget" to the URL of the sdc-onboard-backend (**pay attention, it is a JSON file**):

    For example *http://\<host>:\<port>*
* run `npm start`
* your favorite UI will wait for you at: `http://localhost:9000/sdc1/#!/onboardVendor`

## Troubleshooting
| Problem                                    | Why is this happening                | Solution                                                                                                                                                                                |
|--------------------------------------------|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Build (npm install) error                  | npm/node_modules cache               | If having problems with the compilation of  dox-sequence-diagram-ui and openecomp-ui, delete the node_modules and package-lock.json in each respective projects folder.                 |
| npm cannot reach destination               | proxy                                | When within managed network, you should set your proxy to NPM as the following: <br> `npm config set proxy http://<host>:<port>` <br> `npm config set https-proxy http://<host>:<port>` |
| git protocol is blocked and cannot connect | managed network rules for protocols	 | When within managed network, you should set globally that when git protocol is used, then it will be replaced with "https" <br> `git config --global url."https://".insteadOf git://`   |
