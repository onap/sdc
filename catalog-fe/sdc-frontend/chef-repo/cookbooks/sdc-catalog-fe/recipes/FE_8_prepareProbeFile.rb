if node[:disableHttp]
  protocol = "https"
  port = "#{node['FE'][:https_port]}"
else
  protocol = "http"
  port = "#{node['FE'][:http_port]}"
end

template "/var/lib/ready-probe.sh" do
  source "ready-probe.sh.erb"
  sensitive true
  mode 0755
  variables({
    :protocol => protocol,
    :port => port
  })
end