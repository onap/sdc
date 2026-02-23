import time
from datetime import datetime

from sdcBePy.common.bColors import BColors
from sdcBePy.common.logger import print_and_exit
from sdcBePy.common.normative.toscaElements import process_and_create_normative_element
from sdcBePy.common.normative.toscaTypes import process_and_create_normative_types
from sdcBePy.common.errors import ResourceCreationError
from sdcBePy import properties

colors = BColors()


def process_element_list(normative_elements_list, sdc_be_proxy, model=None):
    for normative_element in normative_elements_list:
        if normative_element is None:
            continue
        attempt = 0
        while True:
            attempt += 1
            try:
                process_and_create_normative_element(normative_element,
                                                     sdc_be_proxy=sdc_be_proxy, model=model)
                break
            except ResourceCreationError as e:
                _check_and_retry(attempt, e.error_code, e.message)
            except Exception as e:
                _check_and_retry(attempt, 1, str(e))


def process_type_list(normative_type_list, sdc_be_proxy, update_version):
    for normative_type in normative_type_list:
        attempt = 0
        while True:
            attempt += 1
            try:
                process_and_create_normative_types(normative_type,
                                                   sdc_be_proxy=sdc_be_proxy,
                                                   update_version=update_version)
                break
            except ResourceCreationError as e:
                _check_and_retry(attempt, e.error_code, e.message)
                normative_type.normative_types_list = _reduce(normative_type.normative_types_list, e.resource_name)
            except Exception as e:
                _check_and_retry(attempt, 1, str(e))


def _check_and_retry(attempt, code, message):
    if attempt == properties.retry_attempts + 1:
        print_and_exit(code, message)

    print(colors.FAIL + '[WARRING]: ' + datetime.now().strftime('%Y/%m/%d %H:%M:%S')
          + ' ' + message + ", try again: #" + str(attempt) + colors.END_C)
    time.sleep(properties.retry_time)


def _reduce(_list, element):
    return _list[_list.index(element)::]
