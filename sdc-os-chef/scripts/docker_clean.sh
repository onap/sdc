pat=$1
docker_ids=`docker ps -a | grep ${pat} | awk '{print $1}'`
for X in ${docker_ids}
do
   docker rm -f ${X}
done
