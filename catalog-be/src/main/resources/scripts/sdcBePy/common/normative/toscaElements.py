import json
import zipfile

import pycurl

from sdcBePy.common.logger import debug, log, print_name_and_return_code, error_and_exit
from sdcBePy.common.sdcBeProxy import SdcBeProxy


def process_and_create_normative_element(normative_element,
                                         scheme=None, be_host=None, be_port=None, admin_user=None, sdc_be_proxy=None,
                                         debug=False,
                                         exit_on_success=False):
    if sdc_be_proxy is None:
        sdc_be_proxy = SdcBeProxy(be_host, be_port, scheme, admin_user, debug=debug)

    file_dir, url_suffix, element_name, element_from_name, with_metadata = normative_element.get_parameters()
    _create_normative_element(sdc_be_proxy,
                              file_dir,
                              url_suffix,
                              element_name,
                              element_from_name,
                              with_metadata,
                              exit_on_success)


def _create_normative_element(sdc_be_proxy, file_dir,
                              url_suffix, element_name, element_form_name, with_metadata=False,
                              exit_on_success=False):
    result = _send_request(sdc_be_proxy,
                           file_dir,
                           url_suffix,
                           element_name,
                           element_form_name,
                           with_metadata)
    print_and_check_result(result, exit_on_success)


def _send_request(sdc_be_proxy, file_dir, url_suffix, element_name,
                  element_form_name,
                  with_metadata=False):
    try:
        log("create normative element ", element_name)

        type_file_name = file_dir + element_name
        multi_part_form_data = _create_multipart_form_data(element_form_name, type_file_name, with_metadata,
                                                           element_name)

        http_res = sdc_be_proxy.post_file(url_suffix, multi_part_form_data)
        if http_res is not None:
            debug("http response =", http_res)
        debug("response buffer", str(sdc_be_proxy.con.buffer.getvalue(), "UTF-8"))
        # c.close()
        return element_name, http_res, sdc_be_proxy.con.buffer.getvalue()

    except Exception as inst:
        print("ERROR=" + str(inst))
        return element_name, None, None


def _create_multipart_form_data(element_form_name, type_file_name, with_metadata, element_name):
    tosca_type_zip_part = _create_zip_file_multi_part(element_form_name, type_file_name, element_name)
    multi_part_form_data = [tosca_type_zip_part]
    if with_metadata:
        metadata_type_part = _create_metadata_multipart(type_file_name)
        multi_part_form_data.append(metadata_type_part)
    debug(multi_part_form_data)
    return multi_part_form_data


def _create_metadata_multipart(type_file_name):
    metadata = _create_json_metadata_str(type_file_name)
    return "toscaTypeMetadata", metadata


def _create_zip_file_multi_part(element_form_name, type_file_name, element_name):
    zf = zipfile.ZipFile(type_file_name + ".zip", "w")
    zf.write(type_file_name + '.yml', element_name + '.yml')
    zf.close()

    tosca_type_zip_path = type_file_name + ".zip"
    tosca_type_zip_part = (element_form_name, (pycurl.FORM_FILE, tosca_type_zip_path))
    return tosca_type_zip_part


def _create_json_metadata_str(file_name):
    type_metadata_json_file = file_name + ".json"
    debug(type_metadata_json_file)
    json_file = open(type_metadata_json_file)

    debug("before load json")
    json_data = json.load(json_file, strict=False)
    debug(json_data)

    return json.dumps(json_data)


def print_and_check_result(result, exit_on_success):
    if result is not None:
        print_name_and_return_code(result[0], result[1])
        if result[1] is None or result[1] not in [200, 201, 409]:
            error_and_exit(1, "Failed to create the normatives elements!!")
        else:
            if exit_on_success is True:
                error_and_exit(0, "All normatives elements created successfully!!")
    else:
        error_and_exit(1, "Results is None!!")
