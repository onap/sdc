elasticsearch_list = ''

node['Nodes']['ES'].each  do |item|
    elasticsearch_list += "- " + item + ":9300\n"
end


template "/usr/share/elasticsearch/config/elasticsearch.yml" do
   source "ES-elasticsearch.yml.erb"
   owner "elasticsearch"
   group "elasticsearch"
   mode "0755"
   variables({
        :cluster_name => node['elasticsearch'][:cluster_name]+node.chef_environment,
        :node_name => node[:hostname],
        :es_ip_list => "#{elasticsearch_list}",
        :es_ip_list_XXX => node['Nodes']['ES'],
        :num_of_shards => node['elasticsearch'][:num_of_shards],
        :num_of_replicas => node['elasticsearch'][:num_of_replicas]
   })
end
