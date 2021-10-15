import json
import zipfile

import pycurl

from sdcBePy.common.logger import debug, log, print_name_and_return_code, print_and_exit
from sdcBePy.common.sdcBeProxy import SdcBeProxy
from sdcBePy.common.errors import ResourceCreationError


def process_and_create_normative_element(normative_element,
                                         scheme=None, be_host=None, be_port=None, header=None, admin_user=None, sdc_be_proxy=None,
                                         model=None, debug=False,
                                         exit_on_success=False):
    if sdc_be_proxy is None:
        sdc_be_proxy = SdcBeProxy(be_host, be_port, header, scheme, admin_user, debug=debug)

    file_dir, url_suffix, element_name, element_from_name, with_metadata = normative_element.get_parameters()
    _create_normative_element(sdc_be_proxy,
                              file_dir,
                              url_suffix,
                              element_name,
                              element_from_name,
                              model,
                              with_metadata,
                              exit_on_success)


def _create_normative_element(sdc_be_proxy, file_dir,
                              url_suffix, element_name, element_form_name, model, with_metadata=False,
                              exit_on_success=False):
    result = _send_request(sdc_be_proxy,
                           file_dir,
                           url_suffix,
                           element_name,
                           element_form_name,
                           model,
                           with_metadata)
    print_and_check_result(result, exit_on_success)


def _send_request(sdc_be_proxy, file_dir, url_suffix, element_name,
                  element_form_name, model,
                  with_metadata=False):
    try:
        log("create normative element ", element_name)

        type_file_name = file_dir + element_name
        multi_part_form_data = _create_multipart_form_data(element_form_name, type_file_name, with_metadata,
                                                           element_name, model)

        debug("http request url =", url_suffix)
        http_res = sdc_be_proxy.post_file(url_suffix, multi_part_form_data)
        if http_res is not None:
            debug("http response =", http_res)

        response = sdc_be_proxy.get_response_from_buffer()
        debug("response buffer", response)
        # c.close()
        return element_name, http_res, response

    except Exception as inst:
        print("ERROR=" + str(inst))
        return element_name, None, None


def _create_multipart_form_data(element_form_name, type_file_name, with_metadata, element_name, model):
    tosca_type_zip_part = _create_zip_file_multi_part(element_form_name, type_file_name, element_name)
    multi_part_form_data = [tosca_type_zip_part]
    if with_metadata:
        metadata_type_part = _create_metadata_multipart(type_file_name)
        multi_part_form_data.append(metadata_type_part)
    if model is not None:
        model_data = ("model", model)
        multi_part_form_data.append(model_data)
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
    json_file = open(type_metadata_json_file, encoding='utf-8')

    debug("before load json")
    json_data = json.load(json_file, strict=False)
    debug(json_data)

    return json.dumps(json_data)


def print_and_check_result(result, exit_on_success):
    if result is not None:
        print_name_and_return_code(result[0], result[1])
        if result[1] is None or result[1] not in [200, 201, 409]:
            raise ResourceCreationError("Failed to create the normatives elements!!", 1)
        else:
            if exit_on_success is True:
                print_and_exit(0, "All normatives elements created successfully!!")
    else:
        raise ResourceCreationError("Results is None!", 1)
