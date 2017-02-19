import pycurl
import sys, getopt, os
from StringIO import StringIO
import json
import copy
from importCommon import *
import importCommon
import zipfile
################################################################################################################################################
#																																		       #	
################################################################################################################################################

def createZipFromYml(ymlFile, zipFile):
	zip = zipfile.ZipFile(zipFile, 'w', zipfile.ZIP_DEFLATED)
	
    	zip.write(ymlFile, os.path.basename(ymlFile)) 
	zip.close()

def createUserNormativeType(beHost, bePort, adminUser, fileDir, ELEMENT_NAME):
	
	try:
		log("in create normative type ", ELEMENT_NAME)
		debug("userId", adminUser)
		debug("fileDir", fileDir)
		
		buffer = StringIO()
		c = pycurl.Curl()

		url = 'http://' + beHost + ':' + bePort + '/sdc2/rest/v1/catalog/upload/multipart'
		c.setopt(c.URL, url)
		c.setopt(c.POST, 1)		

		adminHeader = 'USER_ID: ' + adminUser
		#c.setopt(pycurl.HTTPHEADER, ['Content-Type: application/json', 'Accept: application/json', adminHeader])
		c.setopt(pycurl.HTTPHEADER, [adminHeader])

		ymlFile = fileDir + ELEMENT_NAME + "/normative-types-new-" + ELEMENT_NAME + ".yml"
		zipFile = fileDir + ELEMENT_NAME + "/normative-types-new-" + ELEMENT_NAME + ".zip"
		debug(ymlFile)
		debug(zipFile)
		path = zipFile 
		debug("path=" + path)
        	CURRENT_JSON_FILE=fileDir + ELEMENT_NAME + "/" + ELEMENT_NAME + ".json"
		debug(CURRENT_JSON_FILE)
		jsonFile = open(CURRENT_JSON_FILE)
		
		debug("before load json")
		json_data = json.load(jsonFile, strict=False)
		debug(json_data)
	
		jsonAsStr = json.dumps(json_data)
		debug(path)
		send = [('resourceMetadata', jsonAsStr), ('resourceZip', (pycurl.FORM_FILE, path))]
		debug(send)
		c.setopt(pycurl.HTTPPOST, send)		

		c.setopt(c.WRITEFUNCTION, buffer.write)
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
	print sys.argv[0], '[-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ] [-l <directory base location> | --location=<directory base location>] [-e <element name> | --element=<element name>]'
	print "----------------- Example -------------------"
	print "python importNodeType.py -d false -l  /home/vagrant/catalog-be-1604.0.2.15.6-SNAPSHOT/scripts/import/tosca/../../../import/tosca/user-normative-types/ -e root1"

def main(argv):
	print 'Number of arguments:', len(sys.argv), 'arguments.'

	beHost = 'localhost' 
	bePort = '8080'
	adminUser = 'jh0003'
	debugf = None
	location = None
	element = None

	try:
		opts, args = getopt.getopt(argv,"i:p:u:d:l:e:h",["ip=","port=","user=","location=","element=", "debug="])
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
		elif opt in ("-l", "--location"):
			location = arg
		elif opt in ("-e", "--element"):
			element = arg
		elif opt in ("-d", "--debug"):
                        print arg
                        debugf = bool(arg.lower() == "true" or arg.lower() == "yes")

	print 'be host =',beHost,', be port =', bePort,', user =', adminUser
	
	if ( beHost == None ):
		usage()
		sys.exit(3)

	if (debugf != None):
		print 'set debug mode to ' + str(debugf)
		importCommon.debugFlag = debugf

	if (location == None):
		print 'Missing file location'
		usage()
		sys.exit(3)
		
	if (element == None):
		print 'Missing element name. E.g. root, compute, ...'
		usage()
		sys.exit(3)

	#pathdir = os.path.dirname(os.path.realpath(sys.argv[0]))

	#baseFileLocation = pathdir + "/../../../import/tosca/"
        #fileDir = baseFileLocation + "user-normative-types/"
	
	#normativeType = "root1"	

	result = createUserNormativeType(beHost, bePort, adminUser, location, element)
	#result = createUserNormativeType(beHost, bePort, adminUser, fileDir, normativeType)
	print "---------------------------------------"
	print "{0:30} | {1:6}".format(result[0], result[1])
	print "---------------------------------------"

	if ( result[1] == None or result[1] not in [200, 201] ) :
		print "Failed creating normative type " + element + ". " + str(result[1]) 				
		errorAndExit(1, None)

	errorAndExit(0, None)

if __name__ == "__main__":
        main(sys.argv[1:])

