working_directory =  "/tmp"
cl_release=node['version'].split('.')[0..2].join('.').split('-')[0]
printf("\033[33mcl_release=[%s]\n\033[0m", cl_release)

execute "import-Conformance" do
  command "conf_dir=/tmp/sdctool/config && tosca_dir=/tmp/sdctool/tosca && cl_version=`grep 'toscaConformanceLevel:' $conf_dir/configuration.yaml |awk '{print $2}'` && cd /tmp/sdctool/scripts && /bin/chmod +x sdcSchemaFileImport.sh && echo \"execute /tmp/sdctool/scripts/sdcSchemaFileImport.sh ${tosca_dir} #{cl_release} ${cl_version} ${conf_dir} onap\" && ./sdcSchemaFileImport.sh ${tosca_dir} #{cl_release} ${cl_version} ${conf_dir} onap"
  cwd "#{working_directory}"
  action :run
end