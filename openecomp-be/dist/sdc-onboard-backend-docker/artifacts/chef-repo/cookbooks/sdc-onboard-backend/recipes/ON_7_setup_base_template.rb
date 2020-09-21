cookbook_file "#{ENV['JETTY_BASE']}/resources/base_template.yaml" do
  source "base_template.yaml"
  mode 0644
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
end

cookbook_file "#{ENV['JETTY_BASE']}/resources/base_template.env" do
  source "base_template.env"
  mode 0644
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
end