#!/bin/bash

service="apache2"

if (( $(ps -ef | grep -v grep | grep $service | wc -l) > 0 ))
then
  sudo /etc/init.d/$service restart
else
  sudo /etc/init.d/$service start
fi
