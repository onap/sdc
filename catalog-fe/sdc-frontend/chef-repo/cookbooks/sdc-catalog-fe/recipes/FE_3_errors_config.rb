cookbook_file "#{ENV['JETTY_BASE']}/config/catalog-fe/ecomp-error-configuration.yaml" do
  source "FE-ecomp-error-configuration.yaml"
  mode 0755
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
end
 
