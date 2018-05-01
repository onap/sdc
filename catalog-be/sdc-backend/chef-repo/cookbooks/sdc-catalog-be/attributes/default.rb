#
default['BE'][:http_port] = 8080
default['BE'][:https_port] = 8443
default['FE'][:http_port] = 8181
default['FE'][:https_port] = 9443
default['disableHttp'] = true
default['cassandra'][:truststore_password] = "Aa123456"
default['jetty'][:keystore_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"
default['jetty'][:keymanager_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"
default['jetty'][:truststore_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"

#Reserved for DCAE backend
default['DCAE']['BE'][:http_port] = 8082
default['DCAE']['BE'][:https_port] = 8444
default['DCAE_BE_VIP'] = "dcaed-be"