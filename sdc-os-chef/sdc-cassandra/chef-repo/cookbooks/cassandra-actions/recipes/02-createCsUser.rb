template "/tmp/create_cassandra_user.sh" do
  source "create_cassandra_user.sh.erb"
  mode 0755
  variables({
     :cassandra_ip => "HOSTIP"    
  })
end

