jetty_base=ENV['JETTY_BASE']
clusterName = node['elasticsearch'][:cluster_name]+node.chef_environment

template "elasticsearch.yml-config" do
   path "/#{jetty_base}/config/elasticsearch.yml"
   source "BE-elasticsearch.yml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
        :cluster_name => "#{clusterName}",
        :es_host_ip => node['Nodes']['ES']
   })
end

