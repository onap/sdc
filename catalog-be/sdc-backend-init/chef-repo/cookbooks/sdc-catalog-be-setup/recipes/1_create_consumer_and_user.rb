template "/tmp/user.py" do
    source "user.py.erb"
    sensitive true
    mode 0755
    variables({
      :be_ip => node['Nodes']['BE']
    })
end

bash "excuting-create_user" do
   code <<-EOH
     python /tmp/user.py
     rc=$?
     if [[ $rc != 0 ]]; then exit $rc; fi
   EOH
end

template "/tmp/consumers.py" do
    source "consumers.py.erb"
    sensitive true
    mode 0755
    variables({
      :be_ip => node['Nodes']['BE']
    })
end

bash "excuting-consumers" do
   code <<-EOH
     python /tmp/consumers.py
     rc=$?
     if [[ $rc != 0 ]]; then exit $rc; fi
   EOH
end
