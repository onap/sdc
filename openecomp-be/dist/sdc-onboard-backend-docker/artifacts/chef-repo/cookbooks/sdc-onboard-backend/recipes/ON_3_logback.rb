cookbook_file "#{ENV['JETTY_BASE']}/config/onboarding-be/logback.xml" do
  source "logback.xml"
  mode 0755
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
end
 
