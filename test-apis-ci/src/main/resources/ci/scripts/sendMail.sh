#!/bin/bash

now=$(date +'%Y%-m%d%H%M')

REPORT_NAME=$1
VERSION=$2
ENV=$3

RECIPIENTS1="dl-sdcqa@intl.att.com,ml636r@intl.att.com,bl5783intl.att.com,ak314p@intl.att.com,el489u@intl.att.com,hk096q@intl.att.com,bs5719@intl.att.com"
RECIPIENTS2="dl-asdcqa@intl.att.com"

source ExtentReport/versions.info
if [ -z "$REPORT_NAME" ]
 then
        now=$(date -d '+7 hour' "+%Y-%m-%d_%H_%M")
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
BODY_MESSAGE='Hello, \n\n Please find automation results on following link: \n\n http://asdc-srv-210-45.tlv.intl.att.com/'${ENV}'/'${VERSION}'/APIs/'${REPORT_NAME}'/SDC_CI_Extent_Report.html \n\nThanks, \nSDC QA Team\n\n '

#OLD_FILE=$(find ./ -type f -name ${REPORT_ZIP_FILE} -print)
#if [ ! -z ${OLD_FILE} ]
#then
#        rm -f ${REPORT_ZIP_FILE}
#        echo "Removing old zip file............"
#fi

#echo "Creating new zip file"
#zip -r ${REPORT_ZIP_FILE} ./${REPORT_FOLDER}


echo -e ${BODY_MESSAGE} | mail -s 'External APIs Automation '$ENV' results - ASDC '$VERSION -r 'ASDC@Automation.team' $RECIPIENTS
