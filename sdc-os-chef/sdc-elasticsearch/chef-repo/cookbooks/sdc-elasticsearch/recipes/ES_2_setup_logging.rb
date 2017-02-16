cookbook_file "/usr/share/elasticsearch/config/logging.yml" do
   source "logging.yml"
   owner "elasticsearch"
   group "elasticsearch"
   mode "0755"
end
