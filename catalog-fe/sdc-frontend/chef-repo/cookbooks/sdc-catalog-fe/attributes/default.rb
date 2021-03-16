#
default['BE'][:http_port] = 8080
default['BE'][:https_port] = 8443
default['FE'][:http_port] = 8181
default['FE'][:https_port] = 9443
default['disableHttp'] = true
default['jetty'][:keystore_pwd] = "?(kP!Yur![*!Y5!E^f(ZKc31"
default['jetty'][:keymanager_pwd] = "?(kP!Yur![*!Y5!E^f(ZKc31"
# TO CHANGE THE TRUSTSTORE CERT THE JVM CONFIGURATION
# MUST BE ALSO CHANGE IN THE startup.sh FILE
default['jetty'][:truststore_pwd] = "z+KEj;t+,KN^iimSiS89e#p0"

#Onboard
default['ONBOARDING_BE'][:http_port] = 8081
default['ONBOARDING_BE'][:https_port] = 8445

#BasicAuth
default['basic_auth']['enabled'] = true
default['basic_auth'][:user_name] = "testName"
default['basic_auth'][:user_pass] = "testPass"