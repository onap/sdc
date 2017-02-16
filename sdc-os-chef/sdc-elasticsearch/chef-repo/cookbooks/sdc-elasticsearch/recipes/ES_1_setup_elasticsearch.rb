clusterName = node['elasticsearch'][:cluster_name]+node.chef_environment
node_name = node[:hostname]

template "/usr/share/elasticsearch/config/elasticsearch.yml" do
   source "ES-elasticsearch.yml.erb"
   owner "elasticsearch"
   group "elasticsearch"
   mode "0755"
   variables({
        :cluster_name => "#{clusterName}",
        :node_name => node_name,
        :ES_IP => node['Nodes']['ES'],
        :num_of_shards => node['elasticsearch'][:num_of_shards],
        :num_of_replicas => node['elasticsearch'][:num_of_replicas]
   })
end
