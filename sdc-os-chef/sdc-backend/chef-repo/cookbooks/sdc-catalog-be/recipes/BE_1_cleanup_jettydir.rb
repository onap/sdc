#directory "BE_tempdir_cleanup" do
#  path "/var/lib/jetty/tempdir"
#  recursive true
#  action :delete
#end


directory "BE_tempdir_creation" do
  path "/var/lib/jetty/temp"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


#directory "BE_webapps_cleanup" do
#  path "/var/lib/jetty/webapps"
#  recursive true
#  action :delete
#end


#directory "BE_webapps_creation" do
#  path "/var/lib/jetty/webapps"
#  owner 'jetty'
#  group 'jetty'
#  mode '0755'
#  action :create
#end


directory "BE_create_config_dir" do
  path "/var/lib/jetty/config"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end


directory "BE_create_catalog-be" do
  path "/var/lib/jetty/config/catalog-be"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end

directory "BE_create_onboarding-be" do
  path "/var/lib/jetty/config/onboarding-be"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  action :create
end

