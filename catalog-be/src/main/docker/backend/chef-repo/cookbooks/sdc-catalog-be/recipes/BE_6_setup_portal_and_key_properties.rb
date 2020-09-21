template "template portal.properties" do
    path "#{ENV['JETTY_BASE']}/resources/portal.properties"
    source "BE-portal.properties.erb"
    owner "#{ENV['JETTY_USER']}"
    group "#{ENV['JETTY_GROUP']}"
    mode "0755"
    variables ({
        :ecomp_rest_url      => node['ECompP']['ecomp_rest_url'],
        :ecomp_redirect_url  => node['ECompP']['ecomp_redirect_url'],
        :ecomp_portal_user  => node['ECompP']['portal_user'],
        :ecomp_portal_pass  => node['ECompP']['portal_pass'],
        :portal_app_name  => node['ECompP']['portal_app_name'],
    })
end

template "template key.properties" do
    path "#{ENV['JETTY_BASE']}/resources/key.properties"
    source "BE-key.properties.erb"
    owner "#{ENV['JETTY_USER']}"
    group "#{ENV['JETTY_GROUP']}"
    mode "0755"
    variables ({
        :cipher_key      => node['ECompP']['cipher_key']
    })
end