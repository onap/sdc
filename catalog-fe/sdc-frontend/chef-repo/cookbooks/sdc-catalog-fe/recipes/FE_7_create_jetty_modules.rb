bash "create-jetty-modules" do
cwd "#{ENV['JETTY_BASE']}"
code <<-EOH
   cd "#{ENV['JETTY_BASE']}"
   java -jar "#{ENV['JETTY_HOME']}"/start.jar --add-to-start=deploy
   java -jar "#{ENV['JETTY_HOME']}"/start.jar --add-to-startd=http,https,logging,setuid
EOH
end



template "FE-http-ini" do
   path "#{ENV['JETTY_BASE']}/start.d/http.ini"
   source "FE-http-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables :FE_http_port => "#{node['FE'][:http_port]}"
end


template "FE-https-ini" do
   path "#{ENV['JETTY_BASE']}/start.d/https.ini"
   source "FE-https-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables :FE_https_port => "#{node['FE'][:https_port]}"
end


template "ssl-ini" do
   path "#{ENV['JETTY_BASE']}/start.d/ssl.ini"
   source "ssl-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables :https_port => "#{node['FE'][:https_port]}"
end


