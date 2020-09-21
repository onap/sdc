directory "SDC_Simulator_tempdir_creation" do
  path "#{ENV['JETTY_BASE']}/temp"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode '0755'
  action :create
end


directory "SDC_Simulator_create_config_dir" do
  path "#{ENV['JETTY_BASE']}/config"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode '0755'
  action :create
end


directory "SDC_Simulator_create_sdc-simulator" do
  path "#{ENV['JETTY_BASE']}/config/sdc-simulator"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode '0755'
  action :create
end
