import pycurl
import sys, getopt
from StringIO import StringIO
import json
import copy
from importCommon import *
import importCommon
################################################################################################################################################
#																																		       #	
# Import all users from a given file																										   #
# 																																			   #		
# activation :																																   #
#       python importUsers.py [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]     #
#							  [-v <true|false> | --updateversion=<true|false>]																											  	   #			
# shortest activation (be host = localhost, be port = 8080): 																				   #																       #
#		python importUsers.py [-f <input file> | --ifile=<input file> ]												 				           #
#																																		       #	
################################################################################################################################################

def createNormativeType(beHost, bePort, adminUser, fileDir, ELEMENT_NAME, updateversion):
	
	try:
		log("in create normative type ", ELEMENT_NAME)
		debug("userId", adminUser)
		debug("fileDir", fileDir)
		
		buffer = StringIO()
		c = pycurl.Curl()

		url = 'http://' + beHost + ':' + bePort + '/sdc2/rest/v1/catalog/upload/multipart'
		if updateversion != None:
			url += '?createNewVersion=' + updateversion
		c.setopt(c.URL, url)
		c.setopt(c.POST, 1)		

		adminHeader = 'USER_ID: ' + adminUser
		#c.setopt(pycurl.HTTPHEADER, ['Content-Type: application/json', 'Accept: application/json', adminHeader])
		c.setopt(pycurl.HTTPHEADER, [adminHeader])


		path = fileDir + ELEMENT_NAME + "/" + ELEMENT_NAME + ".zip"
		debug(path)
        	CURRENT_JSON_FILE=fileDir + ELEMENT_NAME + "/" + ELEMENT_NAME + ".json"
        	#sed -i 's/"userId": ".*",/"userId": "'${USER_ID}'",/' ${CURRENT_JSON_FILE}

		jsonFile = open(CURRENT_JSON_FILE)
		
		debug("before load json")
		json_data = json.load(jsonFile, strict=False)
		debug(json_data)
	
		jsonAsStr = json.dumps(json_data)

		send = [('resourceMetadata', jsonAsStr), ('resourceZip', (pycurl.FORM_FILE, path))]
		debug(send)
		c.setopt(pycurl.HTTPPOST, send)		

		#data = json.dumps(user)
		#c.setopt(c.POSTFIELDS, data)	

		#c.setopt(c.WRITEFUNCTION, lambda x: None)
		c.setopt(c.WRITEFUNCTION, buffer.write)
		#print("before perform")	
		res = c.perform()
	
		#print("Before get response code")	
		httpRes = c.getinfo(c.RESPONSE_CODE)
		if (httpRes != None):
			debug("http response=", httpRes)
		#print('Status: ' + str(responseCode))
		debug(buffer.getvalue())
		c.close()

		return (ELEMENT_NAME, httpRes, buffer.getvalue())

	except Exception as inst:
		print("ERROR=" + str(inst))
		return (ELEMENT_NAME, None, None)				


def usage():
	print sys.argv[0], '[-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ] [-v <true|false> | --updateversion=<true|false>]'


def importNormativeTypes(beHost, bePort, adminUser, fileDir, updateversion):
	
	normativeTypes = [ "root", "compute", "softwareComponent", "webServer", "webApplication", "DBMS", "database", "objectStorage", "blockStorage", "containerRuntime", "containerApplication", "loadBalancer", "port", "network", "allottedResource"]
	#normativeTypes = [ "root" ]
	responseCodes = [200, 201]
	
	if(updateversion == 'false'):
		responseCodes = [200, 201, 409]
	
        results = []
        for normativeType in normativeTypes:
                result = createNormativeType(beHost, bePort, adminUser, fileDir, normativeType, updateversion)
                results.append(result)
                if ( result[1] == None or result[1] not in responseCodes ):
			print "Failed creating normative type " + normativeType + ". " + str(result[1]) 				
	return results


def main(argv):
	print 'Number of arguments:', len(sys.argv), 'arguments.'

	beHost = 'localhost' 
	bePort = '8080'
	adminUser = 'jh0003'
	updateversion = 'true'

	try:
		opts, args = getopt.getopt(argv,"i:p:u:v:h:",["ip=","port=","user=","updateversion="])
	except getopt.GetoptError:
		usage()
		errorAndExit(2, 'Invalid input')
		 
	for opt, arg in opts:
	#print opt, arg
		if opt == '-h':
			usage()                        
			sys.exit(3)
		elif opt in ("-i", "--ip"):
			beHost = arg
		elif opt in ("-p", "--port"):
			bePort = arg
		elif opt in ("-u", "--user"):
			adminUser = arg
		elif opt in ("-v", "--updateversion"):
			if (arg.lower() == "false" or arg.lower() == "no"):
				updateversion = 'false'

	print 'be host =',beHost,', be port =', bePort,', user =', adminUser, ', updateversion =', updateversion
	
	if ( beHost == None ):
		usage()
		sys.exit(3)

	results = importNormativeTypes(beHost, bePort, adminUser, "../../../import/tosca/normative-types/", updateversion)

	print "-----------------------------"
	for result in results:
		print "{0:20} | {1:6}".format(result[0], result[1])
	print "-----------------------------"
	
	responseCodes = [200, 201]
	
	if(updateversion == 'false'):
		responseCodes = [200, 201, 409]
	
	failedNormatives = filter(lambda x: x[1] == None or x[1] not in responseCodes, results)
	if (len(failedNormatives) > 0):
		errorAndExit(1, None)
	else:
		errorAndExit(0, None)


if __name__ == "__main__":
        main(sys.argv[1:])

