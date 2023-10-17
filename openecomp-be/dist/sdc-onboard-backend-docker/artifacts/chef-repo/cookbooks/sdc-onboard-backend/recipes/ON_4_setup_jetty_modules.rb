#Set the http module option
if node['disableHttp']
  http_option = "#--module=http"
else
  http_option = "--module=http"
end

execute "create-jetty-modules" do
  command "java -jar #{ENV['JETTY_HOME']}/start.jar --add-to-start=deploy && java -jar #{ENV['JETTY_HOME']}/start.jar --create-startd --add-to-start=http,https,setuid"
  cwd "#{ENV['JETTY_BASE']}"
  action :run
end

template "http-ini" do
  path "#{ENV['JETTY_BASE']}/start.d/http.ini"
  source "http-ini.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0755"
  variables ({
    :http_option => http_option ,
    :http_port => "#{node['ONBOARDING_BE'][:http_port]}"
  })
   
end


template "https-ini" do
  path "#{ENV['JETTY_BASE']}/start.d/https.ini"
  source "https-ini.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0755"
  variables :https_port => "#{node['ONBOARDING_BE'][:https_port]}"
end


template "ssl-ini" do
  path "#{ENV['JETTY_BASE']}/start.d/ssl.ini"
  source "ssl-ini.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0755"
  variables ({
    :https_port => "#{node['ONBOARDING_BE'][:https_port]}" ,
    :keystore_path => "#{node['ONBOARDING_BE'][:keystore_path]}" ,
    :keystore_password => "#{node['ONBOARDING_BE'][:keystore_password]}" ,
    :truststore_path => "#{node['ONBOARDING_BE'][:truststore_path]}" ,
    :truststore_password => "#{node['ONBOARDING_BE'][:truststore_password]}"
  })
end
