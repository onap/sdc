#
# Set the http module option
if node['disableHttp']
  http_option = "#--module=http"
else
  http_option = "--module=http"
end

execute "create-jetty-modules" do
  command "java -jar #{ENV['JETTY_HOME']}/start.jar --add-to-start=deploy && java -jar #{ENV['JETTY_HOME']}/start.jar --create-startd --add-to-start=http,https,console-capture,setuid,rewrite"
  cwd "#{ENV['JETTY_BASE']}"
  action :run
end

template "http-ini" do
  path "#{ENV['JETTY_BASE']}/start.d/http.ini"
  source "SDC-Simulator-http-ini.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0755"
  variables({
              :http_option => http_option,
              :http_port => "8080"
            })
end

template "jetty-rewrite" do
  path "#{ENV['JETTY_BASE']}/etc/rewrite-root-to-sdc1.xml"
  source "SDC-Simulator-jetty-rewrire.xml.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0644"
end

template "https-ini" do
  path "#{ENV['JETTY_BASE']}/start.d/https.ini"
  source "SDC-Simulator-https-ini.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
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
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0755"
  variables({
              :https_port => "8443",
              :jetty_truststore_pwd => "z+KEj;t+,KN^iimSiS89e#p0",
              :jetty_keystore_pwd => "?(kP!Yur![*!Y5!E^f(ZKc31",
              :jetty_keymanager_pwd => "?(kP!Yur![*!Y5!E^f(ZKc31",
            })
end

execute "echo status" do
  command "echo 'DOCKER STARTED'"
  action :run
end