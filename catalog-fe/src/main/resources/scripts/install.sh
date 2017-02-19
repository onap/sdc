#!/bin/sh

export JETTY_BASE=/home/jetty/base

COMP=$1

function usage() {
	echo "$0 <fe | be>"
}

function exitOnError() {
	if [ $1 -ne 0 ]
	then
		echo "Failed running task $2"
		exit 2
	fi
}

if [ $# -ne 1 ]
then
	usage
	exit 1

fi

/opt/app/sdc/catalog-${COMP}/scripts/installJettyBase.sh
exitOnError $? "installJettyBase"

cd ${JETTY_BASE}
exitOnError $? "move_to_base_dir"

mkdir -p scripts

cp /opt/app/sdc/catalog-${COMP}/scripts/* scripts
exitOnError $? "copy_scripts_from_rpm"

cp /opt/app/sdc/catalog-${COMP}/ext/jetty-ipaccess.xml etc
exitOnError $? "override_jetty-ipaccess_module."

cp /opt/app/sdc/catalog-${COMP}/catalog-${COMP}-*.war webapps
exitOnError $? "copy_war"

cp /opt/app/sdc/catalog-${COMP}/scripts/startJetty.sh .
exitOnError $? "copy_startJetty"

cp /opt/app/sdc/catalog-${COMP}/scripts/jvm.properties .
exitOnError $? "copy_jvm_properties"

./scripts/updateSslParams.sh ${JETTY_BASE}
exitOnError $? "updateSslParams_script"
	 
#ONLY FOR BE	 
#cp /opt/app/sdc/config/catalog-${COMP}/elasticsearch.yml config
#exitOnError $? "copy_elasticsearch_yaml_to_config"

mkdir -p ${JETTY_BASE}/config/catalog-${COMP}
cp -r /opt/app/sdc/config/catalog-${COMP}/*.xml ${JETTY_BASE}/config/catalog-${COMP}
exitOnError $? "copy_xml_files_to_config"

cp -r /opt/app/sdc/config/catalog-${COMP}/*.yaml ${JETTY_BASE}/config/catalog-${COMP}
exitOnError $? "copy_yaml_files_to_config"
