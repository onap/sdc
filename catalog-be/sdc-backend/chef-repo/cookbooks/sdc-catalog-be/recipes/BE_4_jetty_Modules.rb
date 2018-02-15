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

template "ssl-ini" do
   path "/#{jetty_base}/start.d/ssl.ini"
   source "ssl-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables :BE_https_port => "#{node['BE'][:https_port]}"
end
