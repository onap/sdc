jetty_base="/var/lib/jetty"


cookbook_file "logback.xml" do
   path "#{jetty_base}/config/sdc-simulator/logback.xml"
   source "logback.xml"
   owner "jetty"
   group "jetty"
   mode "0755"
end

cookbook_file "log4j.properties" do
   path "#{jetty_base}/config/sdc-simulator/log4j.properties"
   source "log4j.properties"
   owner "jetty"
   group "jetty"
   mode "0755"
end