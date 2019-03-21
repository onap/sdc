# Set the cassandra replica number
replication_factor=node['cassandra']['replication_factor']

if node['Pair_EnvName'] == ""
    titan_dcname_with_rep = node['cassandra']['datacenter_name'] + ","   + replication_factor.to_s
    conf_dcname_with_rep  = node['cassandra']['datacenter_name'] + "','" + replication_factor.to_s
else
    titan_dcname_with_rep = node['cassandra']['datacenter_name'] + ","   + replication_factor.to_s + "," + node['cassandra']['cluster_name']   + node['Pair_EnvName'] + ","   + replication_factor.to_s
    conf_dcname_with_rep  = node['cassandra']['datacenter_name'] + "','" + replication_factor.to_s + "','" + node['cassandra']['cluster_name'] + node['Pair_EnvName'] + "','" + replication_factor.to_s
end



template "titan.properties" do
   path "#{ENV['JETTY_BASE']}/config/catalog-be/titan.properties"
   source "BE-titan.properties.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :cassandra_ip             => node['Nodes']['CS'].join(",").gsub(/[|]/,''),
      :cassandra_pwd            => node['cassandra'][:cassandra_password],
      :cassandra_usr            => node['cassandra'][:cassandra_user],
      :rep_factor               => replication_factor,
      :DC_NAME                  => node['cassandra']['datacenter_name'],
      :DC_NAME_WITH_REP         => titan_dcname_with_rep,
      :titan_connection_timeout => node['cassandra']['titan_connection_timeout'],
      :cassandra_truststore_password => node['cassandra'][:truststore_password],
      :cassandra_ssl_enabled => "#{ENV['cassandra_ssl_enabled']}"
   })
end


template "catalog-be-config" do
   path "#{ENV['JETTY_BASE']}/config/catalog-be/configuration.yaml"
   source "BE-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :catalog_ip             => node['Nodes']['BE'],
      :catalog_port           => node['BE'][:http_port],
      :ssl_port               => node['BE'][:https_port],
      :cassandra_ip           => node['Nodes']['CS'].join(",").gsub(/[|]/,''),
      :rep_factor             => replication_factor,
      :DC_NAME                => node['cassandra']['datacenter_name'],
      :REP_STRING             => conf_dcname_with_rep,
      :titan_Path             => "/var/lib/jetty/config/catalog-be/",
      :socket_connect_timeout => node['cassandra']['socket_connect_timeout'],
      :socket_read_timeout    => node['cassandra']['socket_read_timeout'],
      :cassandra_pwd          => node['cassandra'][:cassandra_password],
      :cassandra_usr          => node['cassandra'][:cassandra_user],
      :cassandra_truststore_password => node['cassandra'][:truststore_password],
      :cassandra_ssl_enabled  => "#{ENV['cassandra_ssl_enabled']}",
      :dcae_be_vip            => node['DCAE_BE_VIP'],
      :dmaap_active => node['DMAAP']['active']
   })
end


template "distribution-engine-configuration" do
   path "#{ENV['JETTY_BASE']}/config/catalog-be/distribution-engine-configuration.yaml"
   source "BE-distribution-engine-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
end

