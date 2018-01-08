jetty_base="/var/lib/jetty"
replication_factor=1

template "titan.properties" do
   path "/#{jetty_base}/config/catalog-be/titan.properties"
   source "BE-titan.properties.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :CASSANDRA_IP             => node['Nodes']['CS'],
      :CASSANDRA_PWD            => node['cassandra'][:cassandra_password],
      :CASSANDRA_USR            => node['cassandra'][:cassandra_user],
      :rep_factor               => replication_factor,
      :DC_NAME                  => node['cassandra'][:cluster_name]+node.chef_environment,
      :titan_connection_timeout => node['cassandra']['titan_connection_timeout']
   })
end


template "catalog-be-config" do
   path "/#{jetty_base}/config/catalog-be/configuration.yaml"
   source "BE-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :catalog_ip             => node['HOST_IP'],
      :catalog_port           => node['BE'][:http_port],
      :ssl_port               => node['BE'][:https_port],
      :cassandra_ip           => node['Nodes']['CS'],
      :rep_factor             => 1,
      :DC_NAME                => node['cassandra'][:cluster_name]+node.chef_environment,
      :titan_Path             => "/var/lib/jetty/config/catalog-be/",
      :socket_connect_timeout => node['cassandra']['socket_connect_timeout'],
      :socket_read_timeout    => node['cassandra']['socket_read_timeout'],
      :cassandra_pwd          => node['cassandra'][:cassandra_password],
      :cassandra_usr          => node['cassandra'][:cassandra_user]

   })
end


template "distribution-engine-configuration" do
   path "/#{jetty_base}/config/catalog-be/distribution-engine-configuration.yaml"
   source "BE-distribution-engine-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
end


cookbook_file "ArtifactGenerator" do
   path "/#{jetty_base}/config/catalog-be/Artifact-Generator.properties"
   source "Artifact-Generator.properties"
   owner "jetty"
   group "jetty"
   mode "0755"
end
