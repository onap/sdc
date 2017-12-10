cookbook_file "/tmp/normatives.tar.gz" do 
   source "normatives.tar.gz"
end

working_directory =  "/tmp"

bash "upgrade-normatives" do
  cwd "#{working_directory}"
  code <<-EOH
    tar xvfz /tmp/normatives.tar.gz
    cd normatives/scripts/import/tosca/
    /bin/chmod +x upgradeNormative.py importGroupTypes.py
# add --debug=true to the importNormativeAll.py arguments to enable debug
    python upgradeONAPNormative.py -i localhost > /var/lib/jetty/logs/upgradeNormative.log
  EOH
end

