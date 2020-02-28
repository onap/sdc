import json
import os
import zipfile
from StringIO import StringIO

import pycurl

import importCommon
from importCommon import *


################################################################################################################################################
#																																		       #	
################################################################################################################################################


def createZipFromYml(ymlFile, zipFile):
    zip = zipfile.ZipFile(zipFile, 'w', zipfile.ZIP_DEFLATED)

    zip.write(ymlFile, os.path.basename(ymlFile))
    zip.close()


def createUserNormativeType(scheme, be_host, be_port, admin_user, file_dir, element_name):
    try:
        log("in create normative type ", element_name)
        debug("userId", admin_user)
        debug("fileDir", file_dir)

        _buffer = StringIO()
        c = pycurl.Curl()

        url = scheme + '://' + be_host + ':' + be_port + '/sdc2/rest/v1/catalog/upload/multipart'
        c.setopt(c.URL, url)
        c.setopt(c.POST, 1)

        admin_header = 'USER_ID: ' + admin_user
        # c.setopt(pycurl.HTTPHEADER, ['Content-Type: application/json', 'Accept: application/json', adminHeader])
        c.setopt(pycurl.HTTPHEADER, [admin_header])

        ymlFile = file_dir + element_name + "/normative-types-new-" + element_name + ".yml"
        zipFile = file_dir + element_name + "/normative-types-new-" + element_name + ".zip"
        debug(ymlFile)
        debug(zipFile)
        path = zipFile
        debug("path=" + path)
        current_json_file = file_dir + element_name + "/" + element_name + ".json"
        debug(current_json_file)
        json_file = open(current_json_file)

        debug("before load json")
        json_data = json.load(json_file, strict=False)
        debug(json_data)

        json_as_str = json.dumps(json_data)
        debug(path)
        send = [('resourceMetadata', json_as_str), ('resourceZip', (pycurl.FORM_FILE, path))]
        debug(send)
        c.setopt(pycurl.HTTPPOST, send)

        c.setopt(c.WRITEFUNCTION, _buffer.write)
        if scheme == 'https':
            c.setopt(pycurl.SSL_VERIFYPEER, 0)
            c.setopt(pycurl.SSL_VERIFYHOST, 0)

        c.perform()

        # print("Before get response code")
        http_res = c.getinfo(c.RESPONSE_CODE)
        if http_res is not None:
            debug("http response=", http_res)
        # print('Status: ' + str(responseCode))
        debug(_buffer.getvalue())
        c.close()

        return element_name, http_res, _buffer.getvalue()

    except Exception as inst:
        print("ERROR=" + str(inst))
        return element_name, None, None


def usage():
    print sys.argv[0], \
        '[optional -s <scheme> | --scheme=<scheme>, default http] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-u <user userId> | --user=<user userId> ] [-l <directory base location> | --location=<directory base location>] [-e <element name> | --element=<element name>]'
    print "----------------- Example -------------------"
    print "python importNodeType.py -d false -l  /home/vagrant/catalog-be-1604.0.2.15.6-SNAPSHOT/scripts/import/tosca/../../../import/tosca/user-normative-types/ -e root1"


def main(argv):
    print 'Number of arguments:', len(sys.argv), 'arguments.'

    be_host = 'localhost'
    be_port = '8080'
    admin_user = 'jh0003'
    debug_f = None
    location = None
    element = None
    scheme = 'http'

    try:
        opts, args = getopt.getopt(argv, "i:p:u:d:l:e:h:s:",
                                   ["ip=", "port=", "user=", "location=", "element=", "debug=", "scheme="])
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
        elif opt in ("-l", "--location"):
            location = arg
        elif opt in ("-e", "--element"):
            element = arg
        elif opt in ("-s", "--scheme"):
            scheme = arg
        elif opt in ("-d", "--debug"):
            print arg
            debug_f = bool(arg.lower() == "true" or arg.lower() == "yes")

    print 'scheme =', scheme, ', be host =', be_host, ', be port =', be_port, ', user =', admin_user

    if be_host is None:
        usage()
        sys.exit(3)

    if debug_f is not None:
        print 'set debug mode to ' + str(debug_f)
        importCommon.debugFlag = debug_f

    if location is None:
        print 'Missing file location'
        usage()
        sys.exit(3)

    if element is None:
        print 'Missing element name. E.g. root, compute, ...'
        usage()
        sys.exit(3)

    # pathdir = os.path.dirname(os.path.realpath(sys.argv[0]))

    # baseFileLocation = pathdir + "/../../../import/tosca/"
    # fileDir = baseFileLocation + "user-normative-types/"

    # normativeType = "root1"

    result = createUserNormativeType(scheme, be_host, be_port, admin_user, location, element)
    # result = createUserNormativeType(beHost, bePort, adminUser, fileDir, normativeType)
    print "---------------------------------------"
    print "{0:30} | {1:6}".format(result[0], result[1])
    print "---------------------------------------"

    if result[1] is None or result[1] not in [200, 201]:
        print "Failed creating normative type " + element + ". " + str(result[1])
        error_and_exit(1, None)

    error_and_exit(0, None)


if __name__ == "__main__":
    main(sys.argv[1:])
