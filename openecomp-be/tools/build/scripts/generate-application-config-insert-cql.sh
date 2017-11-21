#/bin/bash

##############################################################################
###
### generate-application-config-insert-cql.sh
###
### A script that generates the CQL commands to INSERT validation schemas to the application_config table.
### We keep the schemas FTL files under a folder - this folder will be parsed and INSERT commands will be created.
###
### If the path is 'schemaTemplates/composition/myFile.ftl' the result KEY will be: composition.myFile .
###
### Usage:
###
###    ./generate-application-config-insert-cql.sh <namespace> <schemas-folder>
###
###
### Author: Avi Ziv
### Version 1.0
### Date: 10 Aug 2016
###
##############################################################################

#GLOBALS

APP_CONFIG_TABLE='application_config'

#### Functions - Start  ####
usage() { echo "Usage: $0 <namespace> <schemaTemplates-folder>, for example: $0 vsp.schemaTemplates schemaTemplates/" 1>&2; exit 1; }

getFileContent()
{
        file=$1
        str=$(<$file)
        echo $str
}


main()
{
        namespace=$1
        path=$2
        for fileName in $(find ${path} -type f)
        do
                value=$(getFileContent ${fileName})
                onlyFilename=$(basename $fileName)
                name="${onlyFilename%.*}"
                tempPath=$(dirname $fileName)
                keyColumn=$(basename $tempPath).$name
                echo "INSERT INTO $APP_CONFIG_TABLE (namespace,key,value) VALUES ('$namespace', '$keyColumn', '$value');"
        done


exit 0
}

#### Functions - End    ####

# Check arguements
if [ "$#" -lt 2 ] || [ "$#" -gt 2 ]; then
        usage
fi


main $1 $2
