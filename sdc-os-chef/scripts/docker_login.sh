pat=$1
docker_ids=`docker ps | grep ${pat} | awk '{print $1}'|head -1`
if [ -z "$docker_ids" ]; then
   echo "No dockers were found matching pattern [${pat}]"
   exit 0
else 
   docker exec -it ${docker_ids} bash
fi
