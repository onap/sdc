import json
import zipfile
from StringIO import StringIO

import pycurl

from importCommon import *


#########################################################################################################################################################################################
#																																		       											#
# Import all users from a given file																										   											#
# 																																			   											#
# activation :																																   											#
#       python importNormativeTypes.py [-s <scheme> | --scheme=<scheme> ] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]	#
#							  [-v <true|false> | --updateversion=<true|false>]																											#
# shortest activation (be host = localhost, be port = 8080): 																				   											#
#		python importNormativeTypes.py [-f <input file> | --ifile=<input file> ]												 				           								#
#																																		       											#	
#########################################################################################################################################################################################

def createNormativeType(scheme, be_host, be_port, admin_user, file_dir, element_name, update_version):
    try:
        log("in create normative type ", element_name)
        debug("userId", admin_user)
        debug("fileDir", file_dir)

        buffer = StringIO()
        c = pycurl.Curl()
        if is_debug():
            c.setopt(pycurl.VERBOSE, 1)

        url = scheme + '://' + be_host + ':' + be_port + '/sdc2/rest/v1/catalog/upload/multipart'
        if update_version is not None:
            url += '?createNewVersion=' + update_version
        c.setopt(c.URL, url)
        c.setopt(c.POST, 1)

        admin_header = 'USER_ID: ' + admin_user
        # c.setopt(pycurl.HTTPHEADER, ['Content-Type: application/json', 'Accept: application/json', adminHeader])
        c.setopt(pycurl.HTTPHEADER, [admin_header])

        yml_path = file_dir + element_name + "/" + element_name + ".yml"
        path = file_dir + element_name + "/" + element_name + ".zip"

        zf = zipfile.ZipFile(path, "w")
        zf.write(yml_path, element_name + '.yml')
        zf.close()

        debug(path)
        current_json_file = file_dir + element_name + "/" + element_name + ".json"
        # sed -i 's/"userId": ".*",/"userId": "'${USER_ID}'",/' ${current_json_file}

        jsonFile = open(current_json_file)

        debug("before load json")
        json_data = json.load(jsonFile, strict=False)
        debug(json_data)

        json_as_str = json.dumps(json_data)

        send = [('resourceMetadata', json_as_str), ('resourceZip', (pycurl.FORM_FILE, path))]
        debug(send)
        c.setopt(pycurl.HTTPPOST, send)

        # data = json.dumps(user)
        # c.setopt(c.POSTFIELDS, data)

        if scheme == 'https':
            c.setopt(pycurl.SSL_VERIFYPEER, 0)
            c.setopt(pycurl.SSL_VERIFYHOST, 0)

        # c.setopt(c.WRITEFUNCTION, lambda x: None)
        c.setopt(c.WRITEFUNCTION, buffer.write)

        # print("before perform")
        c.perform()

        # print("Before get response code")
        http_res = c.getinfo(c.RESPONSE_CODE)
        if http_res is not None:
            debug("http response=", http_res)
        # print('Status: ' + str(responseCode))
        debug(buffer.getvalue())
        c.close()

        return (element_name, http_res, buffer.getvalue())

    except Exception as inst:
        print("ERROR=" + str(inst))
        return (element_name, None, None)


def usage():
    print sys.argv[0], \
        '[optional -s <scheme> | --scheme=<scheme>, default http] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ] [-v <true|false> | --updateversion=<true|false>]'


def importNormativeTypes(scheme, be_host, be_port, admin_user, file_dir, update_version):
    normative_types = ["root", "compute", "softwareComponent", "webServer", "webApplication", "DBMS", "database",
                       "objectStorage", "blockStorage", "containerRuntime", "containerApplication", "loadBalancer",
                       "port", "network"]
    # normative_types = [ "root" ]
    response_codes = [200, 201]

    if update_version == 'false':
        response_codes = [200, 201, 409]

    results = []
    for normative_type in normative_types:
        result = createNormativeType(scheme, be_host, be_port, admin_user, file_dir, normative_type, update_version)
        results.append(result)
        if result[1] is None or result[1] not in response_codes:
            print "Failed creating normative type " + normative_type + ". " + str(result[1])
    return results


def main(argv):
    print 'Number of arguments:', len(sys.argv), 'arguments.'

    be_host = 'localhost'
    be_port = '8080'
    admin_user = 'jh0003'
    update_version = 'true'
    scheme = 'http'

    try:
        opts, args = getopt.getopt(argv, "i:p:u:v:h:s:", ["ip=", "port=", "user=", "updateversion=", "scheme="])
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
        elif opt in ("-v", "--updateversion"):
            if arg.lower() == "false" or arg.lower() == "no":
                update_version = 'false'

    print 'scheme =', scheme, ', be host =', be_host, ', be port =', be_port, ', user =', admin_user, ', updateversion =', update_version

    if be_host is None:
        usage()
        sys.exit(3)

    results = importNormativeTypes(scheme, be_host, be_port, admin_user, "../../../import/tosca/normative-types/",
                                   update_version)

    print "-----------------------------"
    for result in results:
        print "{0:20} | {1:6}".format(result[0], result[1])
    print "-----------------------------"

    response_codes = [200, 201]

    if update_version == 'false':
        response_codes = [200, 201, 409]

    failed_normatives = filter(lambda x: x[1] is None or x[1] not in response_codes, results)
    if len(list(failed_normatives)) > 0:
        error_and_exit(1, None)
    else:
        error_and_exit(0, None)


if __name__ == "__main__":
    main(sys.argv[1:])
