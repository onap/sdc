template "/tmp/check_Backend_Health.py" do
    source "check_Backend_Health.py.erb"
    sensitive true
    mode 0755
    variables({
      :be_ip => node['Nodes']['BE']
    })
end

bash "excuting-check_Backend_Health" do
   code <<-EOH
     python /tmp/check_Backend_Health.py
     rc=$?
     if [[ $rc != 0 ]]; then exit $rc; fi
   EOH
end