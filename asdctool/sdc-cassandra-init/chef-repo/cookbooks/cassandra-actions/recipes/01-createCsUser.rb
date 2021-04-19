template "/tmp/create_cassandra_user.sh" do
  source "create_cassandra_user.sh.erb"
  sensitive true
  mode 0755
  variables({
     :cassandra_ip      => node['Nodes']['CS'].first,
     :cassandra_port    => node['cassandra']['cassandra_port'],
     :cassandra_pwd     => ENV['CS_PASSWORD'],
     :sdc_usr           => ENV['SDC_USER'],
     :sdc_pwd           => ENV['SDC_PASSWORD']
  })
end

execute "create-sdc-user" do
  command "/tmp/create_cassandra_user.sh"
  cwd "/tmp/"
  action :run
end