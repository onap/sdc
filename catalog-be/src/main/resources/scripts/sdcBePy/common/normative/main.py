from sdcBePy.common.normative.toscaElements import process_and_create_normative_element
from sdcBePy.common.normative.toscaTypes import process_and_create_normative_types


def process_element_list(normative_elements_list, sdc_be_proxy):
    for normative_element in normative_elements_list:
        process_and_create_normative_element(normative_element,
                                             sdc_be_proxy=sdc_be_proxy)


def process_type_list(normative_type_list, sdc_be_proxy, update_version):
    for normative_type in normative_type_list:
        process_and_create_normative_types(normative_type,
                                           sdc_be_proxy=sdc_be_proxy,
                                           update_version=update_version)

