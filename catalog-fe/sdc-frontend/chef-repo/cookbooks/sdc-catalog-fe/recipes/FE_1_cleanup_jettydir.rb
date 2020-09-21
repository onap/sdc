directory "FE_tempdir_creation" do
  path "#{ENV['JETTY_BASE']}/temp"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode '0755'
  action :create
end

directory "FE_create_config_dir" do
  path "#{ENV['JETTY_BASE']}/config"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode '0755'
  action :create
end


directory "FE_create_catalog-fe" do
  path "#{ENV['JETTY_BASE']}/config/catalog-fe"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode '0755'
  action :create
end


directory "FE_create_catalog-fe" do
  path "#{ENV['JETTY_BASE']}/config/onboarding-fe"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode '0755'
  action :create
end
