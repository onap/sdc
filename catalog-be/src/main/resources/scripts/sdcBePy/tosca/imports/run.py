#!/usr/bin/env python3

import os

import sdcBePy.common.logger as logger
from sdcBePy.common.normative.main import process_element_list, process_type_list
from sdcBePy.tosca.main import parse_and_create_proxy
from sdcBePy.tosca.models.normativeElementsList import get_normative_element_candidate_list, \
    get_normative_element_with_metadata_list
from sdcBePy.tosca.models.normativeTypesList import get_normative_type_candidate_list


def main(sdc_be_proxy, update_version):
    # use to run script form this dir (not like the command)
    # base_file_location = os.getcwd() + "/../../../import/tosca/"
    base_file_location = os.getcwd() + os.path.sep
    logger.debug("working directory =" + base_file_location)

    process_element_list(get_normative_element_candidate_list(base_file_location), sdc_be_proxy)
    process_type_list(get_normative_type_candidate_list(base_file_location), sdc_be_proxy, update_version)
    process_element_list(get_normative_element_with_metadata_list(base_file_location), sdc_be_proxy)

    logger.log("Script end ->", "All normatives imported successfully!")
    logger.print_and_exit(0, None)


def run():
    sdc_be_proxy, update_version = parse_and_create_proxy()
    main(sdc_be_proxy, update_version)


if __name__ == "__main__":
    run()
