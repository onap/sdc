import pycurl
import sys, getopt, os
from StringIO import StringIO
import json
import copy
from importCommon import *
from importNormativeTypes import createNormativeType
import importCommon

#################################################################################################################################################################################################
#																																		       													#	
# Upgrades the normative types																										   															#
# 																																			   													#		
# activation :																																   													#
#       python upgradeNormative.py [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ] [-d <true|false> | --debug=<true|false>] 		#
#																																																#
#																																		  	   													#			
# shortest activation (be host = localhost, be port = 8080, user = jh0003): 																				   									#	#												       																																			#
#		python upgradeNormative.py												 				           																						#
#																																		       													#	
#################################################################################################################################################################################################

def usage():
	print sys.argv[0], '[-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ] [-d <true|false> | --debug=<true|false>]'

def handleResults(results, updateversion):
	printFrameLine()
	for result in results:
		printNameAndReturnCode(result[0], result[1])
	printFrameLine()
	
	failedResults = filter(lambda x: x[1] == None or x[1] not in [200, 201, 409], results)
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
		opts, args = getopt.getopt(argv,"i:p:u:d:h",["ip=","port=","user=","debug="])
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

	print 'be host =',beHost,', be port =', bePort,', user =', adminUser, ', debug =', debugf

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
	results = []


	##########################################################################
    #---------------------------------for release 1702---------------------- #
    ##########################################################################

	fileLocation = baseFileLocation + "heat-types/"
	result = createNormativeType(beHost, bePort, adminUser, fileLocation, "contrailV2VirtualMachineInterface", updateversion)
	results.append(result)
	
	fileLocation = baseFileLocation + "heat-types/"
	result = createNormativeType(beHost, bePort, adminUser, fileLocation, "neutronPort", updateversion)
	results.append(result)





	handleResults(results, 'false')

	errorAndExit(0, None)	

if __name__ == "__main__":
        main(sys.argv[1:])

