#
# Set the http module option
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
   source "SDC-Simulator-http-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :http_option => http_option,
      :http_port => "8080"
   })
end


template "https-ini" do
   path "#{ENV['JETTY_BASE']}/start.d/https.ini"
   source "SDC-Simulator-https-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :https_port => "8443"
   })
end

# TO CHANGE THE KEYSTORE/TRUSTSTORE CERT THE JVM CONFIGURATION
# MUST BE ALSO CHANGE IN THE startup.sh FILE
template "ssl-ini" do
   path "#{ENV['JETTY_BASE']}/start.d/ssl.ini"
   source "SDC-Simulator-ssl-ini.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
     :https_port           => "8443",
     :jetty_truststore_pwd => "].][xgtze]hBhz*wy]}m#lf*",
     :jetty_keystore_pwd   => "!ppJ.JvWn0hGh)oVF]([Kv)^",
     :jetty_keymanager_pwd => "!ppJ.JvWn0hGh)oVF]([Kv)^",
   })
end


bash "echo status" do
   code <<-EOH
      echo "DOCKER STARTED"
   EOH
end

