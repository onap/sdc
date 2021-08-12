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
    NODE_TYPES = 'node-types/'

    def __init__(self, model_imports_path, model_client):
        self.__model_base_path = model_imports_path
        self.__model_init_path = self.__model_base_path / self.INIT_FOLDER_NAME
        self.__model_upgrade_path = self.__model_base_path / self.UPGRADE_FOLDER_NAME
        self.__model_client = model_client

    def create_models(self):
        for model_folder_name in self.__get_model_init_list():
            model_imports_zip_path = self.__zip_model_imports(model_folder_name, self.ACTION_INIT)
            model_payload_dict = self.__read_model_payload(model_folder_name, self.ACTION_INIT)
            self.__model_client.create_model(model_payload_dict, model_imports_zip_path)
            tosca_path = self.__get_model_tosca_path(self.ACTION_INIT, model_folder_name)
            self.__model_client.import_model_elements(model_payload_dict, tosca_path)
            if os.path.isdir(tosca_path + self.NODE_TYPES):
                self.__model_client.import_model_types(model_payload_dict, self.__get_model_normative_type_candidate(tosca_path), False)

    def update_models(self):
        for model_folder_name in self.__get_model_upgrade_list():
            model_imports_zip_path = self.__zip_model_imports(model_folder_name, self.ACTION_UPGRADE)
            model_payload_dict = self.__read_model_payload(model_folder_name, self.ACTION_UPGRADE)
            self.__model_client.update_model_imports(model_payload_dict, model_imports_zip_path)
            tosca_path = self.__get_model_tosca_path(self.ACTION_UPGRADE, model_folder_name)
            if os.path.isdir(tosca_path + self.NODE_TYPES):
                self.__model_client.import_model_types(model_payload_dict, self.__get_model_normative_type_candidate(tosca_path), True)

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

    def __get_model_normative_type_candidate(self, tosca_path):
        path = tosca_path + self.NODE_TYPES
        return [NormativeTypeCandidate(path, self.__read_model_type_json(path))]

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
        path = tosca_path + "types.json"
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

    def __get_model_tosca_path(self, model, action):
        return str(self.__get_base_action_path(action) / model) + "/tosca/"
