import pycurl
import sys, getopt, os
from StringIO import StringIO
import json
import copy
import time
from importCategoryTypes import importCategories
from upgradeHeatAndNormativeTypes import upgradeTypesPerConfigFile
from importDataTypes import importDataTypes
from importPolicyTypes import importPolicyTypes
from importGroupTypes import importGroupTypes
from importNormativeCapabilities import importNormativeCapabilities
from importNormativeRelationships import importNormativeRelationships
from importNormativeInterfaceLifecycleTypes import importNormativeInterfaceLifecycleType
from importAnnotationTypes import import_annotation_types


from importCommon import *
import importCommon

#################################################################################################################################################################################################################################
#																																		       																					#
# Upgrades the normative types																										   																							#
# 																																			   																					#
# activation :																																   																					#
#       python upgradeNormative.py [-s <scheme> | --scheme=<scheme> ] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ] [-d <true|false> | --debug=<true|false>]	#
#																																																								#
#																																		  	   																					#
# shortest activation (be host = localhost, be port = 8080, user = jh0003): 																				   																	#
#		python upgradeNormative.py												 				           																														#
#																																		       																					#
#################################################################################################################################################################################################################################

def usage():
    print sys.argv[0], '[optional -s <scheme> | --scheme=<scheme>, default http] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ] [-d <true|false> | --debug=<true|false>]'

def handleResults(results, updateversion):
    print_frame_line()
    for result in results:
        print_name_and_return_code(result[0], result[1])
    print_frame_line()

    failedResults = filter(lambda x: x[1] == None or x[1] not in [200, 201, 409], results)
    if (len(failedResults) > 0):
        error_and_exit(1, None)

def main(argv):
    print 'Number of arguments:', len(sys.argv), 'arguments.'

    beHost = 'localhost'
    bePort = '8080'
    adminUser = 'jh0003'
    debugf = None
    updateversion = 'true'
    importCommon.debugFlag = False
    scheme = 'http'

    try:
        opts, args = getopt.getopt(argv,"i:p:u:d:h:s:",["ip=","port=","user=","debug=","scheme="])
    except getopt.GetoptError:
        usage()
        error_and_exit(2, 'Invalid input')

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
        elif opt in ("-d", "--debug"):
            print arg
            debugf = bool(arg.lower() == "true" or arg.lower() == "yes")

    print 'scheme =',scheme,', be host =',beHost,', be port =', bePort,', user =', adminUser, ', debug =', debugf

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
    importCategories(scheme, beHost, bePort, adminUser, False, fileLocation)

    fileLocation = baseFileLocation + "relationship-types/"
    importNormativeRelationships(scheme, beHost, bePort, adminUser, False, fileLocation)

    fileLocation = baseFileLocation + "data-types/"
    importDataTypes(scheme, beHost, bePort, adminUser, False, fileLocation)

    fileLocation = baseFileLocation + "policy-types/"
    importPolicyTypes(scheme, beHost, bePort, adminUser, False, fileLocation)

    fileLocation = baseFileLocation + "capability-types/"
    importNormativeCapabilities(scheme, beHost, bePort, adminUser, False, fileLocation)

    fileLocation = baseFileLocation + "group-types/"
    importGroupTypes(scheme, beHost, bePort, adminUser, False, fileLocation)

    fileLocation = baseFileLocation + "interface-lifecycle-types/"
    importNormativeInterfaceLifecycleType(scheme, beHost, bePort, adminUser, False, fileLocation)

    import_annotation_types(scheme, beHost, bePort, adminUser, False)

    print 'sleep until data type cache is updated'
    time.sleep( 70 )

    resultsHeat = upgradeTypesPerConfigFile(scheme, beHost, bePort, adminUser, baseFileLocation, updateversion)
    handleResults(resultsHeat, 'false')

    error_and_exit(0, None)

if __name__ == "__main__":
        main(sys.argv[1:])
