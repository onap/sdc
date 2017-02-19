template "template portal.properties" do
    path "/var/lib/jetty/resources/portal.properties"
    source "BE-portal.properties.erb"
    owner "jetty"
    group "jetty"
    mode "0755"
    variables ({
        :ecomp_rest_url      => node['ECompP']['ecomp_rest_url'],
        :ecomp_redirect_url  => node['ECompP']['ecomp_redirect_url'],
        :ueb_url_list        => node['ECompP']['ueb_url_list'],
        :inbox_name          => node['ECompP']['inbox_name'],
        :app_key             => node['ECompP']['app_key'],
        :app_secret          => node['ECompP']['app_secret'],
        :app_topic_name      => node['ECompP']['app_topic_name'],
		:decrypt_key         => node['ECompP']['decryption_key']
    })
end
