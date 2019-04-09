template "onboard-be-config" do
   path "#{ENV['JETTY_BASE']}/config/onboarding-be/onboarding_configuration.yaml"
   source "configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :onboard_ip                      => node['ONBOARDING_BE_VIP'],
      :onboard_port                    => node['ONBOARDING_BE'][:http_port],
      :ssl_port                        => node['ONBOARDING_BE'][:https_port],
      :cassandra_ip                    => node['Nodes']['CS'].join(",").gsub(/[|]/,''),
      :cassandra_port                  => node['cassandra']['cassandra_port'],
      :DC_NAME                         => node['cassandra']['datacenter_name']+node.chef_environment,
      :socket_connect_timeout          => node['cassandra']['socket_connect_timeout'],
      :socket_read_timeout             => node['cassandra']['socket_read_timeout'],
      :cassandra_pwd                   => node['cassandra'][:cassandra_password],
      :cassandra_usr                   => node['cassandra'][:cassandra_user],
      :cassandra_truststore_password   => node['cassandra'][:truststore_password],
      :cassandra_ssl_enabled           => "#{ENV['cassandra_ssl_enabled']}",
      :catalog_notification_url        => node['ONBOARDING_BE']['catalog_notification_url'],
      :catalog_be_http_port            => node['BE'][:http_port],
      :catalog_be_ssl_port             => node['BE'][:https_port],
      :catalog_be_fqdn                 => node['Nodes']['BE']
   })
end



template "VnfrepoConfiguration" do
   path "#{ENV['JETTY_BASE']}/config/onboarding-be/config-vnfrepo.yaml"
   source "vnfrepo-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :VNFREPO_IP   => node['VnfRepo']['vnfRepoHost'],
      :VNFREPO_PORT => node['VnfRepo']['vnfRepoPort']
   })
end



template "ExternalTestingConfiguration" do
   path "#{ENV['JETTY_BASE']}/config/onboarding-be/externaltesting-configuration.yaml"
   source "externaltesting-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :EP1_CONFIG => node['EXTTEST']['ep1_config'],
      :EP2_CONFIG => node['EXTTEST']['ep2_config']
   })
end
