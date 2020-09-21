cookbook_file "#{ENV['JETTY_BASE']}/config/catalog-fe/logback.xml" do
  source "FE-logback.xml"
  mode 0755
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
end
