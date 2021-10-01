#!/usr/bin/env python3

import os
from pathlib import Path

from sdcBePy.common import logger
from sdcBePy.common.logger import print_and_exit
from sdcBePy.common.normative.main import process_element_list, process_type_list
from sdcBePy.tosca.main import parse_and_create_proxy
from sdcBePy.tosca.models.model_client import ModelClient
from sdcBePy.tosca.models.model_import_manager import ModelImportManager
from sdcBePy.tosca.models.node_type_client import NodeTypeClient
from sdcBePy.tosca.models.normativeElementsList import get_normative_element_candidate_list, \
    get_normative_element_with_metadata_list
from sdcBePy.tosca.models.normativeToUpdateList import TypesToUpdate, get_heat_and_normative_to_update_list, \
    get_onap_sol_to_update_list, get_nfv_to_update_list


def main(sdc_be_proxy):
    update_version = True
    update_onap_version = False 
    update_nfv_version = True

    # use to run script form this dir (not like the command)
    # base_file_location = os.getcwd() + "/../../../../import/tosca/"
    base_file_location = os.getcwd() + "/"
    logger.debug("working directory =" + base_file_location)

    model_import_manager = ModelImportManager(Path(base_file_location) / 'models', ModelClient(sdc_be_proxy),
                                              NodeTypeClient(sdc_be_proxy))
    try:
        model_import_manager.deploy_models()
    except Exception as ex:
        logger.log("An error has occurred while uploading the models: ", str(ex))
        raise ex

    process_element_list(get_normative_element_candidate_list(base_file_location), sdc_be_proxy)

    all_types = get_all_types()

    heat_and_normative_list = get_heat_and_normative_to_update_list(all_types, base_file_location)
    process_type_list(heat_and_normative_list, sdc_be_proxy, update_version)

    onap_sol_list = get_onap_sol_to_update_list(all_types, base_file_location)
    process_type_list(onap_sol_list, sdc_be_proxy, update_onap_version)

    nfv_list = get_nfv_to_update_list(all_types, base_file_location)
    process_type_list(nfv_list, sdc_be_proxy, update_nfv_version)
    
    process_element_list(get_normative_element_with_metadata_list(base_file_location), sdc_be_proxy)

    logger.log("Updating end ->", "All normatives updated successfully!")
    print_and_exit(0, None)


def get_all_types():
    path = os.path.dirname(__file__)
    return TypesToUpdate([path + "/../data/typesToUpgrade.json",
                          path + "/../data/onapTypesToUpgrade.json"])


def run():
    sdc_be_proxy, _ = parse_and_create_proxy()
    main(sdc_be_proxy)


if __name__ == "__main__":
    run()
