working_directory =  "/tmp"
cl_release="1.1.0"
printf("\033[33mcl_release=[%s]\n\033[0m", cl_release)

cookbook_file "/tmp/sdctool.tar" do
   source "sdctool.tar"
end

bash "extract sdctool.tar" do
  cwd "#{working_directory}"
  code <<-EOH
    tar xvf /tmp/sdctool.tar
  EOH
end

cookbook_file "/tmp/sdctool/config/SDC.zip" do
   source "SDC-#{cl_release}.zip"
end

bash "import-Comformance" do
  cwd "#{working_directory}"
  code <<-EOH
    conf_dir=/tmp/sdctool/config
    schema_file_name=SDC.zip

    cl_version=`grep 'toscaConformanceLevel:' $conf_dir/configuration.yaml |awk '{print $2}'`

    cd /tmp/sdctool/scripts
    /bin/chmod +x sdcSchemaFileImport.sh
    echo "execute /tmp/sdctool/scripts/sdcSchemaFileImport.sh $conf_dir/$schema_file_name #{cl_release} $cl_version $conf_dir "
    ./sdcSchemaFileImport.sh ${conf_dir}/${schema_file_name} $cl_release ${cl_version} ${conf_dir} 
  EOH
end



