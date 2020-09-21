jetty_base = "#{ENV['JETTY_BASE']}"


directory "Jetty_etc_dir_creation" do
	path "#{jetty_base}/etc"
	owner "#{ENV['JETTY_USER']}"
	owner "#{ENV['JETTY_GROUP']}"
	mode '0755'
	action :create
  not_if { ::File.directory?("#{jetty_base}/etc") }
end


cookbook_file "#{jetty_base}/etc/keyfile" do
   source "keyfile"
   owner "#{ENV['JETTY_USER']}"
   owner "#{ENV['JETTY_GROUP']}"
   mode 0755
end


cookbook_file "#{jetty_base}/etc/cadi_truststore.jks" do
   source "cadi_truststore.jks"
   owner "#{ENV['JETTY_USER']}"
   owner "#{ENV['JETTY_GROUP']}"
   mode 0755
end


template "#{jetty_base}/etc/cadi.properties" do
  path "#{jetty_base}/etc/cadi.properties"
  source "cadi.properties.erb"
  owner "#{ENV['JETTY_USER']}"
  owner "#{ENV['JETTY_GROUP']}"
  mode "0755"
end

