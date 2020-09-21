if node['disableHttp']
  protocol = "https"
  https_flag = "--https"
  be_port = node['BE']['https_port']
else
  protocol = "http"
  https_flag = ""
  be_port = node['BE']['http_port']
end

execute "executing-check_backend_health" do
  command "sdccheckbackend -i #{node['Nodes']['BE']} -p #{be_port} #{https_flag}"
  action :run
end