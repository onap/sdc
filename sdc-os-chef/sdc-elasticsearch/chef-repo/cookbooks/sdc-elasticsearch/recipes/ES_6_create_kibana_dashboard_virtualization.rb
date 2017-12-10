ruby_block "check_ElasticSearch_Cluster_Health" do
    block do
      #tricky way to load this Chef::Mixin::ShellOut utilities
      Chef::Resource::RubyBlock.send(:include, Chef::Mixin::ShellOut)
      #curl_command = "http://#{node['ipaddress']}:9200/_cluster/health?pretty=true"
      curl_command = "http://localhost:9200/_cluster/health?pretty=true"
      resp = Net::HTTP.get_response URI.parse(curl_command)
      stat = JSON.parse(resp.read_body)['status']

      case stat
         when "green"
            printf("\033[32m%s\n\033[0m", "  ElasticSearch Cluster status is green.")
         when "yellow"
            printf("\033[33m%s\n\033[0m", "  ElasticSearch Cluster status is yellow...")
         when "red"
            printf("\033[31m%s\n\033[0m", "  ElasticSearch Cluster status is red!")
      end
   end
   retries 10
   retry_delay 2
end

bash "create Kibana dashboard" do
    code <<-EOH
        for file in /root/chef-solo/cookbooks/sdc-elasticsearch/files/default/dashboard_*.json; do 
            name=`basename $file .json | awk -F"_" '{print $2}'` 
            echo "Loading dashboard $name:" 
            curl -XPUT http://localhost:9200/.kibana/dashboard/$name -d @$file || exit 1 
            echo 
        done 
    EOH
end

bash "create Kibana visualization" do
    code <<-EOH
        for file in /root/chef-solo/cookbooks/sdc-elasticsearch/files/default/visualization_*.json; do 
            name=`basename $file .json | awk -F"_" '{print $2}'` 
            echo "Loading visualization $name:" 
            curl -XPUT http://localhost:9200/.kibana/visualization/$name -d @$file || exit 1 
            echo 
        done 
    EOH
end

bash "echo status" do
   code <<-EOH
     echo "DOCKER STARTED"
   EOH
end