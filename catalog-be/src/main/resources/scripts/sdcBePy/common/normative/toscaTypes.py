import json
import zipfile

import pycurl

from sdcBePy.common.logger import print_name_and_return_code, error_and_exit, log, debug
from sdcBePy.common.sdcBeProxy import SdcBeProxy


def process_and_create_normative_types(normative_type,
                                       scheme=None, be_host=None, be_port=None, admin_user=None,
                                       sdc_be_proxy=None,
                                       update_version=False,
                                       debug=False,
                                       exit_on_success=False):
    if sdc_be_proxy is None:
        sdc_be_proxy = SdcBeProxy(be_host, be_port, scheme, admin_user, debug=debug)

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
        check_results_and_exit(results, update_version, exit_on_success)
    else:
        error_and_exit(1, "results is none -> error occurred!!")


def check_results_and_exit(results, update_version, exit_on_success):
    if not _results_ok(results, _get_response_code(update_version)):
        error_and_exit(1, "Failed to create the normatives types !!")
    else:
        if exit_on_success:
            error_and_exit(0, "All normatives types created successfully!!")


def _create_normatives_type(file_dir, sdc_be_proxy, types, update_version):
    results = []
    response_codes = _get_response_code(update_version)
    for normativeType in types:
        result = _send_request(sdc_be_proxy, file_dir, normativeType, update_version)
        results.append(result)
        if result[1] is None or result[1] not in response_codes:
            print("Failed creating normative type " + normativeType + ". " + str(result[1]))
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
        httpRes = sdc_be_proxy.post_file(url, send)
        if httpRes is not None:
            debug("http response=", httpRes)
        debug(sdc_be_proxy.con.buffer.getvalue())

        return element_name, httpRes, sdc_be_proxy.con.buffer.getvalue()

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

    jsonFile = open(current_json_file)

    debug("before load json")
    json_data = json.load(jsonFile, strict=False)
    debug(json_data)

    jsonAsStr = json.dumps(json_data)

    return [('resourceMetadata', jsonAsStr), ('resourceZip', (pycurl.FORM_FILE, path))]


def _results_ok(results, response_codes):
    for result in results:
        if result[1] not in response_codes:
            return False

    return True


def _get_response_code(update_version):
    responseCodes = [200, 201]
    if update_version is False:
        responseCodes.append(409)

    return responseCodes


def _boolean_to_string(boolean_value):
    return "true" if boolean_value else "false"
