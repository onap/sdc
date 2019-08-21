directory "SDC_Simulator_tempdir_creation" do
  path "#{ENV['JETTY_BASE']}/temp"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "SDC_Simulator_create_config_dir" do
  path "#{ENV['JETTY_BASE']}/config"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "SDC_Simulator_create_sdc-simulator" do
  path "#{ENV['JETTY_BASE']}/config/sdc-simulator"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end
