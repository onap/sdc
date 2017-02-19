#!/bin/sh

DEBUG=0

function cdAndExit() {
	cd - >> /dev/null
	exit $1
}

function usage() {
	echo "$0 <envronment file>"
	exit 3
}


if [ $# -ne 1 ]
then
	usage
fi

envFile=$1

echo $envFile

source $envFile

#source ./envCatalogBE.sh

echo "******************************************"
echo "********* CATALOG_FE_HOST    $CATALOG_FE_HOST            "
echo "********* CATALOG_FE_PORT    $CATALOG_FE_PORT            " 
echo "********* CATALOG_BE_HOST    $CATALOG_BE_HOST            "
echo "********* CATALOG_BE_PORT    $CATALOG_BE_PORT            " 
echo "******************************************"

if [[ ${DEBUG} -eq 1 ]]
then
	read stam
fi 

for folder in *
do

if [[ -d $folder && $folder != "env" ]]
then

echo "processing folder" $folder
cd $folder

commandResult=`./command`

if [ $? -ne 0 ]
then
	 echo "command $folder failed"
	 cdAndExit 2
fi

echo "command result is $commandResult"
results=`cat results`

echo "Going to match $commandResult with pattern $results"

matchNumber=`echo $commandResult | grep -c "$results" `
echo $matchNumber
if [ $matchNumber -eq 1 ]
then
	echo "command $folder succeed."
else
	echo "command $folder failed. Going to exit"
	cdAndExit 1	
	
fi

echo "Finish processing folder $folder"
echo "********************************************************"

cd - >> /dev/null

#if folder
fi

#loop on folder
done

echo "***************************************"
echo "*       SUCCESS ${envFile}            *"
echo "***************************************"



