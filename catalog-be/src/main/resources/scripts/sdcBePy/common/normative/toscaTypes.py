import json
import zipfile

import pycurl

from sdcBePy.common.errors import ResourceCreationError
from sdcBePy.common.logger import print_name_and_return_code, print_and_exit, log, debug
from sdcBePy.common.sdcBeProxy import SdcBeProxy


def process_and_create_normative_types(normative_type,
                                       scheme=None, be_host=None, be_port=None, header=None, admin_user=None,
                                       sdc_be_proxy=None,
                                       update_version=False,
                                       debug=False,
                                       exit_on_success=False):
    if sdc_be_proxy is None:
        sdc_be_proxy = SdcBeProxy(be_host, be_port, header, scheme, admin_user, debug=debug)

    file_dir, normative_type_list = normative_type.get_parameters()

    results = _create_normatives_type(file_dir, sdc_be_proxy, normative_type_list, update_version)
    print_and_check_results(results, update_version, exit_on_success)


def print_and_check_results(results, update_version, exit_on_success=False):
    if results is not None:
        if len(results) == 0:
            return
        print("----------------------------------------")
        for result in results:
            print_name_and_return_code(result[0], result[1], with_line=False)
        print("----------------------------------------")
        check_results_and_exit(results, exit_on_success)
    else:
        raise ResourceCreationError("Results is none -> error occurred!!", 1)


def check_results_and_exit(results, exit_on_success):
    if not _results_ok(results, _get_response_code()):
        raise ResourceCreationError("Failed to create the normatives types !!", 1)
    else:
        if exit_on_success:
            print_and_exit(0, "All normatives types created successfully!!")


def _create_normatives_type(file_dir, sdc_be_proxy, types, update_version):
    results = []
    response_codes = _get_response_code()
    for normative_type in types:
        result = _send_request(sdc_be_proxy, file_dir, normative_type, update_version)
        results.append(result)
        if result[1] is None or result[1] not in response_codes:
            raise ResourceCreationError("Failed creating normative type " + normative_type + ". " + str(result[1]),
                                        1,
                                        normative_type)
    return results


def _send_request(sdc_be_proxy, file_dir, element_name, update_version):
    try:
        log("create normative type ", element_name)
        debug("userId", sdc_be_proxy.con.user_header)
        debug("fileDir", file_dir)

        url = '/sdc2/rest/v1/catalog/upload/multipart'
        if update_version is not None:
            url += '?createNewVersion=' + _boolean_to_string(update_version)

        send = _create_send_body(file_dir, element_name)

        debug(send)
        http_res = sdc_be_proxy.post_file(url, send)
        if http_res is not None:
            debug("http response=", http_res)

        response = sdc_be_proxy.get_response_from_buffer()
        debug(response)
        return element_name, http_res, response

    except Exception as inst:
        print("ERROR=" + str(inst))
        return element_name, None, None


def _create_send_body(file_dir, element_name):
    yml_path = file_dir + element_name + "/" + element_name + ".yml"
    path = file_dir + element_name + "/" + element_name + ".zip"

    zf = zipfile.ZipFile(path, "w")
    zf.write(yml_path, element_name + '.yml')
    zf.close()

    debug(path)
    current_json_file = file_dir + element_name + "/" + element_name + ".json"

    json_file = open(current_json_file)

    debug("before load json")
    json_data = json.load(json_file, strict=False)
    debug(json_data)

    json_as_str = json.dumps(json_data)

    return [('resourceMetadata', json_as_str), ('resourceZip', (pycurl.FORM_FILE, path))]


def _results_ok(results, response_codes):
    for result in results:
        if result[1] not in response_codes:
            return False

    return True


def _get_response_code():
    response_codes = [200, 201, 409]
    return response_codes


def _boolean_to_string(boolean_value):
    return "true" if boolean_value else "false"
