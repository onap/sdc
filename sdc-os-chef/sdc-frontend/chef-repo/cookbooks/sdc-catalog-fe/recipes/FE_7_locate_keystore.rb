jetty_base="/var/lib/jetty"
cookbook_file "/#{jetty_base}/etc/keystore" do
   source "keystore"
   owner "jetty"
   group "jetty"
   mode 0755
end
