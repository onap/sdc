#Set the http module option
if node['disableHttp']
  http_option = "#--module=http"
else
  http_option = "--module=http"
end

execute "create-jetty-modules" do
  command "java -jar #{ENV['JETTY_HOME']}/start.jar --add-to-start=deploy && java -jar #{ENV['JETTY_HOME']}/start.jar --create-startd --add-to-start=http,https,setuid,rewrite"
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
    :http_port => "#{node['BE'][:http_port]}"
  })

end

template "jetty-rewrite" do
  path "#{ENV['JETTY_BASE']}/etc/rewrite-root-to-swagger-ui.xml"
  source "BE-jetty-rewrite.yaml.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0644"
end


template "https-ini" do
  path "#{ENV['JETTY_BASE']}/start.d/https.ini"
  source "https-ini.erb"
  owner "#{ENV['JETTY_USER']}"
  group "#{ENV['JETTY_GROUP']}"
  mode "0755"
  variables :https_port => "#{node['BE'][:https_port]}"
end
