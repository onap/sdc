template "template base_template.yaml" do
  path "#{ENV['JETTY_BASE']}/resources/base_template.yaml"
  source "base_template.yaml"
  owner "jetty"
  group "jetty"
  mode "0755"
end


template "template base_template.env" do
  path "#{ENV['JETTY_BASE']}/resources/base_template.env"
  source "base_template.env"
  owner "jetty"
  group "jetty"
  mode "0755"
end
