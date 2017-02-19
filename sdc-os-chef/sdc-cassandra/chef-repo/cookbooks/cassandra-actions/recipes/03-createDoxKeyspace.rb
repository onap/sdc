template "/tmp/create_dox_keyspace.sh" do
  source "create_dox_keyspace.sh.erb"
  mode 0755
  variables({
     :cassandra_ip => "HOSTIP"    
  })
end

