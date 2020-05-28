cookbook_file "#{ENV['JETTY_BASE']}/config/catalog-be/ecomp-error-configuration.yaml" do
  source "ecomp-error-configuration.yaml"
  mode 0644
  owner "jetty"
  group "jetty"
  action :create_if_missing
end
 
cookbook_file "#{ENV['JETTY_BASE']}/config/catalog-be/error-configuration.yaml" do
  source "error-configuration.yaml"
  mode 0644
  owner "jetty"
  group "jetty"
  action :create_if_missing
end
 
