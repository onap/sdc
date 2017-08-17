jetty_base="/var/lib/jetty"
jetty_home="/usr/local/jetty"

###### create Jetty modules
bash "create-jetty-modules" do
cwd "#{jetty_base}"
code <<-EOH
   cd "#{jetty_base}"
   java -jar "/#{jetty_home}"/start.jar --add-to-start=deploy
   java -jar "/#{jetty_home}"/start.jar --add-to-startd=http,https,logging,setuid
EOH
not_if "ls /#{jetty_base}/start.d/https.ini"
end


####### configure Jetty modules
#template "SDC-Simulator-http-ini" do
#   path "/#{jetty_base}/start.d/http.ini"
#   source "SDC-Simulator-http-ini.erb"
#   owner "jetty"
#   group "jetty"
#   mode "0755"
#   variables :Simulator_http_port => "#{node['simulator'][:http_port]}"
#end


#template "FE-https-ini" do
#   path "/#{jetty_base}/start.d/https.ini"
#   source "FE-https-ini.erb"
#   owner "jetty"
#   group "jetty"
#   mode "0755"
#   variables :FE_https_port => "#{node['FE'][:https_port]}"
#end
#
#
#template "FE-ssl-ini" do
#   path "/#{jetty_base}/start.d/ssl.ini"
#   source "FE-ssl-ini.erb"
#   owner "jetty"
#   group "jetty"
#   mode "0755"
#   variables :FE_https_port => "#{node['FE'][:https_port]}"
#end

