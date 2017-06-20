replication_factor=1

template "titan.properties" do
   path "/var/lib/jetty/config/catalog-be/titan.properties"
   source "BE-titan.properties.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :CASSANDRA_IP => node['Nodes']['CS'],
      :CASSANDRA_PWD => node['cassandra'][:cassandra_password],
      :CASSANDRA_USR => node['cassandra'][:cassandra_user],
      :rep_factor => replication_factor,
      :DC_NAME      => node['cassandra'][:cluster_name]+node.chef_environment
   })
end


template "catalog-be-config" do
   path "/var/lib/jetty/config/catalog-be/configuration.yaml"
   source "BE-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :host_ip      => node['HOST_IP'],
      :catalog_port => node['BE'][:http_port],
      :ssl_port     => node['BE'][:https_port],
      :cassandra_ip => node['Nodes']['CS'],
      :rep_factor   => 1,
      :DC_NAME      => node['cassandra'][:cluster_name]+node.chef_environment,
      :titan_Path   => "/var/lib/jetty/config/catalog-be/"
   })
end


template "distribution-engine-configuration" do
   path "/var/lib/jetty/config/catalog-be/distribution-engine-configuration.yaml"
   source "BE-distribution-engine-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
end

