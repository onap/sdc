cookbook_file "/tmp/sdctool.tar" do
  source "sdctool.tar"
  mode 0755
end

## extract sdctool.tar
bash "install tar" do
  cwd "/tmp"
  code <<-EOH
     /bin/tar xvf /tmp/sdctool.tar -C /tmp
  EOH
end


template "/tmp/sdctool/config/configuration.yaml" do
  source "configuration.yaml.erb"
  mode 0755
  variables({
      :host_ip      => node['HOST_IP'],
      :catalog_port => node['BE'][:http_port],
      :ssl_port     => node['BE'][:https_port],
      :cassandra_ip => node['Nodes']['CS'],
      :rep_factor   => 1,
      :dc1          => "DC-"+node.chef_environment
  })
end

template "/tmp/sdctool/config/elasticsearch.yml" do
  source "elasticsearch.yml.erb"
  mode 0755
  variables({
     :elastic_ip => "HOSTIP"    
  })
end

