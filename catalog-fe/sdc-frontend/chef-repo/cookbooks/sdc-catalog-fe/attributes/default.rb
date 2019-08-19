#
default['BE'][:http_port] = 8080
default['BE'][:https_port] = 8443
default['FE'][:http_port] = 8181
default['FE'][:https_port] = 9443
default['disableHttp'] = true
default['jetty'][:keystore_pwd] = "rTIS;B4kM]2GHcNK2c3B4&Ng"
default['jetty'][:keymanager_pwd] = "rTIS;B4kM]2GHcNK2c3B4&Ng"
default['jetty'][:truststore_pwd] = "Y,f975ZNJfVZhV*{+Y[}pA?0"

#Onboard
default['ONBOARDING_BE'][:http_port] = 8081
default['ONBOARDING_BE'][:https_port] = 8445

#Reserved for DCAE backend
default['DCAE']['FE'][:http_port] = 8183
default['DCAE']['FE'][:https_port] = 9444
default['DCAE_FE_VIP'] = "dcaed-fe"
