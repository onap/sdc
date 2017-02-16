cookbook_file "/var/lib/jetty/config/catalog-be/ecomp-error-configuration.yaml" do
  source "BE-ecomp-error-configuration.yaml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
cookbook_file "/var/lib/jetty/config/catalog-be/error-configuration.yaml" do
  source "BE-error-configuration.yaml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
