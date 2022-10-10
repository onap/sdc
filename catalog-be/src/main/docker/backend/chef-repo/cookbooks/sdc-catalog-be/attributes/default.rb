#
default['BE'][:http_port] = 8080
default['BE'][:https_port] = 8443
default['FE'][:http_port] = 8181
default['FE'][:https_port] = 9443
default['disableHttp'] = true


#+----------------------------------+
#|                                  |
#|  Jetty                           |
#|                                  |
#+----------------------------------+

default['jetty']['dmaap_truststore_pwd'] = "dmaap_truststore_pwd"
default['jetty']['keystore_pwd'] = "?(kP!Yur![*!Y5!E^f(ZKc31"
default['jetty']['keymanager_pwd'] = "?(kP!Yur![*!Y5!E^f(ZKc31"
default['jetty']['truststore_path'] = "#{ENV['JETTY_BASE']}/etc/truststore"
# TO CHANGE THE TRUSTSTORE CERT THE JVM CONFIGURATION
# MUST BE ALSO CHANGE IN THE startup.sh FILE
default['jetty'][:truststore_pwd] = "z+KEj;t+,KN^iimSiS89e#p0"

#BasicAuth
default['basic_auth']['enabled'] = false
default['basic_auth'][:user_name] = "testName"
default['basic_auth'][:user_pass] = "testPass"
default['basic_auth']['excludedUrls'] = "/sdc2/rest/healthCheck"

#Cassandra
default['cassandra']['cassandra_port'] = 9042
default['cassandra']['datacenter_name'] = "DC-"
default['cassandra']['cluster_name'] = "SDC-CS-"
default['cassandra']['socket_read_timeout'] = 20000
default['cassandra']['socket_connect_timeout'] = 20000
default['cassandra']['janusgraph_connection_timeout'] = 10000
default['cassandra'][:truststore_password] = "Aa123456"
default['cassandra']['janusgraph_config_file'] = "#{ENV['JETTY_BASE']}/config/catalog-be/janusgraph.properties"
default['cassandra'][:db_cache] = false
default['cassandra'][:read_consistency_level] = "ONE"
default['cassandra'][:write_consistency_level] = "ALL"


#Onboard
default['ONBOARDING_BE'][:http_port] = 8081
default['ONBOARDING_BE'][:https_port] = 8445

#UEB
default['UEB']['PublicKey'] = "sSJc5qiBnKy2qrlc"
default['UEB']['SecretKey'] = "4ZRPzNJfEUK0sSNBvccd2m7X"

default['Pair_EnvName'] = ""

#+----------------------------------+
#|                                  |
#|  Portal                          |
#|                                  |
#+----------------------------------+

default['ECompP']['cipher_key'] = "AGLDdG4D04BKm2IxIWEr8o=="
default['ECompP']['portal_user'] = "Ipwxi2oLvDxctMA1royaRw1W0jhucLx+grHzci3ePIA="
default['ECompP']['portal_pass'] = "j85yNhyIs7zKYbR1VlwEfNhS6b7Om4l0Gx5O8931sCI="
default['ECompP']['portal_app_name'] = "Ipwxi2oLvDxctMA1royaRw1W0jhucLx+grHzci3ePIA="


#+----------------------------------+
#|                                  |
#|  DMAAP Consumer                  |
#|                                  |
#+----------------------------------+

default['DMAAP']['active'] = false
default['DMAAP']['consumer']['aftEnvironment'] = "AFTUAT"
default['DMAAP']['consumer']['consumerGroup'] = "ccd_onap"
default['DMAAP']['consumer']['consumerId'] = "ccd_onap"
default['DMAAP']['consumer']['dme2preferredRouterFilePath'] = "DME2preferredRouter.txt"
default['DMAAP']['consumer']['environment'] = "TEST"
default['DMAAP']['consumer']['host'] = "dmaap.onap.com"
default['DMAAP']['consumer']['password'] = "password"
default['DMAAP']['consumer']['port'] = 3905
default['DMAAP']['consumer']['serviceName'] = "dmaap-v1.dev.dmaap.dt.saat.acsi.onap.com/events"
default['DMAAP']['consumer']['topic'] = "com.onap.ccd.CCD-CatalogManagement-v1"
default['DMAAP']['consumer']['username'] = "user"
default['DMAAP']['partitioncount'] = "3"
default['DMAAP']['replicationcount'] = "3"


#+----------------------------------+
#|                                  |
#|  Access Restriction  / CADI      |
#|                                  |
#+----------------------------------+

# Cadi
default['access_restriction']['cadi_root_dir'] = "#{ENV['JETTY_BASE']}/etc"
default['access_restriction']['cadi_keyfile'] = "#{ENV['JETTY_BASE']}/etc/org.onap.sdc.p12"
default['access_restriction']['cadi_loglevel'] = "DEBUG"
default['access_restriction']['cadi_truststore'] = "#{ENV['JETTY_BASE']}/etc/org.onap.sdc.trust.jks"
default['access_restriction']['cadi_truststore_password'] = "changeit"
default['access_restriction']['cadiX509Issuers'] = "CN=intermediateCA_1, OU=OSAAF, O=ONAP, C=US"
default['access_restriction']['encrypted_password'] = "enc:AccessRestrictionEncryptedPassword"
default['access_restriction_key'] = nil

# Access Restriction Key
default['aafNamespace']  = "com.onap.sdc"
default['access_restriction']['aaf_env'] = "TEST"
default['access_restriction']['aaf_id'] = "user"
default['access_restriction']['aaf_locate_url'] = ""
default['access_restriction']['aaf_password'] = "enc:AafEncriptedPassword"
default['access_restriction']['aaf_url'] = ""
default['access_restriction']['aafAuthNeeded'] = false
default['access_restriction']['AFT_DME2_CLIENT_IGNORE_SSL_CONFIG'] = true
default['access_restriction']['AFT_DME2_HTTP_EXCHANGE_TRACE_ON'] = true
default['access_restriction']['AFT_ENVIRONMENT'] = "AFTUAT"
default['access_restriction']['csp_domain'] = "PROD"
default['access_restriction']['excluded_urls'] = "'/.*'"
default['access_restriction']['excluded_urls_onboarding'] = "'/.*'"

