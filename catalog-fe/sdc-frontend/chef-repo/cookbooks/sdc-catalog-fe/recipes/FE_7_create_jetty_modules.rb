jetty_base=ENV['JETTY_BASE']
jetty_home=ENV['JETTY_HOME']


bash "create-jetty-modules" do
cwd "#{jetty_base}"
code <<-EOH
   cd "#{jetty_base}"
   java -jar "/#{jetty_home}"/start.jar --add-to-start=deploy
   java -jar "/#{jetty_home}"/start.jar --add-to-startd=http,https,logging,setuid
EOH
end



template "FE-http-ini" do
   path "/#{jetty_base}/start.d/http.ini"
   source "FE-http-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables :FE_http_port => "#{node['FE'][:http_port]}"
end


template "FE-https-ini" do
   path "/#{jetty_base}/start.d/https.ini"
   source "FE-https-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables :FE_https_port => "#{node['FE'][:https_port]}"
end


template "ssl-ini" do
   path "/#{jetty_base}/start.d/ssl.ini"
   source "ssl-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables :https_port => "#{node['FE'][:https_port]}"
end


