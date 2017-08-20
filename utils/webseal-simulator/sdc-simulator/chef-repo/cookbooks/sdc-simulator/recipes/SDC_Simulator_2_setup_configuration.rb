jetty_base="/var/lib/jetty"


template "webseal.conf" do
   path "#{jetty_base}/config/sdc-simulator/webseal.conf"
   source "SDC-Simulator-webseal.conf.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :fe_host_ip   => node['HOST_IP'],
      :fe_http_port => "#{node['FE'][:http_port]}"
   })
end
