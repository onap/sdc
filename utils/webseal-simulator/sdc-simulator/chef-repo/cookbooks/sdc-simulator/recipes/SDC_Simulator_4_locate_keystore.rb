jetty_base="/var/lib/jetty"

directory "Jetty_etcdir_creation" do
	path "/#{jetty_base}/etc"
	owner 'jetty'
	group 'jetty'
	mode '0755'
	action :create
end

cookbook_file "/#{jetty_base}/etc/org.onap.sdc.trust.jks" do
   source "org.onap.sdc.trust.jks"
   owner "jetty"
   group "jetty"
   mode 0755
end
