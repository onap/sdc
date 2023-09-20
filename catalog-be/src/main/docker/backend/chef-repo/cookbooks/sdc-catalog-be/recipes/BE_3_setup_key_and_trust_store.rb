#Set the http module option
if node['BE'][:tls_cert]
  execute "generate-keystore" do
    command "openssl pkcs12 -inkey #{node['BE'][:tls_key]} -in #{node['BE'][:tls_cert]} -export -out /tmp/keystore.pkcs12 -passin pass:#{node['BE'][:tls_password]} -passout pass:#{node['BE'][:tls_password]}"
  end

  execute "import-keystore" do
    command "keytool -importkeystore -srcstoretype PKCS12 -srckeystore /tmp/keystore.pkcs12 -srcstorepass #{node['BE'][:tls_password]} -destkeystore #{ENV['JETTY_BASE']}/#{node['BE'][:keystore_path]} -deststorepass #{node['BE'][:keystore_password]} -noprompt"
  end
end

if node['BE'][:ca_cert]
  execute "delete-existing-ca-alias" do
    command "keytool -delete -alias sdc-be -storepass #{node['BE'][:truststore_password]} -keystore #{ENV['JETTY_BASE']}/#{node['BE'][:truststore_path]}"
    returns [0, 1]
  end

  execute "generate-truststore" do
    command "keytool -import  -alias sdc-be -file #{node['BE'][:ca_cert]} -storetype JKS -keystore #{ENV['JETTY_BASE']}/#{node['BE'][:truststore_path]} -storepass #{node['BE'][:truststore_password]} -noprompt"
  end
end
