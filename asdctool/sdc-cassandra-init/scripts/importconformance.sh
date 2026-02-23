#!/bin/sh

# Accept sdctool base directory as optional first argument,
# defaulting to /home/sdc/sdctool (the path inside the Docker image).
sdctool_base="${1:-/home/sdc/sdctool}"

# Extract the cl_release version dynamically from the asdctool jar filename
# The jar is named asdctool-<version>-jar-with-dependencies.jar
sdctool_lib="$sdctool_base/lib"
version=$(ls "$sdctool_lib"/asdctool-*-jar-with-dependencies.jar 2>/dev/null \
    | sed 's|.*/asdctool-||;s|-jar-with-dependencies.jar||' \
    | head -1)

if [ -z "$version" ]; then
    echo "ERROR: Could not determine SDC version from asdctool jar in $sdctool_lib" >&2
    exit 1
fi

cl_release=$(echo $version | cut -d. -f1-3 | cut -d- -f1)
printf "\033[33mcl_release=[$cl_release]\033[0m\n"

# Execute the import-Conformance command
conf_dir="$sdctool_base/config"
tosca_dir="$sdctool_base/tosca"
cl_version=$(grep 'toscaConformanceLevel:' $conf_dir/configuration.yaml | awk '{print $2}')

cd "$sdctool_base/scripts"
chmod +x sdcSchemaFileImport.sh

echo "execute $sdctool_base/scripts/sdcSchemaFileImport.sh ${tosca_dir} ${cl_release} ${cl_version} ${conf_dir} onap"
./sdcSchemaFileImport.sh ${tosca_dir} ${cl_release} ${cl_version} ${conf_dir} onap
