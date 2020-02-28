import getopt
import json
import sys

import pycurl
import yaml


#########################################################################################################################################################################################
#																																				       									#
# Import all users from a given YAML file																											   									#
# 																																					   									#
# activation :																																		   									#
#       python importUsersFromYaml.py [-s <scheme> | --scheme=<scheme> ] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]	#
#																																				  	   									#
# shortest activation (be host = localhost, be port = 8080): 																						   									#
#		python importUsersFromYaml.py [-f <input file> | --ifile=<input file> ]												 				           									#
#									    																											   									#
#   PyYAML module shall be added to python.																											   									#
#   pip install PyYAML>=3.1.0 --proxy=http://one.proxy.att.com:8080														                               									#
#########################################################################################################################################################################################


def importUsers(scheme, be_host, be_port, users, admin_user):
    result = []

    for user in users:
        # print("Going to add user " + user['userId'])
        get_res = getUser(scheme, be_host, be_port, user)
        user_id = get_res[0]
        error = get_res[1]
        # print error
        if error is not None and error == 404:
            res = createUser(scheme, be_host, be_port, user, admin_user)
            result.append(res)
        else:
            if error == 200:
                cur_result = user_id, 409
                result.append(cur_result)
            else:
                result.append(get_res)

    return result


def getUser(scheme, be_host, be_port, user):
    if user.get('userId') is None:
        print "Ignoring record", user
        return 'NotExist', 200
    user_id = user['userId']
    try:
        c = pycurl.Curl()

        # print type(userId)
        url = scheme + '://' + be_host + ':' + be_port + '/sdc2/rest/v1/user/' + str(user_id)
        c.setopt(c.URL, url)

        if scheme == 'https':
            c.setopt(pycurl.SSL_VERIFYPEER, 0)
            c.setopt(pycurl.SSL_VERIFYHOST, 0)

        # adminHeader = 'USER_ID: ' + adminUser
        c.setopt(pycurl.HTTPHEADER, ['Content-Type: application/json', 'Accept: application/json'])
        c.setopt(c.WRITEFUNCTION, lambda x: None)
        res = c.perform()

        # print("Before get response code")
        http_res = c.getinfo(c.RESPONSE_CODE)
        # print("After get response code")
        # response_code = c.getinfo(c.RESPONSE_CODE)
        # print('Status: ' + str(response_code))

        c.close()

        return user_id, http_res

    except Exception as inst:
        print(inst)
        return user_id, None


def createUser(scheme, be_host, be_port, user, admin_user):
    if user.get('userId') is None:
        print "Ignoring record", user
        return 'NotExist', 200

    user_id = user['userId']
    try:
        c = pycurl.Curl()

        url = scheme + '://' + be_host + ':' + be_port + '/sdc2/rest/v1/user'
        c.setopt(c.URL, url)
        c.setopt(c.POST, 1)

        admin_header = 'USER_ID: ' + admin_user
        c.setopt(pycurl.HTTPHEADER, ['Content-Type: application/json', 'Accept: application/json', admin_header])

        data = json.dumps(user)
        c.setopt(c.POSTFIELDS, data)

        if scheme == 'https':
            c.setopt(pycurl.SSL_VERIFYPEER, 0)
            c.setopt(pycurl.SSL_VERIFYHOST, 0)

        c.setopt(c.WRITEFUNCTION, lambda x: None)
        # print("before perform")
        c.perform()

        # print("Before get response code")
        http_res = c.getinfo(c.RESPONSE_CODE)
        # print("After get response code")
        # responseCode = c.getinfo(c.RESPONSE_CODE)
        # print('Status: ' + str(responseCode))

        c.close()

        return user_id, http_res

    except Exception as inst:
        print(inst)
        return user_id, None


def error_and_exit(error_code, error_desc):
    if error_code > 0:
        print("status=" + str(error_code) + ". " + error_desc)
    else:
        print("status=" + str(error_code))
    sys.exit(error_code)


def usage():
    print sys.argv[0], \
        '[optional -s <scheme> | --scheme=<scheme>, default http] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]'


def main(argv):
    print 'Number of arguments:', len(sys.argv), 'arguments.'

    be_host = 'localhost'
    be_port = '8080'
    input_file = None

    admin_user = 'jh0003'
    scheme = 'http'

    try:
        opts, args = getopt.getopt(argv, "i:p:f:h:s:", ["ip=", "port=", "ifile=", "scheme="])
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
        elif opt in ("-f", "--ifile"):
            input_file = arg
        elif opt in ("-s", "--scheme"):
            scheme = arg

    print 'scheme =', scheme, ', be host =', be_host, ', be port =', be_port, ', users file =', input_file

    if input_file is None:
        usage()
        sys.exit(3)

    print 'Input file is ', input_file

    users_as_yaml_file = open(input_file, 'r')
    users_doc = yaml.load(users_as_yaml_file)
    print users_doc

    clone_users = []
    for users in users_doc.values():
        for x, y in users.items():
            copied_user = y
            copied_user['userId'] = x
            # print copiedUser
            clone_users.append(copied_user)

    print clone_users

    users_as_yaml_file.close()

    # activeUsers = filter(lambda x: x.get('status') == None or x['status'] == 'ACTIVE', cloneUsers)

    result_table = importUsers(scheme, be_host, be_port, clone_users, admin_user)

    g = lambda x: x[1] != 201 and x[1] != 409

    result = filter(g, result_table)

    if len(result) > 0:
        # print("ERROR: Failed to load the users " + ', '.join(map(lambda x: x[0],result)))
        error_and_exit(3, "Failed to load the users " + ', '.join(map(lambda x: x[0], result)))

    g = lambda x: x[1] == 409
    result = filter(g, result_table)

    print("-------------------------------------------")
    print("Existing users: " + ', '.join(map(lambda x: x[0], result)))

    result = filter(lambda x: x[1] == 201, result_table)
    if len(list(result)) == 0:
        print("-------------------------------------------")
        print("No NEW user was loaded. All users are already exist")
        print("-------------------------------------------")
    else:
        print("-------------------------------------------")
        print("Loaded users: " + ', '.join(map(lambda x: x[0], result)))
        print("-------------------------------------------")

    error_and_exit(0, None)


if __name__ == "__main__":
    main(sys.argv[1:])
