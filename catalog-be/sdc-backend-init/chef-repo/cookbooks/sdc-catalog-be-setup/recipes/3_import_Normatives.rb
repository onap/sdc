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

bash "executing-import_Normatives" do
  code <<-EOH
    set -ex

    cd /var/tmp/
    tar -xvf normatives.tar.gz
    
    # executing the normatives
    # add --debug to the sdcinit command to enable debug

    cd /var/tmp/normatives/import/tosca
    sdcinit #{param} > /var/lib/jetty/logs/init.log
    rc=$?
    if [[ $rc != 0 ]]; then exit $rc; fi

  EOH
end
