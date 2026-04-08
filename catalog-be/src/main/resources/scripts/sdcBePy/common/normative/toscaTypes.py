import json
import zipfile
import os
import pycurl
import time
from sdcBePy.common.errors import ResourceCreationError
from sdcBePy.common.logger import print_name_and_return_code, print_and_exit, log, debug
from sdcBePy.common.sdcBeProxy import SdcBeProxy


def process_and_create_normative_types(normative_type,
                                       scheme=None, be_host=None, be_port=None, header=None,
                                       tls_cert=None, tls_key=None, tls_key_pw=None, ca_cert=None, admin_user=None,
                                       sdc_be_proxy=None,
                                       update_version=False,
                                       debug=False,
                                       exit_on_success=False):
    if sdc_be_proxy is None:
        sdc_be_proxy = SdcBeProxy(be_host, be_port, header, scheme, tls_cert, tls_key, tls_key_pw, ca_cert, admin_user, debug=debug)

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
    print("=== Starting Normative Types Creation ===")
    print("File Dir:", file_dir)
    print("Types Count:", len(types))
    print("Update Version Flag:", update_version)
    print("----------------------------------------")

    results = []
    response_codes = _get_response_code()

    for normative_type in types:
        print("\n----------------------------------------")
        print(f"Processing normative type: {normative_type}")

        if not os.path.exists(file_dir):
            print(f"❌ ERROR: Directory does not exist -> {file_dir}")
            break

        print(f"➡ Sending request for normative type: {normative_type}")
        result = _send_request(sdc_be_proxy, file_dir, normative_type, update_version)

        print(f"✔ Raw Response Received: {result}")

        results.append(result)

        status_code = result[1]
        print(f"➡ HTTP Status Code for {normative_type}: {status_code}")

        if status_code is None:
            print(f"❌ ERROR: No response code returned for {normative_type}")

        if status_code not in response_codes:
            # If parent not found, retry a few times with exponential backoff
            response_body = result[2]
            parent_not_found = False
            try:
                if response_body is not None:
                    if isinstance(response_body, str):
                        parent_not_found = 'PARENT_RESOURCE_NOT_FOUND' in response_body
                    elif isinstance(response_body, dict):
                        parent_not_found = response_body.get('actionStatus') == 'PARENT_RESOURCE_NOT_FOUND' or \
                            'PARENT_RESOURCE_NOT_FOUND' in str(response_body)
            except Exception:
                parent_not_found = False

            if parent_not_found:
                # Instead of re-sending the full multipart repeatedly, poll the backend
                # for the parent resource visibility. This avoids duplicate uploads and
                # is more robust to JanusGraph eventual-consistency/indexing delays.
                print(f"⚠️ Detected PARENT_RESOURCE_NOT_FOUND for {normative_type}. Polling backend for parent visibility...")
                parent_name, parent_version = _extract_parent_info(file_dir, normative_type)
                # default timeout 60s, exponential backoff starting at 2s
                found = _poll_parent_visibility(sdc_be_proxy, parent_name, parent_version, timeout=60)
                if not found:
                    print(f"❌ ERROR: Parent {parent_name} not found after polling timeout")
                    print(f"❌ Failing Normative Type: {normative_type}")
                    raise ResourceCreationError(
                        "Failed creating normative type " + normative_type + ". parent not visible: " + str(parent_name),
                        1,
                        normative_type
                    )
                # Parent is now visible; retry upload once
                print(f"✔ Parent {parent_name} visible. Retrying upload for {normative_type}...")
                retry_result = _send_request(sdc_be_proxy, file_dir, normative_type, update_version)
                print(f"✔ Raw Retry Response Received: {retry_result}")
                retry_status = retry_result[1]
                if retry_status in response_codes:
                    print(f"✔ Retry succeeded for {normative_type} with status {retry_status}")
                    results[-1] = retry_result
                else:
                    print(f"❌ ERROR: Status {retry_status} NOT in expected {response_codes} after retry")
                    print(f"❌ Failing Normative Type: {normative_type}")
                    raise ResourceCreationError(
                        "Failed creating normative type " + normative_type + ". " + str(retry_status),
                        1,
                        normative_type
                    )
            else:
                print(f"❌ ERROR: Status {status_code} NOT in expected {response_codes}")
                print(f"❌ Failing Normative Type: {normative_type}")
                raise ResourceCreationError(
                    "Failed creating normative type " + normative_type + ". " + str(status_code),
                    1,
                    normative_type
                )

        print(f"✔ SUCCESS: Normative type '{normative_type}' created successfully.")

        # Workaround for potential backend visibility race:
        # if we just created the Root normative, wait briefly so subsequent
        # uploads that depend on it can find the parent in the backend.
        try:
            if status_code in response_codes and str(normative_type).lower() == 'root':
                print("Waiting 2s after Root import to allow backend visibility...")
                time.sleep(2)
        except Exception:
            pass

    print("\n=== Completed Normative Types Creation ===")
    print("Total Processed:", len(results))
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

    json_file = open(current_json_file, encoding='utf-8')

    debug("before load json")
    json_data = json.load(json_file, strict=False)
    debug(json_data)

    json_as_str = json.dumps(json_data)

    return [('resourceMetadata', json_as_str), ('resourceZip', (pycurl.FORM_FILE, path))]


def _extract_parent_info(file_dir, element_name):
    """Try to extract parent tosca name and version from the element's JSON metadata.
    Returns (parent_name, parent_version) where values can be None if not found.
    """
    json_path = file_dir + element_name + "/" + element_name + ".json"
    try:
        with open(json_path, encoding='utf-8') as jf:
            data = json.load(jf)
    except Exception:
        return (None, None)

    # Common keys we've seen in metadata
    # derivedFromGenericType / derivedFromGenericVersion
    parent_name = None
    parent_version = None
    if isinstance(data, dict):
        parent_name = data.get('derivedFrom') or data.get('derivedFromGenericType') or data.get('derivedFromGeneric')
        parent_version = data.get('derivedFromGenericVersion') or data.get('derivedFromVersion')
        # some artifacts may include parentUniqueId but that is less useful for polling by name
        if not parent_name:
            # try nested topologyTemplate or other keys
            parent_name = data.get('parentType')

    return (parent_name, parent_version)


def _poll_parent_visibility(sdc_be_proxy, parent_name, parent_version=None, timeout=60):
    """Poll the Catalog BE for the given parent_name (and optional version) until visible or timeout.
    Returns True if found, False if timed out or parent_name is None.
    """
    if not parent_name:
        return False

    elapsed = 0
    delay = 2
    path = '/sdc2/rest/v1/catalog/resources?name=' + parent_name
    if parent_version:
        path += '&version=' + str(parent_version)

    while elapsed < timeout:
        try:
            # use with_buffer so we can inspect response body
            status = sdc_be_proxy.con.get(path, with_buffer=True)
            body = sdc_be_proxy.get_response_from_buffer()
            # consider status 200 and body containing the parent name as success
            if status == 200 and body and parent_name in str(body):
                return True
            # some APIs might return 204/404 when not found -> keep polling
        except Exception:
            pass

        time.sleep(delay)
        elapsed += delay
        delay = min(delay * 2, 10)

    return False


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
