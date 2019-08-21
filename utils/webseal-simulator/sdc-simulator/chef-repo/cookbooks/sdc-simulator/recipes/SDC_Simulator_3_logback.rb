cookbook_file "log4j.properties" do
   path "#{ENV['JETTY_BASE']}/config/sdc-simulator/log4j.properties"
   source "log4j.properties"
   owner "jetty"
   group "jetty"
   mode "0755"
end