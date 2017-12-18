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


bash "create audit mapping" do
	code <<-EOH
		curl -i -X PUT -d '{ "order": 1, "template": "auditingevents-*", "settings": {}, "mappings":
	{
		"distributiondownloadevent": { 
			"properties": {
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"SERVICE_INSTANCE_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"RESOURCE_URL": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"CONSUMER_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" }
			},
			"_all": { "enabled": true } 
		},
		"auditinggetuebclusterevent": { 
			"properties": {
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"SERVICE_INSTANCE_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"CONSUMER_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" }
			},
			"_all": { "enabled": true } 
		},
		"distributionstatusevent": {
			"properties": {
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"SERVICE_INSTANCE_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"RESOURCE_URL": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"TOPIC_NAME":{ "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"CONSUMER_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" }
			},
			"_all": { "enabled": true } 
		},
		"distributionengineevent": {
			"properties": {
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"SERVICE_INSTANCE_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"TOPIC_NAME":{ "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"ROLE": { "include_in_all": true, "type": "string" },
				"API_KEY": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"D_ENV": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"CONSUMER_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" }
			},
			"_all": { "enabled": true } 
		},
		"useraccessevent": { 
			"properties": {
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"SERVICE_INSTANCE_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"USER": { "include_in_all": true, "type": "string" }
			},
			"_all": { "enabled": true }
		},
		"resourceadminevent": {
			"properties": {
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"SERVICE_INSTANCE_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"INVARIANT_UUID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"CURR_VERSION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"CURR_STATE": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"MODIFIER": { "include_in_all": true, "type": "string" },
				"PREV_VERSION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"PREV_STATE": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"RESOURCE_NAME": { "include_in_all": true, "type": "string" },
				"RESOURCE_TYPE": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DPREV_STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DCURR_STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"TOSCA_NODE_TYPE": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"COMMENT": { "include_in_all": true, "type": "string" },
				"ARTIFACT_DATA": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"PREV_ARTIFACT_UUID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"CURR_ARTIFACT_UUID": { "include_in_all": true, "index": "not_analyzed", "type": "string" } 
			},
			"_all": { "enabled": true }
		},
		"useradminevent": {
			"properties": {
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"SERVICE_INSTANCE_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"USER_AFTER": { "include_in_all": true, "type": "string" },
				"USER_BEFORE": { "include_in_all": true, "type": "string" },
				"MODIFIER": { "include_in_all": true, "type": "string" }
			},
			"_all": { "enabled": true } 
		},
		"distributionnotificationevent": {
			"properties": {
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" },
                "REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
                "SERVICE_INSTANCE_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
                "ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
                "DESC": { "include_in_all": true, "type": "string" },
                "STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
                "CURR_STATE": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
                "CURR_VERSION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
                "DID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
                "MODIFIER": { "include_in_all": true, "type": "string" },
                "RESOURCE_NAME": { "include_in_all": true, "type": "string" },
                "RESOURCE_TYPE": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
                "TOPIC_NAME":{ "include_in_all": true, "index": "not_analyzed", "type": "string" }
			},
            "_all": { "enabled": true } 
		},
		"categoryevent": {
			"properties": {
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"MODIFIER": { "include_in_all": true, "type": "string" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"CATEGORY_NAME": { "include_in_all": true, "type": "string" },
				"SUB_CATEGORY_NAME": { "include_in_all": true, "type": "string" },
				"GROUPING_NAME": { "include_in_all": true, "type": "string" },
				"RESOURCE_TYPE": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"SERVICE_INSTANCE_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" }
			},
            "_all": { "enabled": true } 
		},
		"authevent": {
			"properties": {
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" },
				"DESC": { "include_in_all": true, "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"URL": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"USER": { "include_in_all": true, "type": "string" },
				"AUTH_STATUS": { "include_in_all": true, "index": "not_analyzed","type": "string" } ,
				"REALM": { "include_in_all": true, "index": "not_analyzed","type": "string" }
			},
			"_all": { "enabled": true }
		},
		"consumerevent": {
			"properties": {
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"MODIFIER": { "include_in_all": true, "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"ECOMP_USER": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" }
			},
			"_all": { "enabled": true } 
		},
		"getuserslistevent": {
			"properties": {
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"MODIFIER": { "include_in_all": true, "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DETAILS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" }
			},
            "_all": { "enabled": true } 
		},
		"getcategoryhierarchyevent": {
			"properties": {
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"MODIFIER": { "include_in_all": true, "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DETAILS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" }
			},
			"_all": { "enabled": true } 
		},
		"distributiondeployevent": {
			"properties": {
				"ACTION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"CURR_VERSION": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"DESC": { "include_in_all": true, "type": "string" },
				"DID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"MODIFIER": { "include_in_all": true, "type": "string" },
				"REQUEST_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"RESOURCE_NAME": { "include_in_all": true, "type": "string" },
				"RESOURCE_TYPE": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"SERVICE_INSTANCE_ID": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"STATUS": { "include_in_all": true, "index": "not_analyzed", "type": "string" },
				"TIMESTAMP": { "include_in_all": true, "ignore_malformed": false, "format": "yyyy-MM-dd HH:mm:ss.SSS z", "precision_step": 4, "type": "date" }
			},
            "_all": { "enabled": true } }
		},
        "aliases": { "last_3_months": {}}}' http://localhost:9200/_template/audit_template
	EOH
end

bash "set default index for Kibana" do
	code <<-EOH
        curl -XPUT http://localhost:9200/.kibana/index-pattern/auditingevents-* -d '{"title" : "events-*",  "timeFieldName": "TIMESTAMP"}'
        curl -XPUT http://localhost:9200/.kibana/config/4.3.3 -d '{"defaultIndex" : "auditingevents-*"}'
    EOH
end
