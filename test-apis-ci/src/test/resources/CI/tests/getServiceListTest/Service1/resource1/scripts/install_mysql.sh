#!/bin/bash

echo "Debian based MYSQL install 5..."
LOCK="/tmp/lockaptget"

while true; do
  if mkdir "${LOCK}" &>/dev/null; then
    echo "MySQL take the lock"
    break;
  fi
  echo "Waiting the end of one of our recipes..."
  sleep 0.5
done

while sudo fuser /var/lib/dpkg/lock >/dev/null 2>&1 ; do
  echo "Waiting for other software managers to finish..."
  sleep 0.5
done
sudo rm -f /var/lib/dpkg/lock

sudo apt-get update || (sleep 15; sudo apt-get update || exit ${1})
sudo DEBIAN_FRONTEND=noninteractive apt-get -y install mysql-server-5.5 pwgen || exit ${1}
rm -rf "${LOCK}"

sudo /etc/init.d/mysql stop
sudo rm -rf /var/lib/apt/lists/*
sudo rm -rf /var/lib/mysql/*
echo "MySQL Installation complete."