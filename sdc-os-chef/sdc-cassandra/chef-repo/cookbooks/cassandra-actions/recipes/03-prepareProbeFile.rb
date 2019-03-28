interface = node['interfaces']['application']
application_host = ''
node['network']['interfaces'][interface][:addresses].each do | addr , details |
    if details['family'] == ('inet')
        application_host = addr
    end
end

template "/var/lib/ready-probe.sh" do
  source "ready-probe.sh.erb"
  sensitive true
  mode 0755
  variables({
     :cassandra_ip => application_host,
     :cassandra_pwd => ENV['CS_PASSWORD'],
     :cassandra_port   => node['cassandra']['cassandra_port']
  })
end