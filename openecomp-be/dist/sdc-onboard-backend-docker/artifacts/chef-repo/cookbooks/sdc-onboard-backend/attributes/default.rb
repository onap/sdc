#
default['ONBOARDING_BE'][:http_port] = 8081
default['ONBOARDING_BE'][:https_port] = 8445
default['FE'][:http_port] = 8181
default['FE'][:https_port] = 9443
default['disableHttp'] = true
default['cassandra'][:truststore_password] = "Aa123456"
default['jetty'][:keystore_pwd] = "?(kP!Yur![*!Y5!E^f(ZKc31"
default['jetty'][:keymanager_pwd] = "?(kP!Yur![*!Y5!E^f(ZKc31"
default['jetty'][:truststore_pwd] = "z+KEj;t+,KN^iimSiS89e#p0"

default['VnfRepo']['vnfRepoPort'] = 8702
default['VnfRepo']['vnfRepoHost'] = "refrepo"

#Cassandra
default['cassandra']['cassandra_port'] = 9042
default['cassandra']['datacenter_name'] = "DC-"
default['cassandra']['cluster_name'] = "SDC-CS-"
default['cassandra']['socket_read_timeout'] = 20000
default['cassandra']['socket_connect_timeout'] = 20000
default['cassandra']['janusgraph_connection_timeout'] = 10000

#ExternalTesting
default['EXTTEST']['ep1_config'] = "vtp,VTP,true,http://refrepo:8702/onapapi/vnfsdk-marketplace,onap.*"
default['EXTTEST']['ep2_config'] = "repository,Repository,false,,.*"
