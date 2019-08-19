jetty_base="/var/lib/jetty"
jetty_home="/usr/local/jetty"

bash "create-jetty-modules" do
cwd "#{jetty_base}"
code <<-EOH
   cd "#{jetty_base}"
   java -jar "/#{jetty_home}"/start.jar --add-to-start=deploy
   java -jar "/#{jetty_home}"/start.jar --create-startd --add-to-start=http,https,console-capture,setuid
EOH
end

template "ssl-ini" do
   path "/#{jetty_base}/start.d/ssl.ini"
   source "SDC-Simulator-ssl-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
        :https_port           => "8443",
        :jetty_keystore_pwd   => "rTIS;B4kM]2GHcNK2c3B4&Ng",
        :jetty_keymanager_pwd => "rTIS;B4kM]2GHcNK2c3B4&Ng"
   })
end

template "https-ini" do
   path "/#{jetty_base}/start.d/https.ini"
   source "SDC-Simulator-https-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables ({
        :https_port => "8443"
   })
end

bash "echo status" do
   code <<-EOH
      echo "DOCKER STARTED"
   EOH
end

