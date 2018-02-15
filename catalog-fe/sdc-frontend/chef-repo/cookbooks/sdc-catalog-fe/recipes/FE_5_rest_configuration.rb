jetty_base=ENV['JETTY_BASE']
cookbook_file "/#{jetty_base}/config/catalog-fe/rest-configuration-info.yaml" do
  source "FE-rest-configuration.yaml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
