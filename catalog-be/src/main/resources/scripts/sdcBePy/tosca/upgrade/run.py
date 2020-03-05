#!/usr/bin/env python3

import os
import sys

from sdcBePy.common import logger
from sdcBePy.common.logger import error_and_exit
from sdcBePy.common.normative.main import process_element_list, process_type_list
from sdcBePy.common.sdcBeProxy import SdcBeProxy
from sdcBePy.tosca.main import get_args, usage
from sdcBePy.tosca.models.normativeElementsList import get_normative_element_candidate_list, \
    get_normative_element_with_metadata_list
from sdcBePy.tosca.models.normativeToUpdateList import TypesToUpdate, get_heat_and_normative_to_update_list, \
    get_nfv_onap_sol_to_update_list


def main():
    scheme, be_host, be_port, admin_user, _, debug = get_args()

    update_version = True
    update_onap_version = False

    if debug is False:
        print('Disabling debug mode')
        logger.debugFlag = debug

    try:
        sdc_be_proxy = SdcBeProxy(be_host, be_port, scheme, admin_user, debug=debug)
    except AttributeError:
        usage()
        sys.exit(3)

    # use to run script form this dir (not like the command)
    # base_file_location = os.getcwd() + "/../../../../import/tosca/"
    base_file_location = os.getcwd() + "/"
    logger.debug("working directory =" + base_file_location)
    process_element_list(get_normative_element_candidate_list(base_file_location), sdc_be_proxy)
    process_element_list(get_normative_element_with_metadata_list(base_file_location), sdc_be_proxy)

    all_types = get_all_types()

    heat_and_normative_list = get_heat_and_normative_to_update_list(all_types, base_file_location)
    process_type_list(heat_and_normative_list, sdc_be_proxy, update_version)

    nfv_onap_sol_list = get_nfv_onap_sol_to_update_list(all_types, base_file_location)
    process_type_list(nfv_onap_sol_list, sdc_be_proxy, update_onap_version)

    logger.log("Updating end ->", "All normatives updated successfully!")
    error_and_exit(0, None)


def get_all_types():
    path = os.path.dirname(__file__)
    return TypesToUpdate([path + "/../data/typesToUpgrade.json",
                          path + "/../data/onapTypesToUpgrade.json"])


if __name__ == "__main__":
    main()
