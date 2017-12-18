directory "/opt/kibana/config" do
  owner "kibana"
  group "kibana"
  mode '0775'
  action :create
end

template "kibana-yml" do
  path "/opt/kibana/config/kibana.yml"
  source "kibana.yml.erb"
  owner "kibana"
  group "kibana"
  mode "0755"
  variables ({
     :catalog_host => node['BE_VIP'] ,
     :catalog_port => node['BE'][:http_port]
  })
end

bash "echo status" do
   code <<-EOH
      echo "DOCKER STARTED"
   EOH
end

