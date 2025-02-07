#!/bin/sh

# Set the working directory
working_directory="/home/sdc"

# Extract the cl_release version
version="1.14.0"
cl_release=$(echo $version | cut -d. -f1-3 | cut -d- -f1)
echo -e "\033[33mcl_release=[$cl_release]\033[0m"

# Execute the import-Conformance command
conf_dir="/home/sdc/sdctool/config"
tosca_dir="/home/sdc/sdctool/tosca"
cl_version=$(grep 'toscaConformanceLevel:' $conf_dir/configuration.yaml | awk '{print $2}')

cd /home/sdc/sdctool/scripts
chmod +x sdcSchemaFileImport.sh

echo "execute /home/sdc/sdctool/scripts/sdcSchemaFileImport.sh ${tosca_dir} ${cl_release} ${cl_version} ${conf_dir} onap"
./sdcSchemaFileImport.sh ${tosca_dir} ${cl_release} ${cl_version} ${conf_dir} onap
