template "catalog-fe-config" do
   path "#{ENV['JETTY_BASE']}/config/catalog-fe/configuration.yaml"
   source "FE-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :fe_host_ip   => node['Nodes']['FE'],
      :be_host_ip   => node['Nodes']['BE'],
      :kb_host_ip   => node['Nodes']['KB'],
      :catalog_port => node['BE'][:http_port],
      :ssl_port     => node['BE'][:https_port],
      :dcae_fe_vip  => node['DCAE_FE_VIP']
   })
end

template "plugins-fe-config" do
   path "#{ENV['JETTY_BASE']}/config/catalog-fe/plugins-configuration.yaml"
   source "FE-plugins-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :dcae_discovery_url     => node['Plugins']['DCAE']['dcae_discovery_url'],
      :dcae_source_url        => node['Plugins']['DCAE']['dcae_source_url'],
      :dcae_dt_discovery_url  => node['Plugins']['DCAE-TAB']['dcae_dt_discovery_url'],
      :dcae_dt_source_url     => node['Plugins']['DCAE-TAB']['dcae_dt_source_url'],
      :workflow_discovery_url => node['Plugins']['WORKFLOW']['workflow_discovery_url'],
      :workflow_source_url    => node['Plugins']['WORKFLOW']['workflow_source_url']
   })
end


template "onboarding-fe-config" do
    path "#{ENV['JETTY_BASE']}/config/onboarding-fe/onboarding_configuration.yaml"
    source "FE-onboarding-configuration.yaml.erb"
    owner "jetty"
    group "jetty"
    mode "0755"
end
