#Set the http module option
if node['FE'][:tls_cert]
  execute "generate-keystore" do
    command "openssl pkcs12 -inkey #{node['FE'][:tls_key]} -in #{node['FE'][:tls_cert]} -export -out /tmp/keystore.pkcs12 -passin pass:#{node['FE'][:tls_password]} -passout pass:#{node['FE'][:tls_password]}"
  end

  execute "import-keystore" do
    command "keytool -importkeystore -srcstoretype PKCS12 -srckeystore /tmp/keystore.pkcs12 -srcstorepass #{node['FE'][:tls_password]} -destkeystore #{ENV['JETTY_BASE']}/#{node['FE'][:keystore_path]} -deststorepass #{node['FE'][:keystore_password]} -noprompt"
  end
end

if node['FE'][:ca_cert]
  execute "delete-existing-ca-alias" do
    command "keytool -delete -alias sdc-be -storepass #{node['FE'][:truststore_password]} -keystore #{ENV['JETTY_BASE']}/#{node['FE'][:truststore_path]}"
    returns [0, 1]
  end

  execute "generate-truststore" do
    command "keytool -import  -alias sdc-be -file #{node['FE'][:ca_cert]} -storetype JKS -keystore #{ENV['JETTY_BASE']}/#{node['FE'][:truststore_path]} -storepass #{node['FE'][:truststore_password]} -noprompt"
  end
end
