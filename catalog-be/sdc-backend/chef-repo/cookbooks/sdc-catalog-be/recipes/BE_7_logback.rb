jetty_base=ENV['JETTY_BASE']

cookbook_file "/#{jetty_base}/config/catalog-be/logback.xml" do
  source "logback.xml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
