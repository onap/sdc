template "catalog-fe-config" do
   path "#{ENV['JETTY_BASE']}/config/catalog-fe/configuration.yaml"
   source "FE-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :fe_host_ip   => node['FE_VIP'],
      :be_host_ip   => node['BE_VIP'],
      :kb_host_ip   => node['Nodes']['KB'],
      :catalog_port => node['BE'][:http_port],
      :ssl_port     => node['BE'][:https_port],
      :dcae_fe_vip  => node['DCAE_FE_VIP']
   })
end

cookbook_file "#{ENV['JETTY_BASE']}/config/catalog-fe/workspace-configuration.yaml" do
  source "FE-workspace-configuration.yaml"
  mode 0755
  owner "jetty"
  group "jetty"
end



template "onboarding-fe-config" do
    path "#{ENV['JETTY_BASE']}/config/onboarding-fe/onboarding_configuration.yaml"
    source "FE-onboarding-configuration.yaml.erb"
    owner "jetty"
    group "jetty"
    mode "0755"
end
