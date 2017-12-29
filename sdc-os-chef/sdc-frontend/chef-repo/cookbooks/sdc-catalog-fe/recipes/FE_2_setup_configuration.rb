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

template "designers-fe-config" do
   path "#{jetty_base}/config/catalog-fe/designers-configuration.yaml"
   source "FE-designers-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :dcae_host         => node['Designers']['DCAE']['dcae_host'],
      :dcae_port         => node['Designers']['DCAE']['dcae_port'],
      :dcae_path         => node['Designers']['DCAE']['dcae_path'],
      :dcae_protocol     => node['Designers']['DCAE']['dcae_protocol'],
      :workflow_host     => node['Designers']['WORKFLOW']['workflow_host'],
      :workflow_port     => node['Designers']['WORKFLOW']['workflow_port'],
      :workflow_path     => node['Designers']['WORKFLOW']['workflow_path'],
      :workflow_protocol => node['Designers']['WORKFLOW']['workflow_protocol']
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