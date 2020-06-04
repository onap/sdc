if node['disableHttp']
  protocol = "https"
  https_flag = "--https"
  be_port = node['BE']['https_port']
else
  protocol = "http"
  https_flag = ""
  be_port = node['BE']['http_port']
end

bash "executing-check_backend_health" do
   code <<-EOH
     sdccheckbackend -i #{node['Nodes']['BE']} -p #{be_port} #{https_flag}
     rc=$?
     if [[ $rc != 0 ]]; then exit $rc; fi
   EOH
  returns [0]
end