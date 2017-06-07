template "/tmp/create_cassandra_user.sh" do
  source "create_cassandra_user.sh.erb"
  sensitive true
  mode 0755
  variables({
     :cassandra_ip => "HOSTIP"    
  })
end


bash "create-sdc-user" do
   code <<-EOH
     cd /tmp ; /tmp/create_cassandra_user.sh
   EOH
end
