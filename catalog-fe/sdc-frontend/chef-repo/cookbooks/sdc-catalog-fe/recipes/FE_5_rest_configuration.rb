cookbook_file "#{ENV['JETTY_BASE']}/config/catalog-fe/rest-configuration-info.yaml" do
  source "FE-rest-configuration.yaml"
  mode 0755
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
end
 
