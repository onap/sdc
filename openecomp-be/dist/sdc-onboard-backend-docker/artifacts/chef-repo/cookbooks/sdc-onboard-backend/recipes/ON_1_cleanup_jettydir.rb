directory "tempdir_creation" do
  path "#{ENV['JETTY_BASE']}/temp"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode '0755'
  action :create
end

directory "create_config_dir" do
  path "#{ENV['JETTY_BASE']}/config"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode '0755'
  action :create
end

directory "onboarding-be" do
  path "#{ENV['JETTY_BASE']}/config/onboarding-be"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode '0755'
  action :create
end
