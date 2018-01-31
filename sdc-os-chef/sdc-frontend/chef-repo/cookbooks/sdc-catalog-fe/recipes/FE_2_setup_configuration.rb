jetty_base="/var/lib/jetty"


template "catalog-fe-config" do
   path "#{jetty_base}/config/catalog-fe/configuration.yaml"
   source "FE-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :fe_host_ip   => node['HOST_IP'],
      :be_host_ip   => node['HOST_IP'],
      :kb_host_ip   => node['HOST_IP'],
      :catalog_port => node['BE'][:http_port],
      :ssl_port     => node['BE'][:https_port]
   })
end

template "plugins-fe-config" do
   path "#{jetty_base}/config/catalog-fe/plugins-configuration.yaml"
   source "FE-plugins-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :dcae_protocol             => node['Plugins']['DCAE']['dcae_protocol'],
      :dcae_host                 => node['Plugins']['DCAE']['dcae_host'],
      :dcae_port                 => node['Plugins']['DCAE']['dcae_port'],
      :workflow_protocol         => node['Plugins']['WORKFLOW']['workflow_protocol'],
      :workflow_host             => node['Plugins']['WORKFLOW']['workflow_host'],
      :workflow_port             => node['Plugins']['WORKFLOW']['workflow_port']
   })
end


template "onboarding-fe-config" do
 path "#{jetty_base}/config/onboarding-fe/onboarding_configuration.yaml"
 source "FE-onboarding-configuration.yaml.erb"
 owner "jetty"
 group "jetty"
 mode "0755"
 variables({
    :catalog_ip   => node['HOST_IP'],
    :catalog_port => node['BE'][:http_port],
    :ssl_port     => node['BE'][:https_port]
})
end