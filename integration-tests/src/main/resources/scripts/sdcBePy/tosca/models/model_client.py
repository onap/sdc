import json
from io import BytesIO
from pathlib import Path

import pycurl

from sdcBePy.common import logger
from sdcBePy.common.normative.main import process_element_list, process_type_list
from sdcBePy.tosca.models.normativeElementsList import get_normative_element_candidate_list

class ModelClient:

    def __init__(self, sdc_be_proxy):
        self.__base_path = Path('/sdc2/rest/v1/catalog/model')
        self.__imports_path = self.__base_path / 'imports'
        self.__sdc_be_proxy = sdc_be_proxy

    def create_model(self, model_payload_dict, model_imports_zip_path):
        model_name = model_payload_dict['name']

        logger.debug("Starting to create model '{}', zip path '{}'".format(model_name, model_imports_zip_path))

        multi_part_form_data = []

        model_zip_param = ('modelImportsZip', (pycurl.FORM_FILE, str(model_imports_zip_path)))
        multi_part_form_data.append(model_zip_param)

        json_payload = self.__parse_to_json_str(model_payload_dict)
        model_param = ('model', (
            pycurl.FORM_CONTENTS, json_payload,
            pycurl.FORM_CONTENTTYPE, 'application/json'
        ))
        multi_part_form_data.append(model_param)

        response_buffer = BytesIO()
        response_code = self.__sdc_be_proxy.post_file(str(self.__base_path), multi_part_form_data, response_buffer)
        logger.debug("Create model response code '{}'".format(response_code))
        if response_code != 201:
            error_msg = "Failed to create model '{}'".format(model_name)
            logger.log(error_msg, response_buffer.getvalue())
            raise Exception(error_msg)
        logger.log("Created model", model_name)

    def import_model_elements(self, model_payload_dict, tosca_elements_import_path):
        model_name = model_payload_dict['name']
        logger.debug("Starting import of normative elements for model '{}'".format(model_name))
        process_element_list(get_normative_element_candidate_list(tosca_elements_import_path), self.__sdc_be_proxy, model=model_name)
        logger.log("Finished importing normative elements for model", model_name)

    def import_model_types(self, model_name, types_list, upgrade):
        logger.debug("Starting import of normative types for model '{}'".format(model_name))
        process_type_list(types_list, self.__sdc_be_proxy, upgrade)
        logger.log("Finished importing normative types for model", model_name)

    def get_model_list(self):
        response = self.__sdc_be_proxy.get_model_list()
        if response == 200:
            models = self.__sdc_be_proxy.get_response_from_buffer()
            return json.loads(models)
        else:
            return []

    @staticmethod
    def __parse_to_json_str(model_payload_dict):
        return json.dumps(model_payload_dict)
