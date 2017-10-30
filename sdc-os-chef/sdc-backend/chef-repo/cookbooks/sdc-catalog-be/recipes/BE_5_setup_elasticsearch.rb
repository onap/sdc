clusterName = node['elasticsearch'][:cluster_name]+node.chef_environment
node_name = node[:hostname]

template "elasticsearch.yml-config" do
   path "/var/lib/jetty/config/elasticsearch.yml"
   source "BE-elasticsearch.yml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
        :cluster_name => "#{clusterName}",
        :es_host_ip => node['Nodes']['ES']
   })
end

