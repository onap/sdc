template "/tmp/create_dox_keyspace.sh" do
    source "create_dox_keyspace.sh.erb"
    sensitive true
    mode 0755
    variables({
      :cassandra_ip => "HOSTIP",
      :DC_NAME      => node['cassandra'][:cluster_name]+node.chef_environment
    })
end


remote_directory '/tmp/tools' do
    source 'tools'
    mode '0755'
    files_mode '0755'
    action :create
end


bash "onboard-db-schema-creation" do
    ignore_failure true
    code <<-EOH
     cd /tmp/tools/build/scripts
     chmod +x onboard-db-schema-creation.sh
     bash /tmp/tools/build/scripts/onboard-db-schema-creation.sh
    EOH
end
    
bash "create-DOX-schema" do
    ignore_failure true
    code <<-EOH
     cd /tmp 
     chmod +x /tmp/create_dox_keyspace.sh
     /tmp/create_dox_keyspace.sh
    EOH
end
