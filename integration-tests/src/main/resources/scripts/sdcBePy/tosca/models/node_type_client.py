from io import BytesIO
from pathlib import Path

import pycurl

from sdcBePy.common import logger


class NodeTypeClient:

    def __init__(self, sdc_be_proxy):
        self.__base_path = Path('/sdc2/rest/v1/catalog/upload')
        self.__import_all_path = self.__base_path / 'resource/import'
        self.__sdc_be_proxy = sdc_be_proxy

    def import_all(self, node_type_yaml_path, node_type_metadata_json_str):
        logger.debug("Starting to import node types '{}'".format(node_type_yaml_path))

        multi_part_form_data = []

        node_type_yaml_param = ('nodeTypesYaml', (pycurl.FORM_FILE, str(node_type_yaml_path)))
        multi_part_form_data.append(node_type_yaml_param)

        node_type_metadata_json_param = ('nodeTypeMetadataJson', (
            pycurl.FORM_CONTENTS, node_type_metadata_json_str,
            pycurl.FORM_CONTENTTYPE, 'application/json'
        ))
        multi_part_form_data.append(node_type_metadata_json_param)

        response_buffer = BytesIO()
        response_code = self.__sdc_be_proxy.post_file(str(self.__import_all_path), multi_part_form_data,
                                                      response_buffer)
        logger.log("Import all node types response code '{}'".format(response_code))
        if response_code != 201:
            error_msg = "Failed to import node types '{}'".format(node_type_yaml_path)
            logger.log(error_msg, response_buffer.getvalue())
            raise Exception(error_msg)
