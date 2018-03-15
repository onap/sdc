template "onboard-be-config" do
   path "#{ENV['JETTY_BASE']}/config/onboarding-be/onboarding_configuration.yaml"
   source "configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :onboard_ip             => node['Nodes']['BE'],
      :onboard_port           => node['BE'][:http_port],
      :ssl_port               => node['BE'][:https_port],
      :cassandra_ip           => node['Nodes']['CS'],
      :rep_factor             => 1,
      :DC_NAME                => node['cassandra'][:cluster_name]+node.chef_environment,
      :socket_connect_timeout => node['cassandra']['socket_connect_timeout'],
      :socket_read_timeout    => node['cassandra']['socket_read_timeout'],
      :cassandra_pwd          => node['cassandra'][:cassandra_password],
      :cassandra_usr          => node['cassandra'][:cassandra_user]
      :cassandra_traststore_password => node['cassandra'][:truststore_password],
      :cassandra_ssl_enabled => "#{ENV['cassandra_ssl_enabled']}"

   })
end