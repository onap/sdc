jetty_base=ENV['JETTY_BASE']

directory "FE_tempdir_creation" do
  path "/#{jetty_base}/temp"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end

directory "FE_create_config_dir" do
  path "/#{jetty_base}/config"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "FE_create_catalog-fe" do
  path "/#{jetty_base}/config/catalog-fe"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "FE_create_catalog-fe" do
  path "/#{jetty_base}/config/onboarding-fe"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end