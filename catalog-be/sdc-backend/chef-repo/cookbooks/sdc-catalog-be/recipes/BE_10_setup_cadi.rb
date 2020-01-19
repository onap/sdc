jetty_base = "#{ENV['JETTY_BASE']}"


directory "Jetty_etc_dir_creation" do
	path "#{jetty_base}/etc"
	owner 'jetty'
	group 'jetty'
	mode '0755'
	action :create
  not_if { ::File.directory?("#{jetty_base}/etc") }
end


cookbook_file "#{jetty_base}/etc/keyfile" do
   source "keyfile"
   owner "jetty"
   group "jetty"
   mode 0755
end


cookbook_file "#{jetty_base}/etc/cadi_truststore.jks" do
   source "cadi_truststore.jks"
   owner "jetty"
   group "jetty"
   mode 0755
end


template "#{jetty_base}/etc/cadi.properties" do
  path "#{jetty_base}/etc/cadi.properties"
  source "cadi.properties.erb"
  owner "jetty"
  group "jetty"
  mode "0755"
end


#Workaround due to hardcode definition in cata,log-be web.xml file
directory "/opt/app/jetty" do
  path "/opt/app/jetty"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  recursive true
  action :create
end

directory "/opt/app/jetty/base/" do
  path "/opt/app/jetty/base/"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  recursive true
  action :create
end


directory "/opt/app/jetty/base/be/" do
  path "/opt/app/jetty/base/be/"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  recursive true
  action :create
end

directory "/opt/app/jetty/base/be/etc" do
  path "/opt/app/jetty/base/be/etc"
  owner 'jetty'
  group 'jetty'
  mode '0755'
  recursive true
  action :create
end

#Workaround due to hardcode definition in catalog-be web.xml file
template "/opt/app/jetty/base/be/etc/cadi.properties" do
  path "/opt/app/jetty/base/be/etc/cadi.properties"
  source "cadi.properties.erb"
  owner "jetty"
  group "jetty"
  mode "0755"
end

