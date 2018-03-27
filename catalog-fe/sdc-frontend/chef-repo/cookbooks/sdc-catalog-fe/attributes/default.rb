#
default['BE'][:http_port] = 8080
default['BE'][:https_port] = 8443
default['FE'][:http_port] = 8181
default['FE'][:https_port] = 9443
default['disableHttp'] = true
default['jetty'][:keystore_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"
default['jetty'][:keymanager_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"
default['jetty'][:truststore_pwd] = "OBF:1cp61iuj194s194u194w194y1is31cok"
