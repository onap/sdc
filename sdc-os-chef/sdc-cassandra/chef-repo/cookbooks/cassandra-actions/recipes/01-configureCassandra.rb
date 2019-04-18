cluster_name = ''
cluster_name = node['cassandra'][:cluster_name]

cas_ips=''
cas_ips=node['Nodes'][:CS]

interface = node['interfaces']['application']
application_host = ''
node['network']['interfaces'][interface][:addresses].each do | addr , details |
	if details['family'] == ('inet')
		application_host = addr
	end
end


template "cassandra-yaml-config" do
    path "/etc/cassandra/cassandra.yaml"
    source "cassandra.yaml.erb"
    sensitive true
    owner "cassandra"
    group "cassandra"
    mode "0755"
    variables ({
        :cassandra_port             => node['cassandra']['cassandra_port'],
        :cassandra_cluster          => cluster_name,
        :cassandra_data_dir         => node['cassandra'][:data_dir],
        :cassandra_commitlog_dir    => node['cassandra'][:commitlog_dir],
        :cassandra_cache_dir        => node['cassandra'][:cache_dir],
        :seeds_address              => cas_ips,
        :listen_address             => application_host,
        :broadcast_address          => application_host,
		:broadcast_rpc_address      => application_host,
        :rpc_address                => "0.0.0.0",
        :num_tokens                 => node['cassandra'][:num_tokens],
        :internode_encryption       => "none",
        :cassandra_truststore_dir   => "/etc/cassandra/cs_trust"
     })
end

rackNum=1
template "cassandra-rackdc.properties" do
    path "/etc/cassandra/cassandra-rackdc.properties"
    source "cassandra-rackdc.properties.erb"
    owner "cassandra"
    group "cassandra"
    mode "0755"
    variables ({
        :dc => node['cassandra']['datacenter_name'],
        :rack => "Rack"+"#{rackNum}"
    })
end
