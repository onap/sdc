cookbook_file "#{ENV['JETTY_BASE']}/config/logback.xml" do
  source "logback.xml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
