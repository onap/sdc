jetty_base="/var/lib/jetty"
jetty_home="/usr/local/jetty"

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
   source "SDC-Simulator-ssl-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
        :https_port           => "8443",
        :jetty_keystore_pwd   => "OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4",
        :jetty_keymanager_pwd => "OBF:1u2u1wml1z7s1z7a1wnl1u2g"
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

