bash "create-jetty-modules" do
cwd "#{ENV['JETTY_BASE']}"
code <<-EOH
   cd "#{ENV['JETTY_BASE']}"
   java -jar "#{ENV['JETTY_HOME']}"/start.jar --add-to-start=deploy
   java -jar "#{ENV['JETTY_HOME']}"/start.jar --add-to-startd=http,https,logging,setuid
EOH
end

template "ssl-ini" do
   path "#{ENV['JETTY_BASE']}/start.d/ssl.ini"
   source "ssl-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables :BE_https_port => "#{node['BE'][:https_port]}"
end
