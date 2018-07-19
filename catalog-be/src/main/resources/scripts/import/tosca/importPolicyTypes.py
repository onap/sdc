import getopt
import sys

from importCommon import *
from importNormativeElements import createNormativeElement


#####################################################################################################################################################################################
#																																		       										#
# Import tosca data types																										   													#
# 																																			   										#
# activation :																																   										#
#       python importPolicyTypes.py [-s <scheme> | --scheme=<scheme> ] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]	#
#																																		  	   										#
# shortest activation (be host = localhost, be port = 8080): 																				   										#
#		python importPolicyTypes.py [-f <input file> | --ifile=<input file> ]												 				           								#
#																																		       										#
#####################################################################################################################################################################################

def usage():
    print sys.argv[
        0], '[optional -s <scheme> | --scheme=<scheme>, default http] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ]'


def importPolicyTypes(scheme, be_host, be_port, admin_user, exit_on_success, file_dir):
    result = createNormativeElement(scheme, be_host, be_port, admin_user, file_dir,
                                    "/sdc2/rest/v1/catalog/uploadType/policytypes", "policyTypes", "policyTypesZip",
                                    True)

    print_frame_line()
    print_name_and_return_code(result[0], result[1])
    print_frame_line()

    if result[1] is None or result[1] not in [200, 201, 409]:
        error_and_exit(1, None)
    else:
        if exit_on_success:
            error_and_exit(0, None)


def main(argv):
    print 'Number of arguments:', len(sys.argv), 'arguments.'

    be_host = 'localhost'
    be_port = '8080'
    admin_user = 'jh0003'
    scheme = 'http'

    try:
        opts, args = getopt.getopt(argv, "i:p:u:h:s:", ["ip=", "port=", "user=", "scheme="])
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

    print 'scheme =', scheme, ', be host =', be_host, ', be port =', be_port, ', user =', admin_user

    if be_host is None:
        usage()
        sys.exit(3)

    importPolicyTypes(scheme, be_host, be_port, admin_user, True, "../../../import/tosca/policy-types/")


if __name__ == "__main__":
    main(sys.argv[1:])
