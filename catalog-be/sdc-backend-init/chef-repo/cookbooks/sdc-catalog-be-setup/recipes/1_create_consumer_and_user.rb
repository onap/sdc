if node['disableHttp']
  protocol = "https"
  https_flag = "--https"
  be_port = node['BE']['https_port']
else
  protocol = "http"
  https_flag = ""
  be_port = node['BE']['http_port']
end

if node['BE']['user_conf']
  user_conf_dir = "--conf " + node['BE']['user_conf']
else
  # set default user configuration file
  user_conf_dir = ""
end

execute "executing-create_users" do
  command "sdcuserinit -i #{node['Nodes']['BE']} -p #{be_port} #{user_conf_dir} #{https_flag}"
  action :run
end

execute "executing-create_consumers" do
  command "sdcconsumerinit -i #{node['Nodes']['BE']} -p #{be_port} #{https_flag}"
  action :run
end