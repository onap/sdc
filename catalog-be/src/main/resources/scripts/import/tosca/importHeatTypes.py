import pycurl
import sys, getopt
from StringIO import StringIO
import json
import copy
from importCommon import *
from importNormativeTypes import *
import importCommon

################################################################################################################################################
#																																		       #	
# Import all users from a given file																										   #
# 																																			   #		
# activation :																																   #
#       python importUsers.py [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]     #
#																																		  	   #			
# shortest activation (be host = localhost, be port = 8080): 																				   #
#		python importUsers.py [-f <input file> | --ifile=<input file> ]												 				           #
#																																		       #	
################################################################################################################################################

def importHeatTypes(beHost, bePort, adminUser, fileDir, updateversion):
	
	heatTypes = [ "globalNetwork",
				  "globalPort",
				  "globalCompute",
				  "volume",
				  "cinderVolume",
				  "contrailVirtualNetwork",
				  "neutronNet",
				  "neutronPort",
				  "novaServer",
				  "extVl",
				  "internalVl",
				  "extCp",
				  "vl",
				  "eline",
				  "abstractSubstitute",
				  "Generic_VFC", 
				  "Generic_VF",
				  "Generic_PNF",
				  "Generic_Service",
				  "contrailNetworkRules",
				  "contrailPort",
				  "portMirroring",
				  "serviceProxy",
				  "contrailV2NetworkRules",
				  "contrailV2VirtualNetwork",
				  "securityRules",
				  "contrailAbstractSubstitute",
				  "contrailCompute",
				  "contrailV2VirtualMachineInterface",
				  "subInterface",
				  "contrailV2VLANSubInterface",
				  "multiFlavorVFC",
				  "vnfConfiguration",
				  "extCp2",
				  "extNeutronCP",
                  "extContrailCP",
				  "portMirroringByPolicy"
				  ]
		
	responseCodes = [200, 201]
		
	if(updateversion == 'false'):
		responseCodes = [200, 201, 409]
		
        results = []
        for heatType in heatTypes:
                result = createNormativeType(beHost, bePort, adminUser, fileDir, heatType, updateversion)
                results.append(result)
                if ( result[1] == None or result[1] not in responseCodes) :
			print "Failed creating heat type " + heatType + ". " + str(result[1]) 				
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

	print 'be host =',beHost,', be port =', bePort,', user =', adminUser
	
	if ( beHost == None ):
		usage()
		sys.exit(3)

	results = importHeatTypes(beHost, bePort, adminUser, "../../../import/tosca/heat-types/", updateversion)

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


