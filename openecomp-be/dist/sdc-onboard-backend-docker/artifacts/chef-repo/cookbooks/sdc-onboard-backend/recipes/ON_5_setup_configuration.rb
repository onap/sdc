template "onboard-be-config" do
   path "#{ENV['JETTY_BASE']}/config/onboarding-be/onboarding_configuration.yaml"
   source "configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :onboard_ip             => node['ONBOARDING_BE_VIP'],
      :onboard_port           => node['ONBOARDING_BE'][:http_port],
      :ssl_port               => node['ONBOARDING_BE'][:https_port],
      :cassandra_ip           => node['Nodes']['CS'].join(",").gsub(/[|]/,''),
      :DC_NAME                => node['cassandra']['datacenter_name']+node.chef_environment,
      :socket_connect_timeout => node['cassandra']['socket_connect_timeout'],
      :socket_read_timeout    => node['cassandra']['socket_read_timeout'],
      :cassandra_pwd          => node['cassandra'][:cassandra_password],
      :cassandra_usr          => node['cassandra'][:cassandra_user],
      :cassandra_truststore_password => node['cassandra'][:truststore_password],
      :cassandra_ssl_enabled => "#{ENV['cassandra_ssl_enabled']}"
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
