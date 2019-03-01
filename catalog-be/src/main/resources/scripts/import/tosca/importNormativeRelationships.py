import pycurl
import sys, getopt
from StringIO import StringIO
import json
import copy
from importNormativeElements import createNormativeElement
from importCommon import *
import importCommon 

#################################################################################################################################################################################################
#																																		       													#
# Import normative relationships
# 				   															#
# 																																			   													#
# activation :																																   													#
#       python importNormativeRelationships.py [-s <scheme> | --scheme=<scheme> ] [-i <be host> | --ip=<be host>] [-p
#  <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]	#
#																																		  	  													#
# shortest activation (be host = localhost, be port = 8080): 																				   													#
#		python importNormativeRelationships.py [-f <input file> | --ifile=<input file> ]
# 					 				           									#
#																																		       													#
#################################################################################################################################################################################################

	
def usage():
	print sys.argv[0], '[optional -s <scheme> | --scheme=<scheme>, default http] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ]'


def importNormativeRelationships(scheme, beHost, bePort, adminUser, exitOnSuccess, fileDir):
	result = createNormativeElement(scheme, beHost, bePort, adminUser, fileDir, "/sdc2/rest/v1/catalog/uploadType/relationship", "relationshipTypes", "relationshipTypeZip")
	
	print_frame_line()
        print_name_and_return_code(result[0], result[1])
        print_frame_line()

        if ( result[1] == None or result[1] not in [200, 201, 409] ):
                importCommon.error_and_exit(1, None)
        else:
		if (exitOnSuccess == True):
                	importCommon.error_and_exit(0, None)


def main(argv):
	print 'Number of arguments:', len(sys.argv), 'arguments.'

	beHost = 'localhost' 
	bePort = '8080'
	adminUser = 'jh0003'
	scheme = 'http'

	try:
		opts, args = getopt.getopt(argv,"i:p:u:h:s:",["ip=","port=","user=","scheme="])
	except getopt.GetoptError:
		usage()
		importCommon.error_and_exit(2, 'Invalid input')
		 
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
		elif opt in ("-s", "--scheme"):
			scheme = arg

	print 'scheme =',scheme,', be host =',beHost,', be port =', bePort,', user =', adminUser
	
	if ( beHost == None ):
		usage()
		sys.exit(3)

	importNormativeRelationships(scheme, beHost, bePort, adminUser, True, "../../../import/tosca/relationship-types/")


if __name__ == "__main__":
        main(sys.argv[1:])

