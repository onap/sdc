cookbook_file "/tmp/sdctool.tar" do
  source "sdctool.tar"
  mode 0755
end

## extract sdctool.tar
bash "install tar" do
  cwd "/tmp"
  code <<-EOH
     /bin/tar xf /tmp/sdctool.tar -C /tmp
  EOH
end


template "janusgraph.properties" do
  sensitive true
  path "/tmp/sdctool/config/janusgraph.properties"
  source "janusgraph.properties.erb"
  mode "0755"
  variables({
     :DC_NAME                       => node['cassandra']['datacenter_name'],
     :cassandra_ip                  => node['Nodes']['CS'].first,
     :cassandra_port_num        => node['cassandra'][:cassandra_port],
     :janus_connection_timeout => node['cassandra'][:janusgraph_connection_timeout],
     :cassandra_pwd                 => node['cassandra'][:cassandra_password],
     :cassandra_usr                 => node['cassandra'][:cassandra_user],
     :janusgraph_connection_timeout => node['cassandra']['janusgraph_connection_timeout'],
     :replication_factor            => node['cassandra']['replication_factor'],
  })
end


template "/tmp/sdctool/config/configuration.yaml" do
  sensitive true
  source "configuration.yaml.erb"
  mode 0755
  variables({
      :host_ip                => node['Nodes']['BE'],
      :catalog_port           => node['BE'][:http_port],
      :ssl_port               => node['BE'][:https_port],
      :cassandra_ip           => node['Nodes']['CS'].first,
      :cassandra_port         => node['cassandra']['cassandra_port'],
      :rep_factor             => node['cassandra']['replication_factor'],
      :DC_NAME                => node['cassandra']['datacenter_name'],
      :janusgraph_Path        => "/tmp/sdctool/config/",
      :socket_connect_timeout => node['cassandra']['socket_connect_timeout'],
      :socket_read_timeout    => node['cassandra']['socket_read_timeout'],
      :cassandra_pwd          => node['cassandra'][:cassandra_password],
      :cassandra_usr          => node['cassandra'][:cassandra_user]
  })
end


template "/tmp/sdctool/config/elasticsearch.yml" do
  sensitive true
  source "elasticsearch.yml.erb"
  mode 0755
  variables({
     :elastic_ip => node['Nodes']['ES']
  })
end

bash "executing-schema-creation" do
   code <<-EOH
     cd /tmp
     chmod +x /tmp/sdctool/scripts/schemaCreation.sh
     /tmp/sdctool/scripts/schemaCreation.sh /tmp/sdctool/config
   EOH
end

bash "executing-janusGraphSchemaCreation.sh" do
  code <<-EOH
     chmod +x /tmp/sdctool/scripts/janusGraphSchemaCreation.sh
     /tmp/sdctool/scripts/janusGraphSchemaCreation.sh /tmp/sdctool/config
   EOH
end
