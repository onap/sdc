#
default['BE'][:http_port] = 8081
default['BE'][:https_port] = 9445
default['disableHttp'] = true
default['cassandra'][:truststore_password] = "OBF:1cp61iuj194s194u194w194y1is31cok"
default['jetty'][:keystore_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"
default['jetty'][:keymanager_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"
default['jetty'][:truststore_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"
