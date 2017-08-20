jetty_base="/var/lib/jetty"

directory "SDC_Simulator_tempdir_creation" do
  path "#{jetty_base}/temp"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "SDC_Simulator_create_config_dir" do
  path "#{jetty_base}/config"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "SDC_Simulator_create_sdc-simulator" do
  path "#{jetty_base}/config/sdc-simulator"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end
