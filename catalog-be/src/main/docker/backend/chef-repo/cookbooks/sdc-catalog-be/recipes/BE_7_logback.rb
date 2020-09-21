cookbook_file "#{ENV['JETTY_BASE']}/config/catalog-be/logback.xml" do
  source "logback.xml"
  mode 0644
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  action :create_if_missing
end
 
