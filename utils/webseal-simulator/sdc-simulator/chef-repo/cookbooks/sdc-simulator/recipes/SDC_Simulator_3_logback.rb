cookbook_file "log4j2.properties" do
   path "#{ENV['JETTY_BASE']}/config/sdc-simulator/log4j2.properties"
   source "log4j2.properties"
   owner "#{ENV['JETTY_USER']}"
   group "#{ENV['JETTY_GROUP']}"
   mode "0755"
end