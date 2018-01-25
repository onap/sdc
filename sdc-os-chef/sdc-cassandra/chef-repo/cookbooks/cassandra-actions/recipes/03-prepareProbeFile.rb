template "/var/lib/ready-probe.sh" do
  source "ready-probe.sh.erb"
  sensitive true
  mode 0755
  variables({
     :cassandra_ip => node['Nodes']['CS'],
     :cassandra_pwd => node['cassandra'][:cassandra_password],
     :cassandra_usr => node['cassandra'][:cassandra_user]
  })
end