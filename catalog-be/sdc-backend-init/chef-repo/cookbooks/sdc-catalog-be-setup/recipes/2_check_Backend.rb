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

bash "executing-check_backend_health" do
   code <<-EOH
     sdccheckbackend -i #{node['Nodes']['BE']} -p #{be_port} #{basic_auth_config} #{https_flag}
     rc=$?
     if [[ $rc != 0 ]]; then exit $rc; fi
   EOH
   returns [0]
end