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
    python upgradeNormative.py -i localhost --debug=true > /var/lib/jetty/logs/upgradeNormative.log
    python importGroupTypes.py -i localhost > /var/lib/jetty/logs/importGroupTypes.log
  EOH
end

