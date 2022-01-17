#!/usr/bin/env python3

import os
from pathlib import Path

import sdcBePy.common.logger as logger
from sdcBePy.common.normative.main import process_element_list, process_type_list
from sdcBePy.tosca.main import parse_and_create_proxy
from sdcBePy.tosca.models import normativeElementsList
from sdcBePy.tosca.models import normativeTypesList
from sdcBePy.tosca.models.model_client import ModelClient
from sdcBePy.tosca.models.model_import_manager import ModelImportManager
from sdcBePy.tosca.models.node_type_client import NodeTypeClient


def main(sdc_be_proxy, update_version):
    # use to run script form this dir (not like the command)
    # base_file_location = os.getcwd() + "/../../../import/tosca/"
    base_file_location = os.getcwd() + os.path.sep
    logger.debug("working directory =" + base_file_location)
    if sdc_be_proxy.disable_locking("true") != 200:
        raise RuntimeError("Failed to disable locking")
    try:
        process_element_list(normativeElementsList.get_normative_element_candidate_list(base_file_location), sdc_be_proxy)
        process_type_list(normativeTypesList.get_normative_type_candidate_list(base_file_location), sdc_be_proxy, update_version)
        process_element_list(normativeElementsList.get_normative_element_with_metadata_list(base_file_location), sdc_be_proxy)
        # Add model based normatives
        model_import_manager = ModelImportManager(Path(base_file_location) / 'models', ModelClient(sdc_be_proxy),
                                                  NodeTypeClient(sdc_be_proxy))
        model_import_manager.deploy_models()
    except Exception as ex:
        logger.log("An error has occurred while uploading elements and types: ", str(ex))
        raise ex
    finally:
        if sdc_be_proxy.disable_locking("false") != 200:
            raise RuntimeError("Failed to enable locking")
    logger.log("Script end ->", "All normatives imported successfully!")
    logger.print_and_exit(0, None)


def run():
    sdc_be_proxy, update_version = parse_and_create_proxy()
    main(sdc_be_proxy, update_version)


if __name__ == "__main__":
    run()
