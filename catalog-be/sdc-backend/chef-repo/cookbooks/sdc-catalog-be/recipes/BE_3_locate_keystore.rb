jetty_base=ENV['JETTY_BASE']

directory "Jetty_etcdir_creation" do
	path "/#{jetty_base}/etc"
	owner 'jetty'
	group 'jetty'
	mode '0755'
	action :create
end
	
cookbook_file "/#{jetty_base}/etc/keystore" do
   source "keystore"
   owner "jetty"
   group "jetty"
   mode 0755
end
