import pycurl
import zipfile
from StringIO import StringIO
import json
import copy
from importCommon import *


#################################################################################################################################################################################
#																																		       									#
# Import all users from a given file																										   									#
# 																																			   									#
# activation :																																   									#
#       python importUsers.py [-s <scheme> | --scheme=<scheme> ] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]   #
#																																		  	   									#
# shortest activation (be host = localhost, be port = 8080): 																				   									#
#		python importUsers.py [-f <input file> | --ifile=<input file> ]												 				           									#
#																																		       									#
#################################################################################################################################################################################
def import_element(scheme, be_host, be_port, admin_user, exit_on_success, file_dir, url_suffix, element_name, element_form_name,
                   with_metadata=False):
    result = createNormativeElement(scheme, be_host, be_port, admin_user, file_dir, url_suffix, element_name, element_form_name, with_metadata)
    print_frame_line()
    print_name_and_return_code(result[0], result[1])
    print_frame_line()

    if result[1] is None or result[1] not in [200, 201, 409]:
        error_and_exit(1, None)
    else:
        if exit_on_success:
            error_and_exit(0, None)



def createNormativeElement(scheme, be_host, be_port, admin_user, file_dir, url_suffix, element_name, element_form_name,
                           with_metadata=False):
    try:
        log("in create normative element ", element_name)
        buffer = StringIO()
        c = pycurl.Curl()

        url = scheme + '://' + be_host + ':' + be_port + url_suffix
        c.setopt(c.URL, url)
        c.setopt(c.POST, 1)

        admin_header = 'USER_ID: ' + admin_user
        c.setopt(pycurl.HTTPHEADER, [admin_header])

        type_file_name = file_dir + "/" + element_name

        multi_part_form_data = create_multipart_form_data(element_form_name, type_file_name, with_metadata, element_name)

        c.setopt(pycurl.HTTPPOST, multi_part_form_data)
        c.setopt(c.WRITEFUNCTION, buffer.write)

        if scheme == 'https':
            c.setopt(pycurl.SSL_VERIFYPEER, 0)
            c.setopt(pycurl.SSL_VERIFYHOST, 0)

        c.perform()

        http_res = c.getinfo(c.RESPONSE_CODE)
        if http_res is not None:
            debug("http response=", http_res)
        debug("response buffer", buffer.getvalue())
        c.close()
        return (element_name, http_res, buffer.getvalue())

    except Exception as inst:
        print("ERROR=" + str(inst))
        return (element_name, None, None)


def create_multipart_form_data(element_form_name, type_file_name, with_metadata, element_name):
    tosca_type_zip_part = create_zip_file_multi_part(element_form_name, type_file_name, element_name)
    multi_part_form_data = [tosca_type_zip_part]
    if with_metadata:
        metadata_type_part = create_metadata_multipart(type_file_name)
        multi_part_form_data.append(metadata_type_part)
    debug(multi_part_form_data)
    return multi_part_form_data


def create_metadata_multipart(type_file_name):
    metadata = create_json_metadata_str(type_file_name)
    return ("toscaTypeMetadata", metadata)


def create_zip_file_multi_part(element_form_name, type_file_name, element_name):
    zf = zipfile.ZipFile(type_file_name + ".zip", "w")
    zf.write(type_file_name + '.yml', element_name + '.yml')
    zf.close()

    tosca_type_zip_path = type_file_name + ".zip"
    tosca_type_zip_part = (element_form_name, (pycurl.FORM_FILE, tosca_type_zip_path))
    return tosca_type_zip_part


def create_json_metadata_str(file_name):
    type_metadata_json_file = file_name + ".json"
    debug(type_metadata_json_file)
    json_file = open(type_metadata_json_file)

    debug("before load json")
    json_data = json.load(json_file, strict=False)
    debug(json_data)

    return json.dumps(json_data)
