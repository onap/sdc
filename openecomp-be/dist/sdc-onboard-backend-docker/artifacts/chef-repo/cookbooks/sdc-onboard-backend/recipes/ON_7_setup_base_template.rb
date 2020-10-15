cookbook_file "#{ENV['JETTY_BASE']}/resources/base_template.yaml" do
  source "base_template.yaml"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
cookbook_file "#{ENV['JETTY_BASE']}/resources/base_template.env" do
  source "base_template.env"
  mode 0755
  owner "jetty"
  group "jetty"
end
 
