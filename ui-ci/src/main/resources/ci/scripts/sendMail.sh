#!/bin/bash

now=$(date +'%Y%-m%d%H%M')

REPORT_NAME=$1
VERSION=$2
ENV=$3

RECIPIENTS1="dl-sdcqa@att.com,ml636r@att.com,bl5783@att.com,ak314p@att.com,el489u@att.com,hk096q@att.com,bs5719@att.com"
#RECIPIENTS2="dl-asdcqa@att.com"
RECIPIENTS2="md9897@att.com,ms656r@att.com,al714h@att.com,ak991p@att.com,ya107f@att.com,bv095y@att.com,st198j@att.com,th0695@att.com,vk195d@att.com,gg980r@att.com,il0695@att.com,el489u@att.com"

source ExtentReport/versions.info
if [ -z "$REPORT_NAME" ]
 then
        source ExtentReport/versions.info
        now=$(date +'%Y-%m-%d_%H_%M')
        REPORT_NAME="${now}"
        VERSION="${osVersion}"
               

fi

if [[ $env == *"DEV20"* ]]
    then
         ENV="Nightly"
         RECIPIENTS=$RECIPIENTS1
    else
         ENV=""
         RECIPIENTS=$RECIPIENTS2
fi

#REPORT_ZIP_FILE=ExtentReport_${now}.zip
REPORT_FOLDER='ExtentReport'
REPORT_HTML_FILE=${REPORT_FOLDER}/*Report.html
BODY_MESSAGE='Hello, \n\n Please find automation results on following link: \n\n http://asdc-srv-210-45.tlv.intl.att.com/'${ENV}'/'${VERSION}'/UI/'${REPORT_NAME}'/SDC_UI_Extent_Report.html \n\nThanks, \nASDC QA Team\n\n '

#OLD_FILE=$(find ./ -type f -name ${REPORT_ZIP_FILE} -print)
#if [ ! -z ${OLD_FILE} ]
#then
#        rm -f ${REPORT_ZIP_FILE}
#        echo "Removing old zip file............"
#fi

#echo "Creating new zip file"
#zip -r ${REPORT_ZIP_FILE} ./${REPORT_FOLDER}



echo -e ${BODY_MESSAGE} | mail -s 'E2E Automation '$ENV' results - SDC '$VERSION -r 'ASDC@Automation.team' $RECIPIENTS
