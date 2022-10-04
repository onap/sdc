template "catalog-fe-config" do
   path "#{ENV['JETTY_BASE']}/config/catalog-fe/configuration.yaml"
   source "FE-configuration.yaml.erb"
   owner "#{ENV['JETTY_USER']}"
   group "#{ENV['JETTY_GROUP']}"
   mode "0755"
   variables({
      :fe_host_ip   => node['FE_VIP'],
      :be_host_ip   => node['BE_VIP'],
      :kb_host_ip   => node['Nodes']['KB'],
      :catalog_port => node['BE'][:http_port],
      :ssl_port     => node['BE'][:https_port],
      :basic_auth_flag => node['basic_auth']['enabled'],
      :user_name => node['basic_auth'][:user_name],
      :user_pass => node['basic_auth'][:user_pass],
      :permittedAncestors => "#{ENV['permittedAncestors']}",
      :dcae_fe_vip  => node['DCAE_FE_VIP']
   })
end

cookbook_file "#{ENV['JETTY_BASE']}/config/catalog-fe/workspace-configuration.yaml" do
  source "FE-workspace-configuration.yaml"
  mode 0755
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
end



template "onboarding-fe-config" do
    path "#{ENV['JETTY_BASE']}/config/onboarding-fe/onboarding_configuration.yaml"
    source "FE-onboarding-configuration.yaml.erb"
    owner "#{ENV['JETTY_USER']}"
    group "#{ENV['JETTY_GROUP']}"
    mode "0755"
end
