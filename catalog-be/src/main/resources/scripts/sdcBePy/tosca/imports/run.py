#!/usr/bin/env python3

import os
import sys

import sdcBePy.common.logger as logger
from sdcBePy.common.normative.main import process_element_list, process_type_list
from sdcBePy.common.sdcBeProxy import SdcBeProxy
from sdcBePy.tosca.main import get_args, usage
from sdcBePy.tosca.models.normativeElementsList import get_normative_element_candidate_list, \
    get_normative_element_with_metadata_list
from sdcBePy.tosca.models.normativeTypesList import get_normative_type_candidate_list


def main():
    scheme, be_host, be_port, admin_user, update_version, debug = get_args()

    if debug is False:
        print('Disabling debug mode')
        logger.debugFlag = debug

    try:
        sdc_be_proxy = SdcBeProxy(be_host, be_port, scheme, admin_user, debug)
    except AttributeError:
        usage()
        sys.exit(3)

    # use to run script form this dir (not like the command)
    # base_file_location = os.getcwd() + "/../../../../import/tosca/"
    base_file_location = os.getcwd() + os.path.sep
    logger.debug("working directory =" + base_file_location)

    process_element_list(get_normative_element_candidate_list(base_file_location), sdc_be_proxy)
    process_type_list(get_normative_type_candidate_list(base_file_location), sdc_be_proxy, update_version)
    process_element_list(get_normative_element_with_metadata_list(base_file_location), sdc_be_proxy)

    logger.log("Script end ->", "All normatives imported successfully!")
    logger.error_and_exit(0, None)


if __name__ == "__main__":
    main()
