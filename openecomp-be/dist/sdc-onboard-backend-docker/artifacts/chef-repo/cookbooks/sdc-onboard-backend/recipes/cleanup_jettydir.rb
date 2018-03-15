directory "tempdir_creation" do
  path "#{ENV['JETTY_BASE']}/temp"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end

directory "create_config_dir" do
  path "#{ENV['JETTY_BASE']}/config"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end

directory "onboarding-be" do
  path "#{ENV['JETTY_BASE']}/config/onboarding-be"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end
