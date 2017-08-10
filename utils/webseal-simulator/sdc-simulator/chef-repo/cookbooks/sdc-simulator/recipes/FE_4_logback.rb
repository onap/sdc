cookbook_file "/var/lib/jetty/config/catalog-fe/logback.xml" do
  source "FE-logback.xml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
