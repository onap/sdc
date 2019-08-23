#
#Set the http module option
if node['disableHttp']
  http_option = "#--module=http"
else
  http_option = "--module=http"
end


bash "create-jetty-modules" do
cwd "#{ENV['JETTY_BASE']}"
code <<-EOH
   cd "#{ENV['JETTY_BASE']}"
   java -jar "#{ENV['JETTY_HOME']}"/start.jar --add-to-start=deploy
   java -jar "#{ENV['JETTY_HOME']}"/start.jar --create-startd --add-to-start=http,https,console-capture,setuid
EOH
end


template "http-ini" do
   path "#{ENV['JETTY_BASE']}/start.d/http.ini"
   source "http-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
     :http_option => http_option ,
     :http_port => "#{node['FE'][:http_port]}"
    })
end


template "https-ini" do
   path "#{ENV['JETTY_BASE']}/start.d/https.ini"
   source "https-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables :https_port => "#{node['FE'][:https_port]}"
end


template "ssl-ini" do
   path "#{ENV['JETTY_BASE']}/start.d/ssl.ini"
   source "ssl-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
     :https_port => "#{node['FE'][:https_port]}" ,
     :jetty_keystore_pwd => "#{node['jetty'][:keystore_pwd]}" ,
     :jetty_keymanager_pwd => "#{node['jetty'][:keymanager_pwd]}" ,
     :jetty_truststore_pwd => "#{node['jetty'][:truststore_pwd]}"
   })
end
