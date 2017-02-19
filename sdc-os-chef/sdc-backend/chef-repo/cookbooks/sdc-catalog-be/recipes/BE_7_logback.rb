cookbook_file "/var/lib/jetty/config/catalog-be/logback.xml" do
  source "BE-logback.xml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
