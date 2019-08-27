if node['disableHttp']
  protocol = "https"
  be_port = node['BE']['https_port']
else
  protocol = "http"
  be_port = node['BE']['http_port']
end

template "/var/tmp/check_Backend_Health.py" do
    source "check_Backend_Health.py.erb"
    sensitive true
    mode 0755
    variables({
      :protocol => protocol,
      :be_ip => node['Nodes']['BE'],
      :be_port => be_port
    })
end

bash "executing-check_Backend_Health" do
   code <<-EOH
     python /var/tmp/check_Backend_Health.py
     rc=$?
     if [[ $rc != 0 ]]; then exit $rc; fi
   EOH
end