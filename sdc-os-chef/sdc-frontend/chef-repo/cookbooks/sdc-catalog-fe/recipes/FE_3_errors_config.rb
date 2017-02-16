cookbook_file "/var/lib/jetty/config/catalog-fe/ecomp-error-configuration.yaml" do
  source "FE-ecomp-error-configuration.yaml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
