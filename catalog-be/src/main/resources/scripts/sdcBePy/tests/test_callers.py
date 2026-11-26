#  ============LICENSE_START=======================================================
#  Copyright (C) 2026 Deutsche Telekom. All rights reserved.
#  ================================================================================
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
#  SPDX-License-Identifier: Apache-2.0
#  ============LICENSE_END========================================================


import http.server
import json
import os
import tempfile
import threading
import unittest

from test_http_connector import _Recorder, _multipart_parts

from sdcBePy.common.normative import toscaElements, toscaTypes
from sdcBePy.common.sdcBeProxy import SdcBeProxy
from sdcBePy.tosca.models.model_client import ModelClient
from sdcBePy.tosca.models.node_type_client import NodeTypeClient


class CallersTest(unittest.TestCase):
    """Runs every call site that builds a multipart form through the real proxy and connector."""

    def setUp(self):
        self.server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), _Recorder)
        self.server.requests = []
        self.server.routes = {}
        threading.Thread(target=self.server.serve_forever, daemon=True).start()
        self.proxy = SdcBeProxy("127.0.0.1", self.server.server_port, "dGVzdDp0ZXN0", "http",
                                None, None, None, None)
        self.dir = tempfile.TemporaryDirectory()
        self.root = self.dir.name + "/"

    def tearDown(self):
        self.server.shutdown()
        self.server.server_close()
        self.dir.cleanup()

    def route(self, method, path, status, body=b"{}"):
        self.server.routes[(method, path)] = (status, body, {})

    def write(self, relative_path, content):
        path = os.path.join(self.root, relative_path)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as file:
            file.write(content)
        return path

    def only_request(self):
        self.assertEqual(1, len(self.server.requests))
        return self.server.requests[0]

    def test_tosca_type_upload(self):
        self.write("root/root.yml", "tosca_definitions_version: tosca_simple_yaml_1_0\n")
        self.write("root/root.json", '{"payloadName": "root.yml"}')
        self.route("POST", "/sdc2/rest/v1/catalog/upload/multipart?createNewVersion=true", 201, b'{"name": "root"}')

        name, status, response = toscaTypes._send_request(self.proxy, self.root, "root", True)

        self.assertEqual(("root", 201, '{"name": "root"}'), (name, status, response))
        parts = _multipart_parts(self.only_request())
        self.assertEqual({"payloadName": "root.yml"}, json.loads(parts["resourceMetadata"].get_content()))
        self.assertEqual("root.zip", parts["resourceZip"].get_filename())
        self.assertEqual("application/octet-stream", parts["resourceZip"].get_content_type())

    def test_tosca_element_upload_with_metadata_and_model(self):
        self.write("dataTypes.yml", "data_types: {}\n")
        self.write("dataTypes.json", '{"payloadName": "dataTypes.yml"}')
        self.route("POST", "/sdc2/rest/v1/catalog/uploadType/datatypes", 201)

        name, status, _ = toscaElements._send_request(self.proxy, self.root, "/sdc2/rest/v1/catalog/uploadType/datatypes",
                                                      "dataTypes", "dataTypesZip", "SOL001", with_metadata=True)

        self.assertEqual(("dataTypes", 201), (name, status))
        parts = _multipart_parts(self.only_request())
        self.assertEqual("dataTypes.zip", parts["dataTypesZip"].get_filename())
        self.assertEqual("application/octet-stream", parts["dataTypesZip"].get_content_type())
        self.assertIn("toscaTypeMetadata", parts)
        self.assertEqual("SOL001", parts["model"].get_content())

    def test_create_model(self):
        zip_path = self.write("model.zip", "zip")
        self.route("POST", "/sdc2/rest/v1/catalog/model", 201)

        ModelClient(self.proxy).create_model({"name": "SOL001"}, zip_path)

        parts = _multipart_parts(self.only_request())
        self.assertEqual("model.zip", parts["modelImportsZip"].get_filename())
        self.assertEqual("application/json", parts["model"].get_content_type())
        self.assertEqual({"name": "SOL001"}, json.loads(parts["model"].get_content()))

    def test_update_model_imports(self):
        zip_path = self.write("model.zip", "zip")
        self.route("PUT", "/sdc2/rest/v1/catalog/model/imports", 204, b"")

        ModelClient(self.proxy).update_model_imports({"name": "SOL001"}, zip_path)

        request = self.only_request()
        self.assertEqual("PUT", request["method"])
        parts = _multipart_parts(request)
        self.assertEqual("SOL001", parts["modelName"].get_content())
        self.assertEqual("model.zip", parts["modelImportsZip"].get_filename())

    def test_node_type_import_all(self):
        yaml_path = self.write("node_types.yml", "node_types: {}\n")
        self.route("POST", "/sdc2/rest/v1/catalog/upload/resource/import", 201)

        NodeTypeClient(self.proxy).import_all(yaml_path, '{"model": "SOL001"}', is_update=False)

        parts = _multipart_parts(self.only_request())
        self.assertEqual("node_types.yml", parts["nodeTypesYaml"].get_filename())
        self.assertEqual("application/octet-stream", parts["nodeTypesYaml"].get_content_type())
        self.assertEqual("application/json", parts["nodeTypeMetadataJson"].get_content_type())
        self.assertEqual("text/plain", parts["createNewVersion"].get_content_type())
        self.assertEqual("false", parts["createNewVersion"].get_content())


if __name__ == "__main__":
    unittest.main()
