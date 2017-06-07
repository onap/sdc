#!/bin/sh



### Set the node environment.
NODE_VERSION="v6.10.0"



### Set the NVM root dir
NVM_DIR=/home/${USER}/.nvm



echo "Set the node environment."
. "${NVM_DIR}/nvm.sh"
echo "OK."
echo ""



### Add newer c++ compiler.
if [ -f /opt/rh/devtoolset-4/enable ]; then
  . /opt/rh/devtoolset-4/enable
fi



### Set the node version manager version.
echo "Set the node version manager version."
nvm use ${NODE_VERSION}
echo "OK."
echo ""



### Run the Node package manager (NPM).
echo "Run the Node package manager (NPM)."
npm install
echo "OK."
echo ""



### Build the application.
echo "Build the application."
npm run build:prod
echo "OK."
echo ""

