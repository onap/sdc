# ASDC - Amdocs Onboard UI App

## Setup

##### Install `nodejs`:
			
download nodejs from here: https://nodejs.org/en/ (take the "current" version with latest features) & install it.
##### Install `gulp`

install gulp by running the following command `npm install --global gulp-cli`

##### Install DOX-UI
* pull for latest changes
* go to folder `dox-sequence-diagram-ui`
* run `npm install`
* wait for it...
* go to folder `openecomp-ui`
* run `npm install`
* create a copy of `devConfig.defaults.json` file and name it `devConfig.json` (we already configured git to ignore it so it will not be pushed)
in that file.
  
  populate the fields of the IP addresses of your BE machine you'd like to connect (**pay attention, it is a JSON file**):
  
  For example *http://\<host>:\<port>* 
* run `npm start`
* your favorite UI will wait for you at: `http://localhost:9000/sdc1/proxy-designer1#/onboardVendor`



#### Troubleshooting
Problem | Why is this happening | Solution
------- | --------------------- | --------
npm cannot reach destination | proxy | When within managed network, you should set your proxy to NPM as the following: <br> `npm config set proxy http://<host>:<port>` <br> `npm config set https-proxy http://<host>:<port>`
git protocol is blocked and cannot connect | managed network rules for protocols	| When within managed network, you should set globally that when git protocol is used, then it will be replaced with "https" <br> `git config --global url."https://".insteadOf git://`
