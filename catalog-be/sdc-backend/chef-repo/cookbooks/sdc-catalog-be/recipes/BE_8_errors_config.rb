jetty_base=ENV['JETTY_BASE']

cookbook_file "/#{jetty_base}/config/catalog-be/ecomp-error-configuration.yaml" do
  source "ecomp-error-configuration.yaml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
cookbook_file "/#{jetty_base}/config/catalog-be/error-configuration.yaml" do
  source "error-configuration.yaml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
