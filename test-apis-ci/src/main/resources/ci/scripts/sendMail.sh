#!/bin/bash

now=$(date +'%Y%-m%d%H%M')

REPORT_NAME=$1
VERSION=$2
ENV=$3

RECIPIENTS1="md9897@att.com,NETCOM_ASDC_DEV@att.com,IL-D2-QA-Alex@att.com,bs5719@att.com,yg356h@att.com,yr9970@att.com,sl615n@att.com,yn813h@att.com,as221v@att.com,ms172g@att.com,ma2244@att.com,el489u@att.com,gg980r@att.com,ak991p@att.com,bv095y@att.com,ms656r@att.com,df502y@att.com,bt750h@att.com,ln699k@att.com,hm104p@att.com"
#RECIPIENTS2="dl-asdcqa@intl.att.com"
RECIPIENTS2="md9897@att.com,NETCOM_ASDC_DEV@att.com,IL-D2-QA-Alex@att.com,bs5719@att.com,yg356h@att.com,yr9970@att.com,sl615n@att.com,yn813h@att.com,as221v@att.com,ms172g@att.com,ma2244@att.com,el489u@att.com,gg980r@att.com,ak991p@att.com,bv095y@att.com,ms656r@att.com,df502y@att.com,bt750h@att.com,ln699k@att.com,hm104p@att.com"
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
