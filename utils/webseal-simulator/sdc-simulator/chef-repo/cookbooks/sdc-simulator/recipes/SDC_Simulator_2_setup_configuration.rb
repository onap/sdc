cookbook_file "webseal.conf" do
   path "/#{jetty_base}/config/sdc-simulator/"
   source "webseal.conf"
   owner "jetty"
   group "jetty"
   mode "0755"
end


cookbook_file "logback.xml" do
   path "/#{jetty_base}/config/sdc-simulator/"
   source "logback.xml"
   owner "jetty"
   group "jetty"
   mode "0755"
end