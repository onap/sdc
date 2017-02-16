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

bash "create monitoring mapping" do
	code <<-EOH	
		curl -i -X PUT -d '{ "order": 1, "template": "monitoring_events-*", "settings": {}, "mappings":
			{
				"monitoringevent": {
					"properties": {
						"hostid": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
						"hostcpu": { "include_in_all": true,  "type": "long" },
						"hostmem": { "include_in_all": true,  "type": "double" },
						"hostdisk": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
						"jvmid": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
						"jvmcpu": { "include_in_all": true,"type": "long" },
						"jvmmem": { "include_in_all": true, "type": "long" },
						"jvmtnum": { "include_in_all": true, "type": "integer" },
						"appid": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
						"appstat": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
						"timestamp": { "include_in_all": true, "index": "not_analyzed", "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" }
					},
					"_all": { "enabled": true } 
				}
			},
			"aliases": { "last_3_months": {} }
		}' http://localhost:9200/_template/monitoring_template
	EOH
end
