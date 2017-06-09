template "/tmp/create_dox_keyspace.sh" do
  source "create_dox_keyspace.sh.erb"
  sensitive true
  mode 0755
  variables({
     :cassandra_ip => "HOSTIP",
     :DC_NAME      => node['cassandra'][:cluster_name]+node.chef_environment
  })
end


cookbook_file "/tmp/create_dox_db.cql" do
  sensitive true
  source "create_dox_db.cql"
  mode 0755 
end

cookbook_file "/tmp/alter_dox_db.cql" do
  sensitive true
  source "alter_dox_db.cql"
  mode 0755 
end


bash "create-DOX-schema" do
   ignore_failure true
   code <<-EOH
     cd /tmp 
     chmod +x /tmp/create_dox_keyspace.sh
     /tmp/create_dox_keyspace.sh
   EOH
end
