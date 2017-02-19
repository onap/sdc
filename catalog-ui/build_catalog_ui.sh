
#!/bin/sh

### Set the node environment.
#NODE_VERSION="v6.2.2"

#echo "Set the node environment."
#. "$NVM_DIR/nvm.sh"
#echo "OK."
#echo ""



### Add newer c++ compiler.
#if [ -f /opt/rh/devtoolset-4/enable ]; then
#  . /opt/rh/devtoolset-4/enable
#fi




### Set the node version manager version.
#echo "Set the node version manager version."
#nvm use ${NODE_VERSION}
#echo "OK."
#echo ""


### Run install bower.
echo "Run install bower."
#if [ -e $NVM_DIR/versions/node/${NODE_VERSION}/lib/node_modules/bower/bin/bower ]; then
#	echo " - bower is already installed."
#else
npm install bower
#fi
#echo "OK."
#echo ""



### Run install grunt-cli.
echo "Run install grunt-cli."
#if [ -e $NVM_DIR/versions/node/${NODE_VERSION}/lib/node_modules/grunt-cli/bin/grunt ]; then
#	echo " - grunt-cli is already installed."
#else
npm install grunt-cli
#fi
#echo "OK."
#echo ""



### Clean the Node cache.
#echo "Clean the Node cache - if stuck."
#npm cache clean
#echo "OK."
#echo ""



### Run the Node package manager (NPM).
echo "Run the Node package manager (NPM)."
#npm config set proxy http://one.proxy.att.com:8080
#npm config set https-proxy http://one.proxy.att.com:8080
#npm config set registry https://registry.npmjs.org

npm install
echo "OK."
echo ""



### Install the Bower components.
echo "Install the Bower components."
bower install
echo "OK."
echo ""



### Build the application.
echo "Build the application."
grunt build

