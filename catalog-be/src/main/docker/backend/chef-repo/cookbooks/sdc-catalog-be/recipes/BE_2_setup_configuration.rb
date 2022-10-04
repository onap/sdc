# Set the cassandra replica number
replication_factor = node['cassandra']['replication_factor']

if node['Pair_EnvName'] == ""
  janusgraph_dcname_with_rep = node['cassandra']['datacenter_name'] + "," + replication_factor.to_s
  conf_dcname_with_rep = node['cassandra']['datacenter_name'] + "','" + replication_factor.to_s
else
  janusgraph_dcname_with_rep = node['cassandra']['datacenter_name'] + "," + replication_factor.to_s + "," + node['cassandra']['cluster_name'] + node['Pair_EnvName'] + "," + replication_factor.to_s
  conf_dcname_with_rep = node['cassandra']['datacenter_name'] + "','" + replication_factor.to_s + "','" + node['cassandra']['cluster_name'] + node['Pair_EnvName'] + "','" + replication_factor.to_s
end

#Set random ID for DMaap configuration
if node['DMAAP']['random_id'].nil?
  node.default['DMAAP']['random_id'] = Time.now.getutc.to_i
end

template "janusgraph.properties" do
  path "#{ENV['JETTY_BASE']}/config/catalog-be/janusgraph.properties"
  source "BE-janusgraph.properties.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0644"
  action :create_if_missing
  variables({
                :cassandra_ip => node['Nodes']['CS'].join(",").gsub(/[|]/, ''),
                :cassandra_cql_port => node['cassandra'][:cassandra_port],
                :cassandra_pwd => node['cassandra'][:cassandra_password],
                :cassandra_usr => node['cassandra'][:cassandra_user],
                :rep_factor => replication_factor,
                :DC_NAME => node['cassandra']['datacenter_name'],
                :DC_NAME_WITH_REP => janusgraph_dcname_with_rep,
                :janus_connection_timeout => node['cassandra']['janusgraph_connection_timeout'],
                :cassandra_truststore_password => node['cassandra'][:truststore_password],
                :cassandra_ssl_enabled => "#{ENV['cassandra_ssl_enabled']}",
                :cassandra_read_consistency_level => node['cassandra'][:read_consistency_level],
                :cassandra_write_consistency_level => node['cassandra'][:write_consistency_level],
                :cassandra_db_cache => node['cassandra'][:db_cache]
             })
end

template "catalog-be-config" do
  path "#{ENV['JETTY_BASE']}/config/catalog-be/configuration.yaml"
  source "BE-configuration.yaml.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0644"
  action :create_if_missing
  variables({
                :catalog_ip => node['Nodes']['BE'],
                :catalog_port => node['BE'][:http_port],
                :ssl_port => node['BE'][:https_port],
                :basic_auth_flag => node['basic_auth']['enabled'],
                :user_name => node['basic_auth'][:user_name],
                :user_pass => node['basic_auth'][:user_pass],
                :cassandra_ip => node['Nodes']['CS'].join(",").gsub(/[|]/, ''),
                :cassandra_port => node['cassandra']['cassandra_port'],
                :rep_factor => replication_factor,
                :DC_NAME => node['cassandra']['datacenter_name'],
                :REP_STRING => conf_dcname_with_rep,
                :janusgraph_Path => "#{ENV['JETTY_BASE']}/config/catalog-be/",
                :socket_connect_timeout => node['cassandra']['socket_connect_timeout'],
                :socket_read_timeout => node['cassandra']['socket_read_timeout'],
                :cassandra_pwd => node['cassandra'][:cassandra_password],
                :cassandra_usr => node['cassandra'][:cassandra_user],
                :cassandra_truststore_password => node['cassandra'][:truststore_password],
                :cassandra_ssl_enabled => "#{ENV['cassandra_ssl_enabled']}",
                :permittedAncestors => "#{ENV['permittedAncestors']}",
                :dmaap_active => node['DMAAP']['active']
            })
end

template "distribution-engine-configuration" do
  path "#{ENV['JETTY_BASE']}/config/catalog-be/distribution-engine-configuration.yaml"
  source "BE-distribution-engine-configuration.yaml.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0644"
  action :create_if_missing
end
