template "/var/lib/ready-probe.sh" do
  source "ready-probe.sh.erb"
  sensitive true
  mode 0755
  variables({
     :cassandra_ip => node['Nodes']['CS'],
     :cassandra_pwd => ENV['CS_PASSWORD']
  })
end