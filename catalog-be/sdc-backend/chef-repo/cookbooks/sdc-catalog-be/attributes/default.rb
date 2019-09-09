#
default['BE'][:http_port] = 8080
default['BE'][:https_port] = 8443
default['FE'][:http_port] = 8181
default['FE'][:https_port] = 9443
default['disableHttp'] = true
default['cassandra'][:truststore_password] = "Aa123456"
default['jetty'][:keystore_pwd] = "!ppJ.JvWn0hGh)oVF]([Kv)^"
default['jetty'][:keymanager_pwd] = "!ppJ.JvWn0hGh)oVF]([Kv)^"
# TO CHANGE THE TRUSTSTORE CERT THE JVM CONFIGURATION
# MUST BE ALSO CHANGE IN THE startup.sh FILE
default['jetty'][:truststore_pwd] = "].][xgtze]hBhz*wy]}m#lf*"

#Reserved for DCAE backend
default['DCAE']['BE'][:http_port] = 8082
default['DCAE']['BE'][:https_port] = 8444
default['DCAE_BE_VIP'] = "dcae-be"

#Cassandra
default['cassandra']['cassandra_port'] = 9042
default['cassandra']['datacenter_name'] = "DC-"
default['cassandra']['cluster_name'] = "SDC-CS-"
default['cassandra']['socket_read_timeout'] = 20000
default['cassandra']['socket_connect_timeout'] = 20000
default['cassandra']['janusgraph_connection_timeout'] = 10000

#Elasticsearch
default['elasticsearch']['cluster_name'] = "SDC-ES-"

#Onboard
default['ONBOARDING_BE'][:http_port] = 8081
default['ONBOARDING_BE'][:https_port] = 8445

#UEB
default['UEB']['PublicKey'] = "sSJc5qiBnKy2qrlc"
default['UEB']['SecretKey'] = "4ZRPzNJfEUK0sSNBvccd2m7X"

default['Pair_EnvName'] = ""

#DmaapConsumer
default['DMAAP']['active'] = false

#Portal
default['ECompP']['cipher_key'] = "AGLDdG4D04BKm2IxIWEr8o=="
default['ECompP']['portal_user'] = "Ipwxi2oLvDxctMA1royaRw1W0jhucLx+grHzci3ePIA="
default['ECompP']['portal_pass'] = "j85yNhyIs7zKYbR1VlwEfNhS6b7Om4l0Gx5O8931sCI="
default['ECompP']['portal_app_name'] = "Ipwxi2oLvDxctMA1royaRw1W0jhucLx+grHzci3ePIA="
