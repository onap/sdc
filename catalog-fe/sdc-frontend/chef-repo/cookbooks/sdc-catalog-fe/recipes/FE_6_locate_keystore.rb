directory "Jetty_etcdir_creation" do
	path "#{ENV['JETTY_BASE']}/etc"
	owner 'jetty'
	group 'jetty'
	mode '0755'
	action :create
end

cookbook_file "#{ENV['JETTY_BASE']}/etc/org.onap.sdc.p12" do
   source "org.onap.sdc.p12"
   owner "jetty"
   group "jetty"
   mode 0755
end

cookbook_file "#{ENV['JETTY_BASE']}/etc/org.onap.sdc.trust.jks" do
   source "org.onap.sdc.trust.jks"
   owner "jetty"
   group "jetty"
   mode 0755
end
