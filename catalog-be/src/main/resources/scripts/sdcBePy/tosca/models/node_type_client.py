#  -
#   ============LICENSE_START=======================================================
#   Copyright (C) 2021 Nordix Foundation.
#   ================================================================================
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
#   SPDX-License-Identifier: Apache-2.0
#   ============LICENSE_END=========================================================
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

from io import BytesIO
from pathlib import Path

import pycurl

from sdcBePy.common import logger


class NodeTypeClient:

    def __init__(self, sdc_be_proxy):
        self.__base_path = Path('/sdc2/rest/v1/catalog/upload')
        self.__import_all_path = self.__base_path / 'resource/import'
        self.__sdc_be_proxy = sdc_be_proxy

    def import_all(self, node_type_yaml_path, node_type_metadata_json_str, is_update=False):
        logger.debug("Starting to import node types '{}'".format(node_type_yaml_path))

        multi_part_form_data = []

        node_type_yaml_param = ('nodeTypesYaml', (pycurl.FORM_FILE, str(node_type_yaml_path)))
        multi_part_form_data.append(node_type_yaml_param)

        node_type_metadata_json_param = ('nodeTypeMetadataJson', (
            pycurl.FORM_CONTENTS, node_type_metadata_json_str,
            pycurl.FORM_CONTENTTYPE, 'application/json'
        ))
        multi_part_form_data.append(node_type_metadata_json_param)

        if is_update is not None:
            create_new_version_param = ('createNewVersion', (
                pycurl.FORM_CONTENTS, str(is_update).lower(),
                pycurl.FORM_CONTENTTYPE, 'text/plain'
            ))
            multi_part_form_data.append(create_new_version_param)

        response_buffer = BytesIO()
        response_code = self.__sdc_be_proxy.post_file(str(self.__import_all_path), multi_part_form_data,
                                                      response_buffer)
        logger.debug("Import all node types response code '{}'".format(response_code))
        if response_code != 201:
            error_msg = "Failed to import node types '{}'".format(node_type_yaml_path)
            logger.log(error_msg, response_buffer.getvalue())
            raise Exception(error_msg)
        logger.log("Failed to import node types '{}'".format(node_type_yaml_path))

