require 'base64'
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

if node['basic_auth']
  basic_auth_enabled = node['basic_auth']['enabled']
  basic_auth_user = node['basic_auth']['user_name']
  basic_auth_pass = node['basic_auth']['user_pass']
  if basic_auth_enabled
    basic_auth_config = "--header " + Base64.encode64(basic_auth_user + ":" + basic_auth_pass)
  else
    # set default user configuration file
    basic_auth_config = ""
  end
end

bash "executing-create_users" do
  code <<-EOH
    sdcuserinit -i #{node['Nodes']['BE']} -p #{be_port} #{basic_auth_config} #{user_conf_dir} #{https_flag}
    rc=$?
    if [[ $rc != 0 ]]; then exit $rc; fi
  EOH
  returns [0]
end

bash "executing-create_consumers" do
  code <<-EOH
    sdcconsumerinit -i #{node['Nodes']['BE']} -p #{be_port} #{basic_auth_config} #{https_flag}
    rc=$?
    if [[ $rc != 0 ]]; then exit $rc; fi
  EOH
  returns [0]
end
