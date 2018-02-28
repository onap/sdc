cookbook_file "#{ENV['JETTY_BASE']}/config/catalog-fe/rest-configuration-info.yaml" do
  source "FE-rest-configuration.yaml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
