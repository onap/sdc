import pycurl
import sys, getopt
from StringIO import StringIO
import json
import copy

###############################################################################################################
#
#
###############################################################################################################

debugFlag = True


def join_strings(lst):
    concat = ""
    for string in lst:
        if string is not None:
            if type(string) == int:
                string = str(string)
            concat += (string + " ")
    return concat


def debug(desc, *args):
    if debugFlag:
        print desc, join_strings(args)


def is_debug():
    return debugFlag


def log(desc, arg=None):
    print desc, arg


def error_and_exit(error_code, error_desc):
    if error_code > 0:
        print "status={0}. {1}".format(error_code, '' if error_desc is None else error_desc)
    else:
        print "status={0}".format(error_code)
    sys.exit(error_code)


def print_name_and_return_code(name, code):
    print "{0:30} | {1:6}".format(name, code)


def print_frame_line():
    print "----------------------------------------"


def parse_cmd_line_params(argv):
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
    return scheme, be_host, be_port, admin_user


def usage():
    print sys.argv[
        0], '[optional -s <scheme> | --scheme=<scheme>, default http ] [-i <be host> | --ip=<be host>] [-p <be port> ' \
            '| --port=<be port> ] [-u <user userId> | --user=<user userId> ] '
