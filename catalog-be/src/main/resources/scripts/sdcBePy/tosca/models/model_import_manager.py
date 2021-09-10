# ============LICENSE_START=======================================================
#  Copyright (C) 2021 Nordix Foundation
#  ===============================================================================
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
#  SPDX-License-Identifier: Apache-2.0
#  ============LICENSE_END========================================================

import json
import os
import zipfile
from sdcBePy.tosca.models.normativeTypeCandidate import NormativeTypeCandidate
from pathlib import Path


class ModelImportManager:
    """Manages the model imports directory"""

    INIT_FOLDER_NAME = 'init'
    UPGRADE_FOLDER_NAME = 'upgrade'
    IMPORTS_FOLDER_NAME = 'imports'
    ACTION_UPGRADE = 'upgrade'
    ACTION_INIT = 'init'
    TYPES_FOLDER = 'tosca'
    NODE_FOLDER = 'node-types'

    def __init__(self, model_imports_path, model_client):
        self.__model_base_path = model_imports_path
        self.__model_init_path = self.__model_base_path / self.INIT_FOLDER_NAME
        self.__model_upgrade_path = self.__model_base_path / self.UPGRADE_FOLDER_NAME
        self.__model_client = model_client

    def deploy_models(self):
        existing_models = self.__model_client.get_model_list()
        for model_folder_name in self.__get_model_init_list():
            model_payload_dict = self.__read_model_payload(model_folder_name, self.ACTION_INIT)
            if (not existing_models or not any(m for m in existing_models if model_payload_dict['name'] == m['name'])):
                self.__create_models(model_folder_name, model_payload_dict)

        for model_folder_name in self.__get_model_upgrade_list():
            model_payload_dict = self.__read_model_payload(model_folder_name, self.ACTION_UPGRADE)
            if (existing_models and any(m for m in existing_models if model_payload_dict['name'] == m['name'])):
                self.__update_models(model_folder_name, model_payload_dict)

    def __create_models(self, model_folder_name, model_payload_dict):
        model_imports_zip_path = self.__zip_model_imports(model_folder_name, self.ACTION_INIT)
        self.__model_client.create_model(model_payload_dict, model_imports_zip_path)
        self.__init_model_non_node_types(model_folder_name, model_payload_dict)
        self.__init_model_node_types(model_folder_name, model_payload_dict)
        self.__init_model_non_node_types(model_folder_name, model_payload_dict, True);

    def __update_models(self, model_folder_name, model_payload_dict):
        model_imports_zip_path = self.__zip_model_imports(model_folder_name, self.ACTION_UPGRADE)
        self.__model_client.update_model_imports(model_payload_dict, model_imports_zip_path)
        self.__upgrade_model_non_node_types(model_folder_name, model_payload_dict)
        self.__upgrade_model_node_types(model_folder_name, model_payload_dict)
        self.__upgrade_model_non_node_types(model_folder_name, model_payload_dict, True)

    def __get_model_init_list(self):
        return self.__get_model_list(self.__model_init_path)

    def __get_model_upgrade_list(self):
        return self.__get_model_list(self.__model_upgrade_path)

    @staticmethod
    def __get_model_list(path):
        model_list = []
        for (dirpath, dirnames, filenames) in os.walk(path):
            model_list.extend(dirnames)
            break
        return model_list

    def __get_node_type_list(self, path):
        return [NormativeTypeCandidate(str(os.path.join(path, '')), self.__read_model_type_json(path))]

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

    def __read_model_payload_as_string(self, model, action_type) -> str:
        base_path = self.__get_base_action_path(action_type)
        model_payload_path = base_path / model / "payload.json"
        json_file = open(model_payload_path)
        json_data = json.load(json_file, strict=False)
        return json.dumps(json_data)

    def __read_model_type_json(self, tosca_path):
        path = tosca_path / "types.json"
        if not os.path.isfile(path):
            return []
        json_file = open(path)
        return json.load(json_file)

    def __read_model_payload(self, model, action_type) -> dict:
        base_path = self.__get_base_action_path(action_type)
        model_payload_path = base_path / model / "payload.json"
        json_file = open(model_payload_path)
        return json.load(json_file, strict=False)

    def __get_base_action_path(self, action_type) -> Path:
        return self.__model_init_path if action_type == self.INIT_FOLDER_NAME else self.__model_upgrade_path

    def __get_tosca_path(self, action, model):
        return self.__get_base_action_path(action) / model / self.TYPES_FOLDER

    def __init_model_non_node_types(self, model, model_payload_dict, with_metadata=False):
        path = self.__get_tosca_path(self.ACTION_INIT, model)
        if os.path.isdir(path):
            self.__model_client.import_model_elements(model_payload_dict, str(os.path.join(path, '')) , with_metadata)

    def __upgrade_model_non_node_types(self, model, model_payload_dict, with_metadata=False):
        path = self.__get_tosca_path(self.ACTION_UPGRADE, model)
        if os.path.isdir(path):
            self.__model_client.import_model_elements(model_payload_dict, str(os.path.join(path, '')), with_metadata)

    def __init_model_node_types(self, model, model_payload_dict, upgrade=False):
        path = self.__get_tosca_path(self.ACTION_INIT, model) / self.NODE_FOLDER
        if os.path.isdir(path):
            self.__model_client.import_model_types(model_payload_dict, self.__get_node_type_list(path), upgrade)

    def __upgrade_model_node_types(self, model, model_payload_dict, upgrade=True):
        path = self.__get_tosca_path(self.ACTION_UPGRADE, model) / self.NODE_FOLDER
        if os.path.isdir(path):
            self.__model_client.import_model_types(model_payload_dict, self.__get_node_type_list(path), upgrade)
