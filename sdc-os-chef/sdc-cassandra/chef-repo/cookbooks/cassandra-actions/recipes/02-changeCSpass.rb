interface = node['interfaces']['application']
application_host = ''
node['network']['interfaces'][interface][:addresses].each do | addr , details |
    if details['family'] == ('inet')
        application_host = addr
    end
end

template "/tmp/change_cassandra_pass.sh" do
  source "change_cassandra_pass.sh.erb"
  sensitive true
  mode 0755
  variables({
     :cassandra_ip => application_host,
     :cassandra_pwd => ENV['CS_PASSWORD'],
     :cassandra_port   => node['cassandra']['cassandra_port']
  })
end


bash "change-cassandra-pass" do
   code <<-EOH
     cd /tmp ; /tmp/change_cassandra_pass.sh
   EOH
end