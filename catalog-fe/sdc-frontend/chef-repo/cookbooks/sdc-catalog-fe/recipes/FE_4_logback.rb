jetty_base=ENV['JETTY_BASE']
cookbook_file "/#{jetty_base}/config/catalog-fe/logback.xml" do
  source "FE-logback.xml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
