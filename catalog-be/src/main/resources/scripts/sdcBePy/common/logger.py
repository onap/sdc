import sys

debugFlag = True


def join_strings(lst):
    return ''.join([str(string) for string in lst])


def debug(desc, *args):
    if debugFlag:
        print(desc, join_strings(args))


def log(desc, arg=None):
    if arg:
        print(desc, arg)
    else:
        print(desc)


def print_and_exit(error_code, error_desc):
    if error_code > 0:
        print("status={0}. {1}".format(error_code, '' if not error_desc else error_desc))
    else:
        print("status={0}".format(error_code))
    sys.exit(error_code)


def print_name_and_return_code(name, code, with_line=True):
    if _strings_correct(name, code):
        if with_line:
            print("----------------------------------------")
        print("{0:30} | {1:6}".format(name, code))
        if with_line:
            print("----------------------------------------")
    else:
        print("name of the item or return code from request is none -> error occurred!!")


def _strings_correct(*strings):
    results = [(string is not None and string != "") for string in strings]
    return all(results) is True

# def parse_cmd_line_params(argv):
#     print('Number of arguments:', len(sys.argv), 'arguments.')
#
#     opts = []
#
#     be_host = 'localhost'
#     be_port = '8080'
#     admin_user = 'jh0003'
#     scheme = 'http'
#
#     try:
#         opts, args = getopt.getopt(argv, "i:p:u:h:s:", ["ip=", "port=", "user=", "scheme="])
#     except getopt.GetoptError:
#         usage()
#         error_and_exit(2, 'Invalid input')
#
#     for opt, arg in opts:
#         # print opt, arg
#         if opt == '-h':
#             usage()
#             sys.exit(3)
#         elif opt in ("-i", "--ip"):
#             be_host = arg
#         elif opt in ("-p", "--port"):
#             be_port = arg
#         elif opt in ("-u", "--user"):
#             admin_user = arg
#         elif opt in ("-s", "--scheme"):
#             scheme = arg
#
#     print('scheme =', scheme, ', be host =', be_host, ', be port =', be_port, ', user =', admin_user)
#
#     if be_host is None:
#         usage()
#         sys.exit(3)
#     return scheme, be_host, be_port, admin_user
#
#
# def usage():
#     print(sys.argv[0], '[optional -s <scheme> | --scheme=<scheme>, default http ] '
#                        '[-i <be host> | --ip=<be host>] [-p <be port> | '
#                        '--port=<be port> ] [-u <user userId> | --user=<user userId> ] ')
