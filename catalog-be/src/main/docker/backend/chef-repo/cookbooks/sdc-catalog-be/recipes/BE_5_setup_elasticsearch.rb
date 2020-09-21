clusterName = node['elasticsearch'][:cluster_name]+node.chef_environment

elasticsearch_list = ''

node['Nodes']['ES'].each  do |item|
    elasticsearch_list += "- " + item + ":9300\n"
end



template "elasticsearch.yml-config" do
  path "#{ENV['JETTY_BASE']}/config/elasticsearch.yml"
  source "BE-elasticsearch.yml.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0755"
  variables ({
    :cluster_name => "#{clusterName}",
    :es_host_ip => "#{elasticsearch_list}"
  })
end
