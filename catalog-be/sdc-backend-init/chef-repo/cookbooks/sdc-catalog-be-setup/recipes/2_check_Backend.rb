require 'base64'
if node['disableHttp']
  protocol = "https"
  https_flag = "--https"
  be_port = node['BE']['https_port']
  if node['BE-init']['tls_cert'] && node['BE-init']['tls_key']
    tls_key = "--tls_key " + node['BE-init']['tls_key']
    tls_cert = "--tls_cert " + node['BE-init']['tls_cert']
    if node['BE-init']['tls_password']
      tls_key_pw = "--tls_key_pw " + node['BE-init']['tls_password']
    end
  end
  if node['BE-init']['ca_cert']
    ca_cert =  "--ca_cert " + node['BE-init']['ca_cert']
  end
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
    basic_auth_config = "--header " + Base64.strict_encode64(basic_auth_user + ":" + basic_auth_pass)
  else
    # set default user configuration file
    basic_auth_config = ""
  end
end

execute "executing-check_backend_health" do
  command "sdccheckbackend -i #{node['Nodes']['BE']} -p #{be_port} #{basic_auth_config} #{https_flag} #{tls_cert} #{tls_key} #{tls_key_pw} #{ca_cert}"
  action :run
end
