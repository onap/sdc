if node['disableHttp']
  protocol = "https"
  be_port = node['BE']['https_port']
else
  protocol = "http"
  be_port = node['BE']['http_port']
end


template "/var/tmp/user.py" do
    source "user.py.erb"
    sensitive true
    mode 0755
    variables({
      :protocol => protocol,
      :be_ip => node['Nodes']['BE'],
      :be_port => be_port
    })
end

bash "executing-create_user" do
   code <<-EOH
     python /var/tmp/user.py
     rc=$?
     if [[ $rc != 0 ]]; then exit $rc; fi
   EOH
end

template "/var/tmp/consumers.py" do
    source "consumers.py.erb"
    sensitive true
    mode 0755
    variables({
      :protocol => protocol,
      :be_ip => node['Nodes']['BE'],
      :be_port => be_port
    })
end

bash "executing-consumers" do
   code <<-EOH
     python /var/tmp/consumers.py
     rc=$?
     if [[ $rc != 0 ]]; then exit $rc; fi
   EOH
end
