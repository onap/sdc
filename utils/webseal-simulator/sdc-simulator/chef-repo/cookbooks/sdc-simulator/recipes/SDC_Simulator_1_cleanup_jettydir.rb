directory "SDC_Simulator_tempdir_creation" do
  path "/var/lib/jetty/temp"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "SDC_Simulator_create_config_dir" do
  path "/var/lib/jetty/config"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "SDC_Simulator_create_sdc-simulator" do
  path "/var/lib/jetty/config/sdc-simulator"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


#directory "SDC_Simulator_tempdir_cleanup" do
#  path "/var/lib/jetty/tempdir"
#  recursive true
#  action :delete
#end


#directory "SDC_Simulator_webapps_cleanup" do
#  path "/var/lib/jetty/webapps"
#  recursive true
#  action :delete
#end


#directory "SDC_Simulator_webapps_creation" do
#  path "/var/lib/jetty/webapps"
#  owner 'jetty'
#  group 'jetty'
#  mode '0755'
#  action :create
#end


#directory "SDC_Simulator_create_catalog-fe" do
#  path "/var/lib/jetty/config/onboarding-fe"
#  owner 'jetty'
#  group 'jetty'
#  mode '0755'
#  action :create
#end