#!/bin/bash

FILE='/data/logs/docker_stats.out'
FE_ID=`docker ps|grep sdc-front|awk '{print $1}'`
BE_ID=`docker ps|grep sdc-back |awk '{print $1}'`

echo `date` >> ${FILE}

if [ ! -z "${FE_ID}" ]; then
   docker stats ${FE_ID} --no-stream >> /data/logs/docker_stats.out
else
   echo "frontend Docker is down!!!" >> /data/logs/docker_stats.out
fi

if [ ! -z "${BE_ID}" ]; then
   docker stats ${BE_ID} --no-stream >> /data/logs/docker_stats.out
else
   echo "backend Docker is down!!!" >> /data/logs/docker_stats.out
fi

echo "------------------------------------------" >>  ${FILE}

grep -v "^\-" ${FILE} |grep -v ^CONT| awk 'BEGIN {
    split("Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec",month)
    for (i in month) {
        month_nums[month[i]]=i
    }
}
/UTC/ {
  d=$6"-"$2"-"substr("00",0,2-length($3))$3"-"substr($4,0,5)
  next
}
/GiB/ {
   print $1" "d" "$3" "$8
   next
}' > `echo ${FILE}|awk -F"." '{ print $1".csv"}'`

