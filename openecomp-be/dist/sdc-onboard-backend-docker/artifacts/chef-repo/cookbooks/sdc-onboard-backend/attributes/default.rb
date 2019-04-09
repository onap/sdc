#
default['ONBOARDING_BE'][:http_port] = 8081
default['ONBOARDING_BE'][:https_port] = 8445
default['FE'][:http_port] = 8181
default['FE'][:https_port] = 9443
default['disableHttp'] = true
default['cassandra'][:truststore_password] = "Aa123456"
default['jetty'][:keystore_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"
default['jetty'][:keymanager_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"
default['jetty'][:truststore_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"
default['VnfRepo']['vnfRepoPort'] = 8702
default['VnfRepo']['vnfRepoHost'] = "192.168.50.5"

#Cassandra
default['cassandra']['cassandra_port'] = 9042
default['cassandra']['datacenter_name'] = "DC-"
default['cassandra']['cluster_name'] = "SDC-CS-"
default['cassandra']['socket_read_timeout'] = 20000
default['cassandra']['socket_connect_timeout'] = 20000
default['cassandra']['titan_connection_timeout'] = 10000


#ExternalTesting
default['EXTTEST']['ep1_config'] = "vtp,VTP,true,http://192.168.50.5:8702/onapapi/vnfsdk-marketplace,c.*"
default['EXTTEST']['ep2_config'] = "repository,Repository,false,,.*"

