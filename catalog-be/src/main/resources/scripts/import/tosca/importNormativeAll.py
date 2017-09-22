import pycurl
import sys, getopt, os
from StringIO import StringIO
import json
import copy
import time
#from importNormativeElements import createNormativeElement
from importNormativeElements import * 
from importNormativeTypes import importNormativeTypes
from importHeatTypes import importHeatTypes
from importOnapTypes import importOnapTypes
from importNormativeCapabilities import importNormativeCapabilities
from importCategoryTypes import importCategories
from importNormativeInterfaceLifecycleTypes import importNormativeInterfaceLifecycleType
from importDataTypes import importDataTypes
from importGroupTypes import importGroupTypes
from importPolicyTypes import importPolicyTypes
from importCommon import *
import importCommon

#################################################################################################################################################################################################
#																																		       													#	
# Import all users from a given file																										   													#
# 																																			   													#		
# activation :																																   													#
#       python importNormativeAll.py [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ] [-d <true|false> | --debug=<true|false>] 	#
#									 [-v <true|false> | --updateversion=<true|false>]																											#
#																																		  	   													#			
# shortest activation (be host = localhost, be port = 8080, user = jh0003): 																				   									#	#												       																																			#
#		python importNormativeAll.py											 				           																						#
#																																		       													#	
#################################################################################################################################################################################################

def usage():
	print sys.argv[0], '[-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ] [-d <true|false> | --debug=<true|false>] [-v <true|false> | --updateversion=<true|false>]'

def handleResults(results, updateversion):
	printFrameLine()
	for result in results:
		printNameAndReturnCode(result[0], result[1])
	printFrameLine()
	
	responseCodes = [200, 201]
	
	if(updateversion == 'false'):
		responseCodes = [200, 201, 409]
	
	failedResults = filter(lambda x: x[1] == None or x[1] not in responseCodes, results)
	if (len(failedResults) > 0):
		errorAndExit(1, None)

def main(argv):
	print 'Number of arguments:', len(sys.argv), 'arguments.'

	beHost = 'localhost' 
	bePort = '8080'
	adminUser = 'jh0003'
	debugf = None
	updateversion = 'true'
	importCommon.debugFlag = False 

	try:
		opts, args = getopt.getopt(argv,"i:p:u:d:v:h",["ip=","port=","user=","debug=","updateversion="])
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
		elif opt in ("-d", "--debug"):
			print arg
			debugf = bool(arg.lower() == "true" or arg.lower() == "yes")
		elif opt in ("-v", "--updateversion"):
			print arg
			if (arg.lower() == "false" or arg.lower() == "no"):
				updateversion = 'false'			

	print 'be host =',beHost,', be port =', bePort,', user =', adminUser, ', debug =', debugf, ', updateversion =', updateversion

	if (debugf != None):
		print 'set debug mode to ' + str(debugf)
		importCommon.debugFlag = debugf
	
	if ( beHost == None ):
		usage()
		sys.exit(3)

	print sys.argv[0]
	pathdir = os.path.dirname(os.path.realpath(sys.argv[0]))      
	debug("path dir =" + pathdir)

	baseFileLocation = pathdir + "/../../../import/tosca/"

	fileLocation = baseFileLocation + "data-types/"
	importDataTypes(beHost, bePort, adminUser, False, fileLocation)

	print 'sleep until data type cache is updated'
	time.sleep( 70 )
	
	fileLocation = baseFileLocation + "capability-types/"
	importNormativeCapabilities(beHost, bePort, adminUser, False, fileLocation)

	fileLocation = baseFileLocation + "interface-lifecycle-types/"
	importNormativeInterfaceLifecycleType(beHost, bePort, adminUser, False, fileLocation)	

	fileLocation = baseFileLocation + "categories/"
	importCategories(beHost, bePort, adminUser, False, fileLocation)
	
	fileLocation = baseFileLocation + "normative-types/"
	results = importNormativeTypes(beHost, bePort, adminUser, fileLocation, updateversion)
	handleResults(results, updateversion)

	fileLocation = baseFileLocation + "heat-types/"
	resultsHeat = importHeatTypes(beHost, bePort, adminUser, fileLocation, updateversion)
	handleResults(resultsHeat, updateversion)
	
	fileLocation = baseFileLocation + "onap-types/"
	resultsHeat = importOnapTypes(beHost, bePort, adminUser, fileLocation, updateversion)
	handleResults(resultsHeat, updateversion)
  
	fileLocation = baseFileLocation + "group-types/"
	importGroupTypes(beHost, bePort, adminUser, False, fileLocation)
	
	fileLocation = baseFileLocation + "policy-types/"
	importPolicyTypes(beHost, bePort, adminUser, False, fileLocation)

	errorAndExit(0, None)	

if __name__ == "__main__":
        main(sys.argv[1:])

