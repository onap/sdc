import pycurl
import sys, getopt, os
from StringIO import StringIO
import json
import copy
import time
from importCategoryTypes import importCategories
from upgradeHeatTypes1707 import upgradeHeatTypes1707
from importDataTypes import importDataTypes

from importCommon import *
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

	fileLocation = baseFileLocation + "categories/"
	importCategories(beHost, bePort, adminUser, False, fileLocation)

	fileLocation = baseFileLocation + "data-types/"
	importDataTypes(beHost, bePort, adminUser, False, fileLocation)

	print 'sleep until data type cache is updated'
	time.sleep( 70 )

	fileLocation = baseFileLocation + "heat-types/"
	resultsHeat = upgradeHeatTypes1707(beHost, bePort, adminUser, fileLocation, updateversion)
	handleResults(resultsHeat, 'false')

	errorAndExit(0, None)	

if __name__ == "__main__":
        main(sys.argv[1:])

