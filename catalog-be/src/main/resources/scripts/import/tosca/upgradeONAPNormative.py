import os
import time
from importCategoryTypes import importCategories
from upgradeHeatAndNormativeTypes import upgradeTypesPerConfigFile
from importDataTypes import importDataTypes
from importPolicyTypes import importPolicyTypes
from importGroupTypes import importGroupTypes
from importNormativeCapabilities import importNormativeCapabilities
from importNormativeRelationships import importNormativeRelationships
from importNormativeInterfaceLifecycleTypes import importNormativeInterfaceLifecycleType
from upgradeNfvTypes import upgradeNfvTypesPerConfigFile
from upgradeONAPTypes import upgradeOnapTypesPerConfigFile
from upgradeSolTypes import upgradeSolTypesPerConfigFile

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
# shortest activation (be host = localhost, be port = 8080, user = jh0003): 																				   									#
#		python upgradeNormative.py												 				           																						#
#																																		       													#
#################################################################################################################################################################################################

def usage():
    print sys.argv[
        0], '[-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user ' \
            'userId> ] [-d <true|false> | --debug=<true|false>] '


def handle_results(results):
    if results is not None:
        print_frame_line()
        for result in results:
            print_name_and_return_code(result[0], result[1])

        print_frame_line()

        failed_results = filter(lambda x: x[1] is None or x[1] not in [200, 201, 409], results)
        if len(failed_results) > 0:
            error_and_exit(1, None)


def main(argv):
    print 'Number of arguments:', len(sys.argv), 'arguments.'

    be_host = 'localhost'
    be_port = '8080'
    admin_user = 'jh0003'
    is_debug = None
    update_version = 'true'
    update_onap_version = 'false'
    importCommon.debugFlag = False
    scheme = 'http'

    try:
        opts, args = getopt.getopt(argv, "i:p:u:d:v:h:s",
                                   ["scheme=", "ip=", "port=", "user=", "debug=", "updateversion="])
    except getopt.GetoptError:
        usage()
        error_and_exit(2, 'Invalid input')

    for opt, arg in opts:
        # print opt, arg
        if opt == '-h':
            usage()
            sys.exit(3)
        elif opt in ("-i", "--ip"):
            be_host = arg
        elif opt in ("-p", "--port"):
            be_port = arg
        elif opt in ("-u", "--user"):
            admin_user = arg
        elif opt in ("-s", "--scheme"):
            scheme = arg
        elif opt in ("-d", "--debug"):
            print arg
            is_debug = bool(arg.lower() == "true" or arg.lower() == "yes")

    print 'scheme =', scheme, ',be host =', be_host, ', be port =', be_port, ', user =', admin_user, ', debug =', is_debug

    if is_debug is not None:
        print 'set debug mode to ' + str(is_debug)
        importCommon.debugFlag = is_debug

    if be_host is None:
        usage()
        sys.exit(3)

    print sys.argv[0]
    pathdir = os.path.dirname(os.path.realpath(sys.argv[0]))
    debug("path dir =" + pathdir)

    base_file_location = pathdir + "/../../../import/tosca/"

    file_location = base_file_location + "categories/"
    importCategories(scheme, be_host, be_port, admin_user, False, file_location)

    file_location = base_file_location + "relationship-types/"
    importNormativeRelationships(scheme, be_host, be_port, admin_user, False, file_location)

    file_location = base_file_location + "data-types/"
    importDataTypes(scheme, be_host, be_port, admin_user, False, file_location)

    file_location = base_file_location + "policy-types/"
    importPolicyTypes(scheme, be_host, be_port, admin_user, False, file_location)

    file_location = base_file_location + "group-types/"
    importGroupTypes(scheme, be_host, be_port, admin_user, False, file_location)

    file_location = base_file_location + "capability-types/"
    importNormativeCapabilities(scheme, be_host, be_port, admin_user, False, file_location)

    file_location = base_file_location + "interface-lifecycle-types/"
    importNormativeInterfaceLifecycleType(scheme, be_host, be_port, admin_user, False, file_location)

    print 'sleep until data type cache is updated'
    time.sleep(70)

    results_heat = upgradeTypesPerConfigFile(scheme, be_host, be_port, admin_user, base_file_location, update_version)
    handle_results(results_heat)

    results_heat = upgradeNfvTypesPerConfigFile(scheme, be_host, be_port, admin_user, base_file_location,
                                                update_onap_version)
    handle_results(results_heat)

    results_heat = upgradeOnapTypesPerConfigFile(scheme, be_host, be_port, admin_user, base_file_location,
                                                 update_onap_version)
    handle_results(results_heat)

    results_heat = upgradeSolTypesPerConfigFile(scheme, be_host, be_port, admin_user, base_file_location,
                                                update_onap_version)
    handle_results(results_heat)

    error_and_exit(0, None)


if __name__ == "__main__":
    main(sys.argv[1:])
