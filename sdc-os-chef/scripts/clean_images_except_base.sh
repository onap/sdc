docker rmi -f `docker images | grep -v base | awk '{print $3}'`
