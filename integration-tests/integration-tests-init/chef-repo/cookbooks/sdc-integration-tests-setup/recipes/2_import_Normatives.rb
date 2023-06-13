require 'base64'
be_ip=node['Nodes'][:BE]

if node['disableHttp']
  protocol = "https"
  be_port = node['BE']['https_port']
  param="-i #{be_ip} -p #{be_port} --https"
else
  protocol = "http"
  be_port = node['BE']['http_port']
  param="-i #{be_ip} -p #{be_port}"
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
cookbook_file "/var/tmp/normatives.tar.gz" do
  source "normatives.tar.gz"
end

execute "create-jetty-modules" do
  command "set -ex && tar -xvf normatives.tar.gz && cd /var/tmp/normatives/import/tosca && sdcinit #{param} #{basic_auth_config} > #{ENV['ONAP_LOG']}/init.log"
  cwd "/var/tmp/"
  action :run
end
