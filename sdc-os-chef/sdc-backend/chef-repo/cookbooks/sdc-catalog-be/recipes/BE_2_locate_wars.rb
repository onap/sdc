jetty_base="/var/lib/jetty"
jetty_home="/usr/local/jetty"

###### create Jetty modules
bash "create-jetty-modules" do
cwd "#{jetty_base}"
code <<-EOH
   cd "#{jetty_base}"
   java -jar "/#{jetty_home}"/start.jar --add-to-start=deploy
   java -jar "/#{jetty_home}"/start.jar --add-to-startd=http,https,logging,setuid
EOH
not_if "ls /#{jetty_base}/start.d/https.ini"
end

###### copy catalog-be.war



###### copy onboarding-be.war& api-docs.war
#bash "copy-onboarding-be" do
#   code <<-EOH
#       /bin/tar -xvf /var/tmp/onboarding-be.tar -C  /var/tmp/
#           mv /var/tmp/onboarding-be*.war /var/tmp//api-docs*.war  #{jetty_base}/webapps
#   EOH
#end

