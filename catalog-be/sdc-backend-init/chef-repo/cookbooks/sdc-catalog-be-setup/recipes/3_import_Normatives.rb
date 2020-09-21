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

cookbook_file "/var/tmp/normatives.tar.gz" do
  source "normatives.tar.gz"
end

execute "create-jetty-modules" do
  command "set -ex && tar -xvf normatives.tar.gz && cd /var/tmp/normatives/import/tosca && sdcinit #{param} > #{ENV['ONAP_LOG']}/init.log"
  cwd "/var/tmp/"
  action :run
end