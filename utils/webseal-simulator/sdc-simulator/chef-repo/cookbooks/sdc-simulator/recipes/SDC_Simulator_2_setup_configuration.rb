if ENV['FE_URL'] && !ENV['FE_URL'].empty?
    fe_url="#{ENV['FE_URL']}"
elsif  node['disableHttp']
    fe_url="https://#{node['Nodes']['FE']}:#{node['FE'][:https_port]}"
else
    fe_url="http://#{node['Nodes']['FE']}:#{node['FE'][:http_port]}"
end

template "webseal.conf" do
   path "#{ENV['JETTY_BASE']}/config/sdc-simulator/webseal.conf"
   source "SDC-Simulator-webseal.conf.erb"
   owner "#{ENV['JETTY_USER']}"
   group "#{ENV['JETTY_GROUP']}"
   mode "0755"
   variables({
      :fe_url  =>"#{fe_url}",
      :permittedAncestors => "#{ENV['permittedAncestors']}"
   })
end
