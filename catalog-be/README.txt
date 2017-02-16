#open rpm
#install jetty
#run installJettyBase.sh
#copy jvm.properties to base
#export variables
#run startJetty.sh

#Properties:

    STOP.PORT=[number]
      The port to use to stop the running Jetty server.
      Required along with STOP.KEY if you want to use the --stop option above.

    STOP.KEY=[alphanumeric]
      The passphrase defined to stop the server.
      Requried along with STOP.PORT if you want to use the --stop option above.

    STOP.WAIT=[number]
      The time (in seconds) to wait for confirmation that the running
      Jetty server has stopped. If not specified, the stopper will wait
      indefinitely. Use in conjunction with the --stop option.
      
      
#Upload Normative types:
# 1. create zip file containing the yaml
# 2. create json string (payloadName should be the yml file name): {
#					"payloadName":"normative-types-new-root.yml",
#					"userId":"adminid",
#					"resourceName":"tosca.nodes.Root",
#					"description":"Represents a generic software component that can be managed and run by a Compute Node Type.",
#					"resourceIconPath":"defaulticon",
#					"category":"Abstract",
#					"tags":["Root"]
#					}
# 
#
# 3. run curl command: curl -v -F resourceMetadata=<json string> -F resourceZip=@<zip file location> <BE host:port>/sdc2/rest/v1/catalog/upload/multipart
#    e.g.: 
#	curl -v -F resourceMetadata='{"payloadName":"normative-types-new-root.yml","userId":"adminid","resourceName":"tosca.nodes.Root","description":"Represents a generic software component that can be managed and run by a Compute Node Type.","resourceIconPath":"defaulticon","category":"Abstract","tags":["Root"]}' -F resourceZip=@/var/tmp/normative-types-new-root.zip localhost:8080/sdc2/rest/v1/catalog/upload/multipart

# 	
