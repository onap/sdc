# Ubuntu 14.04 don't have nsenter - the straight forward way required me to install build tools and etc.
# I preferred to keep the system clean and install nsenter in a container and then copy the command to the host
# Note - its also possible to run nsenter from a container (didn't tried) https://github.com/jpetazzo/nsenter

# start a container
docker run --name nsenter -it ubuntu:14.04 bash

## in the docker
apt-get update
apt-get install git build-essential libncurses5-dev libslang2-dev gettext zlib1g-dev libselinux1-dev debhelper lsb-release pkg-config po-debconf autoconf automake autopoint libtool bison

git clone git://git.kernel.org/pub/scm/utils/util-linux/util-linux.git util-linux
cd util-linux/

./autogen.sh
./configure --without-python --disable-all-programs --enable-nsenter
make

## from different shell - on the host
echo "[Action requires] From different shell on the host run the following:"
docker cp nsenter:/util-linux/nsenter /usr/local/bin/
docker cp nsenter:/util-linux/bash-completion/nsenter /etc/bash_completion.d/nsenter