template "/tmp/change_cassandra_pass.sh" do
  source "change_cassandra_pass.sh.erb"
  sensitive true
  mode 0755
  variables({
     :cassandra_ip => node['Nodes']['CS'],
     :cassandra_pwd => node['cassandra'][:cassandra_password],
     :cassandra_usr => node['cassandra'][:cassandra_user]
  })
end


bash "change-cassandra-pass" do
   code <<-EOH
     cd /tmp ; /tmp/change_cassandra_pass.sh
   EOH
end

bash "echo status" do
   code <<-EOH
     echo "DOCKER STARTED"
   EOH
end