template "onboard-be-config" do
   path "#{ENV['JETTY_BASE']}/config/onboarding-be/onboarding_configuration.yaml"
   source "configuration.yaml.erb"
   owner "#{ENV['JETTY_USER']}"
   group "#{ENV['JETTY_GROUP']}"
   mode "0755"
   variables({
      :onboard_ip                      => node['ONBOARDING_BE_VIP'],
      :onboard_port                    => node['ONBOARDING_BE'][:http_port],
      :ssl_port                        => node['ONBOARDING_BE'][:https_port],
      :cassandra_ip                    => node['Nodes']['CS'].join(",").gsub(/[|]/,''),
      :cassandra_port                  => node['cassandra']['cassandra_port'],
      :DC_NAME                         => node['cassandra']['datacenter_name'],
      :socket_connect_timeout          => node['cassandra']['socket_connect_timeout'],
      :socket_read_timeout             => node['cassandra']['socket_read_timeout'],
      :cassandra_pwd                   => node['cassandra'][:cassandra_password],
      :cassandra_usr                   => node['cassandra'][:cassandra_user],
      :cassandra_truststore_password   => node['cassandra'][:truststore_password],
      :cassandra_ssl_enabled           => "#{ENV['cassandra_ssl_enabled']}",
      :basic_auth_enabled              => node['basic_auth']['enabled'],
      :basic_auth_username             => node['basic_auth'][:user_name],
      :basic_auth_password             => node['basic_auth'][:user_pass],
      :catalog_notification_url        => node['ONBOARDING_BE']['catalog_notification_url'],
      :catalog_be_http_port            => node['BE'][:http_port],
      :catalog_be_ssl_port             => node['BE'][:https_port],
      :permittedAncestors              => "#{ENV['permittedAncestors']}",
      :catalog_be_fqdn                 => node['Nodes']['BE']
   })
end

template "VnfrepoConfiguration" do
   path "#{ENV['JETTY_BASE']}/config/onboarding-be/config-vnfrepo.yaml"
   source "vnfrepo-configuration.yaml.erb"
   owner "#{ENV['JETTY_USER']}"
   group "#{ENV['JETTY_GROUP']}"
   mode "0755"
   variables({
      :VNFREPO_IP   => node['VnfRepo']['vnfRepoHost'],
      :VNFREPO_PORT => node['VnfRepo']['vnfRepoPort']
   })
end

template "HelmValidatorConfiguration" do
   path "#{ENV['JETTY_BASE']}/config/onboarding-be/config-helmvalidator.yaml"
   source "helmvalidator-configuration.yaml.erb"
   owner "#{ENV['JETTY_USER']}"
   group "#{ENV['JETTY_GROUP']}"
   mode "0755"
   variables({
      :HVALIDATOR_ENABLED           => node['HelmValidator']['validator_enabled'],
      :HVALIDATOR_URL               => node['HelmValidator']['validator_url'],
      :HVALIDATOR_HELM_VERSION      => node['HelmValidator']['helm_version'],
      :HVALIDATOR_DEPLOYABLE        => node['HelmValidator']['deployable'],
      :HVALIDATOR_LINTABLE          => node['HelmValidator']['lintable'],
      :HVALIDATOR_STRICT_LINTABLE   => node['HelmValidator']['strict_lintable']
   })
end

template "ExternalTestingConfiguration" do
   path "#{ENV['JETTY_BASE']}/config/onboarding-be/externaltesting-configuration.yaml"
   source "externaltesting-configuration.yaml.erb"
   owner "#{ENV['JETTY_USER']}"
   group "#{ENV['JETTY_GROUP']}"
   mode "0755"
   variables({
      :EP1_CONFIG => node['EXTTEST']['ep1_config'],
      :EP2_CONFIG => node['EXTTEST']['ep2_config']
   })
end

template "FeaturesProperties" do
   path "#{ENV['JETTY_BASE']}/config/onboarding-be/features.properties"
   source "features.properties.erb"
   owner "#{ENV['JETTY_USER']}"
   group "#{ENV['JETTY_GROUP']}"
   mode "0755"
end
