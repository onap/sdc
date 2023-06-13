import json
import os
import zipfile
from sdcBePy.common import logger
from pathlib import Path


class ModelImportManager:
    """Manages the model imports directory"""

    INIT_FOLDER_NAME = 'init'
    IMPORTS_FOLDER_NAME = 'imports'
    ACTION_INIT = 'init'
    TYPES_FOLDER = 'tosca'
    NODE_FOLDER = 'node-types'
    NODE_TYPE_FILE = 'nodeTypes.yaml'

    def __init__(self, model_imports_path, model_client, node_type_client):
        self.__model_base_path = model_imports_path
        self.__model_init_path = self.__model_base_path / self.INIT_FOLDER_NAME
        self.__model_client = model_client
        self.__node_type_client = node_type_client

    def deploy_models(self):
        existing_models = self.__model_client.get_model_list()
        for model_folder_name in self.__get_model_init_list():
            model_payload_dict = self.__read_model_payload(model_folder_name, self.ACTION_INIT)
            if not existing_models or not any(m for m in existing_models if model_payload_dict['name'] == m['name']):
                self.__create_models(model_folder_name, model_payload_dict)

    def __create_models(self, model_folder_name, model_payload_dict):
        logger.log('Creating model {}, based on folder {}'.format(model_payload_dict['name'], model_folder_name))
        model_imports_zip_path = self.__zip_model_imports(model_folder_name, self.ACTION_INIT)
        self.__model_client.create_model(model_payload_dict, model_imports_zip_path)
        self.__init_model_non_node_types(model_folder_name, model_payload_dict)
        self.__init_model_node_types(model_folder_name, model_payload_dict['name'])

    def __get_model_init_list(self):
        return self.__get_model_list(self.__model_init_path)

    @staticmethod
    def __get_model_list(path):
        model_list = []
        for (dirpath, dirnames, filenames) in os.walk(path):
            model_list.extend(dirnames)
            break
        return model_list

    def __zip_model_imports(self, model, action_type) -> Path:
        base_path = self.__get_base_action_path(action_type)
        model_path = base_path / model
        model_imports_path = base_path / model / self.IMPORTS_FOLDER_NAME
        zip_file_path = model_path / "{}.zip".format(model)
        zip_file = zipfile.ZipFile(zip_file_path, 'w', zipfile.ZIP_DEFLATED)
        for root, dirs, files in os.walk(model_imports_path):
            for file in files:
                zip_file.write(os.path.join(root, file), os.path.relpath(os.path.join(root, file), model_imports_path))
        zip_file.close()
        return zip_file_path

    def __read_model_payload(self, model, action_type) -> dict:
        base_path = self.__get_base_action_path(action_type)
        model_payload_path = base_path / model / "payload.json"
        json_file = open(model_payload_path, encoding='utf-8')
        return json.load(json_file, strict=False)

    def __get_base_action_path(self, action_type) -> Path:
        return self.__model_init_path if action_type == self.INIT_FOLDER_NAME else self.__model_upgrade_path

    def __get_tosca_path(self, action, model):
        return self.__get_base_action_path(action) / model / self.TYPES_FOLDER

    def __init_model_non_node_types(self, model, model_payload_dict):
        path = self.__get_tosca_path(self.ACTION_INIT, model)
        if os.path.isdir(path):
            self.__model_client.import_model_elements(model_payload_dict, str(os.path.join(path, '')))

    def __init_model_node_types(self, model_folder_name, model_name):
        self.__import_model_node_types(model_folder_name, model_name, self.ACTION_INIT)

    def __import_model_node_types(self, model_folder_name, model_name, action):
        path = self.__get_tosca_path(action, model_folder_name) / self.NODE_FOLDER
        if not os.path.isdir(path):
            return

        payload_json_path = self.__get_node_types_metadata_file_path(model_name, path)
        payload_json_str = json.dumps(json.load(open(payload_json_path)))

        node_types_yaml_path = self.__get_node_types_yaml_file_path(model_name, path)

        self.__node_type_client.import_all(node_types_yaml_path, payload_json_str)

    def __get_node_types_metadata_file_path(self, model_name, node_types_folder):
        metadata_json_path = node_types_folder / 'metadata.json'
        if not os.path.isfile(metadata_json_path):
            error_msg = "Missing metadata.json file for model '{}'. Expected path '{}'".format(model_name,
                                                                                               metadata_json_path)
            raise Exception(error_msg)
        return metadata_json_path

    def __get_node_types_yaml_file_path(self, model_name, node_types_folder):
        node_types_yaml_path = node_types_folder / self.NODE_TYPE_FILE
        if not os.path.isfile(node_types_yaml_path):
            error_msg = "Missing {} file for model '{}'. Expected path '{}'".format(self.NODE_TYPE_FILE, model_name,
                                                                                    node_types_yaml_path)
            raise Exception(error_msg)
        return node_types_yaml_path
