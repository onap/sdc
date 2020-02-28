import os

import importCommon
from importCommon import *
from importNormativeTypes import createNormativeType


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
    print sys.argv[0], \
        '[optional -s <scheme> | --scheme=<scheme>, default http] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ] [-d <true|false> | --debug=<true|false>]'


def handleResults(results):
    print_frame_line()
    for result in results:
        print_name_and_return_code(result[0], result[1])
    print_frame_line()

    failed_results = filter(lambda x: x[1] is None or x[1] not in [200, 201, 409], results)
    if len(list(failed_results)) > 0:
        error_and_exit(1, None)


def main(argv):
    print 'Number of arguments:', len(sys.argv), 'arguments.'

    be_host = 'localhost'
    be_port = '8080'
    admin_user = 'jh0003'
    debug_f = None
    update_version = 'true'
    importCommon.debugFlag = False
    scheme = 'http'

    try:
        opts, args = getopt.getopt(argv, "i:p:u:d:h:s:", ["ip=", "port=", "user=", "debug=", "scheme="])
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
            debug_f = bool(arg.lower() == "true" or arg.lower() == "yes")

    print 'scheme =', scheme, ', be host =', be_host, ', be port =', be_port, ', user =', admin_user, ', debug =', debug_f

    if debug_f is not None:
        print 'set debug mode to ' + str(debug_f)
        importCommon.debugFlag = debug_f

    if be_host is None:
        usage()
        sys.exit(3)

    print sys.argv[0]
    path_dir = os.path.dirname(os.path.realpath(sys.argv[0]))
    debug("path dir =" + path_dir)

    base_file_location = path_dir + "/../../../import/tosca/"
    results = []

    ##########################################################################
    # ---------------------------------for release 1702---------------------- #
    ##########################################################################

    file_location = base_file_location + "heat-types/"
    result = createNormativeType(scheme, be_host, be_port, admin_user, file_location, "contrailPort", update_version)
    results.append(result)

    file_location = base_file_location + "heat-types/"
    result = createNormativeType(scheme, be_host, be_port, admin_user, file_location,
                                 "contrailV2VirtualMachineInterface", update_version)
    results.append(result)

    file_location = base_file_location + "heat-types/"
    result = createNormativeType(scheme, be_host, be_port, admin_user, file_location, "neutronPort", update_version)
    results.append(result)

    file_location = base_file_location + "heat-types/"
    result = createNormativeType(scheme, be_host, be_port, admin_user, file_location, "contrailCompute", update_version)
    results.append(result)

    file_location = base_file_location + "heat-types/"
    result = createNormativeType(scheme, be_host, be_port, admin_user, file_location, "novaServer", update_version)
    results.append(result)

    file_location = base_file_location + "heat-types/"
    result = createNormativeType(scheme, be_host, be_port, admin_user, file_location, "contrailV2VirtualNetwork",
                                 update_version)
    results.append(result)

    file_location = base_file_location + "heat-types/"
    result = createNormativeType(scheme, be_host, be_port, admin_user, file_location, "contrailVirtualNetwork",
                                 update_version)
    results.append(result)

    file_location = base_file_location + "heat-types/"
    result = createNormativeType(scheme, be_host, be_port, admin_user, file_location, "neutronNet", update_version)
    results.append(result)

    handleResults(results)

    error_and_exit(0, None)


if __name__ == "__main__":
    main(sys.argv[1:])
