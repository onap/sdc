#Set the http module option
if node['ONBOARDING_BE'][:tls_cert]
  execute "generate-keystore" do
    command "openssl pkcs12 -inkey #{node['ONBOARDING_BE'][:tls_key]} -in #{node['ONBOARDING_BE'][:tls_cert]} -export -out /tmp/keystore.pkcs12 -passin pass:#{node['ONBOARDING_BE'][:tls_password]} -passout pass:#{node['ONBOARDING_BE'][:tls_password]}"
  end

  execute "import-keystore" do
    command "keytool -importkeystore -srcstoretype PKCS12 -srckeystore /tmp/keystore.pkcs12 -srcstorepass #{node['ONBOARDING_BE'][:tls_password]} -destkeystore #{ENV['JETTY_BASE']}/#{node['ONBOARDING_BE'][:keystore_path]} -deststorepass #{node['ONBOARDING_BE'][:keystore_password]} -noprompt"
  end
end

if node['ONBOARDING_BE'][:ca_cert]
  execute "delete-existing-ca-alias" do
    command "keytool -delete -alias sdc-be -storepass #{node['ONBOARDING_BE'][:truststore_password]} -keystore #{ENV['JETTY_BASE']}/#{node['ONBOARDING_BE'][:truststore_path]}"
    returns [0, 1]
  end

  execute "generate-truststore" do
    command "keytool -import  -alias sdc-be -file #{node['ONBOARDING_BE'][:ca_cert]} -storetype JKS -keystore #{ENV['JETTY_BASE']}/#{node['ONBOARDING_BE'][:truststore_path]} -storepass #{node['ONBOARDING_BE'][:truststore_password]} -noprompt"
  end
end