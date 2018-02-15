jetty_base=ENV['JETTY_BASE']

directory "BE_tempdir_creation" do
  path "/#{jetty_base}/temp"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end

directory "BE_create_config_dir" do
  path "/#{jetty_base}/config"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "BE_create_catalog-be" do
  path "/#{jetty_base}/config/catalog-be"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end
