if node[:disableHttp]
  protocol = "https"
  port = "#{node['ONBOARDING_BE'][:https_port]}"
else
  protocol = "http"
  port = "#{node['ONBOARDING_BE'][:http_port]}"
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
