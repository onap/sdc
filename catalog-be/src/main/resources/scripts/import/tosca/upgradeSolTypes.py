import pycurl
import sys, getopt
from StringIO import StringIO
import json
import copy
from importCommon import *
from importNormativeTypes import *
import importCommon
import json


################################################################################################################################################################################################
#																																		                                                       #
# Upgrades all Heat and Normative types confiugred in "typesToUpgrade.json" file																										       #
# 																																			                                                   #
# activation                                                                                                                                                                                   #
#       python upgradeHeatAndNormativeTypes.py [-s <scheme> | --scheme=<scheme> ] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ] #
#																																		  	                                                   #
# shortest activation (be host = localhost, be port = 8080): 																				                                                   #
#		python upgradeHeatAndNormativeTypes.py [-f <input file> | --ifile=<input file> ]												 				                                       #
#																																		                                                       #
################################################################################################################################################################################################
def upgradeSolTypesPerConfigFile(scheme, beHost, bePort, adminUser, baseDir, updateversion):
    responseCodes = [200, 201]
    if (updateversion == 'false'):
        responseCodes = [200, 201, 409]
    with open("onapTypesToUpgrade.json", 'r') as stream:
        try:
            types = json.load(stream)
            solTypes = types["sol"]
            debug(solTypes)
            solFileDir = baseDir + "sol-types/"
            debug(solFileDir)
            results = []
            for solType in solTypes:
                result = createNormativeType(scheme, beHost, bePort, adminUser, solFileDir, solType.encode('ascii', 'ignore'), updateversion)
                results.append(result)
                if (result[1] == None or result[1] not in responseCodes):
                    print "Failed creating sol type " + solType + ". " + str(result[1])
        except yaml.YAMLError as exc:
            print(exc)



def main(argv):
    print 'Number of arguments:', len(sys.argv), 'arguments.'

    beHost = 'localhost'
    bePort = '8080'
    adminUser = 'jh0003'
    updateversion = 'true'
    scheme = 'http'

    try:
        opts, args = getopt.getopt(argv, "i:p:u:v:h:s:", ["ip=", "port=", "user=", "updateversion=","scheme="])
    except getopt.GetoptError:
        usage()
        error_and_exit(2, 'Invalid input')

    for opt, arg in opts:
        # print opt, arg
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
        elif opt in ("-v", "--updateversion"):
            if (arg.lower() == "false" or arg.lower() == "no"):
                updateversion = 'false'

    print 'scheme =',scheme,', be host =',beHost, ', be port =', bePort, ', user =', adminUser

    if (beHost == None):
        usage()
        sys.exit(3)

    results = upgradeSolTypesPerConfigFile(scheme, beHost, bePort, adminUser, "../../../import/tosca/", updateversion)

    print "-----------------------------"
    for result in results:
        print "{0:20} | {1:6}".format(result[0], result[1])
    print "-----------------------------"

    responseCodes = [200, 201]

    if (updateversion == 'false'):
        responseCodes = [200, 201, 409]

    failedNormatives = filter(lambda x: x[1] == None or x[1] not in responseCodes, results)
    if (len(failedNormatives) > 0):
        error_and_exit(1, None)
    else:
        error_and_exit(0, None)


if __name__ == "__main__":
    main(sys.argv[1:])

