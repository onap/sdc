cookbook_file "#{ENV['JETTY_BASE']}/config/catalog-be/ecomp-error-configuration.yaml" do
  source "ecomp-error-configuration.yaml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
cookbook_file "#{ENV['JETTY_BASE']}/config/catalog-be/error-configuration.yaml" do
  source "error-configuration.yaml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
