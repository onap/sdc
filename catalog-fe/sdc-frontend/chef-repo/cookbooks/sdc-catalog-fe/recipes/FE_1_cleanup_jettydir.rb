directory "FE_tempdir_creation" do
  path "#{ENV['JETTY_BASE']}/temp"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end

directory "FE_create_config_dir" do
  path "#{ENV['JETTY_BASE']}/config"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "FE_create_catalog-fe" do
  path "#{ENV['JETTY_BASE']}/config/catalog-fe"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "FE_create_catalog-fe" do
  path "#{ENV['JETTY_BASE']}/config/onboarding-fe"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end