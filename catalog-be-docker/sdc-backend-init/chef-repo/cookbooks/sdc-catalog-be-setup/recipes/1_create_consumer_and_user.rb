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

bash "executing-create_users" do
  code <<-EOH
    sdcuserinit -i #{node['Nodes']['BE']} -p #{be_port} #{user_conf_dir} #{https_flag}
    rc=$?
    if [[ $rc != 0 ]]; then exit $rc; fi
  EOH
  returns [0]
end

bash "executing-create_consumers" do
  code <<-EOH
    sdcconsumerinit -i #{node['Nodes']['BE']} -p #{be_port} #{https_flag}
    rc=$?
    if [[ $rc != 0 ]]; then exit $rc; fi
  EOH
  returns [0]
end
