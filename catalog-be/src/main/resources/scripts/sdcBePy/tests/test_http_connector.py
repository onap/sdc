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

import email.parser
import email.policy
import http.server
import json
import os
import socket
import tempfile
import threading
import unittest
from io import BytesIO

import urllib3

from sdcBePy.common.sdcBeProxy import CONNECTION_FAILED, HttpConnector, SdcBeProxy, build_tls_options, file_part


class _Recorder(http.server.BaseHTTPRequestHandler):
    """Answers from `routes` and records every request on the server object."""

    def _handle(self):
        length = int(self.headers.get("Content-Length") or 0)
        self.server.requests.append({
            "method": self.command,
            "path": self.path,
            "headers": self.headers,
            "body": self.rfile.read(length),
        })
        status, body, extra = self.server.routes.get((self.command, self.path), (404, b'{"error": "nope"}', {}))
        self.send_response(status)
        for key, value in extra.items():
            self.send_header(key, value)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    do_GET = do_POST = do_PUT = _handle

    def log_message(self, *args):
        pass


def _multipart_parts(request):
    message = email.parser.BytesParser(policy=email.policy.HTTP).parsebytes(
        b"Content-Type: " + request["headers"]["Content-Type"].encode() + b"\r\n\r\n" + request["body"])
    return {part.get_param("name", header="content-disposition"): part for part in message.iter_parts()}


class HttpConnectorTest(unittest.TestCase):

    def setUp(self):
        self.server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), _Recorder)
        self.server.requests = []
        self.server.routes = {}
        threading.Thread(target=self.server.serve_forever, daemon=True).start()
        self.url = "http://127.0.0.1:%d" % self.server.server_port
        self.connector = HttpConnector(self.url, "jh0003", "dGVzdDp0ZXN0", None, None, None, None)
        self.dir = tempfile.TemporaryDirectory()
        self.zip_path = os.path.join(self.dir.name, "types.zip")
        with open(self.zip_path, "wb") as file:
            file.write(b"PK\x03\x04zip")

    def tearDown(self):
        self.server.shutdown()
        self.server.server_close()
        self.dir.cleanup()

    def route(self, method, path, status, body=b"{}", headers=None):
        self.server.routes[(method, path)] = (status, body, headers or {})

    def test_get_sends_json_headers_and_identity(self):
        self.route("GET", "/sdc2/rest/v1/user/jh0003", 200)
        self.assertEqual(200, self.connector.get("/sdc2/rest/v1/user/jh0003"))
        headers = self.server.requests[0]["headers"]
        self.assertEqual("jh0003", headers["USER_ID"])
        self.assertEqual("Basic dGVzdDp0ZXN0", headers["Authorization"])
        self.assertEqual("application/json", headers["Content-Type"])
        self.assertEqual("application/json; charset=UTF-8", headers["Accept"])

    def test_no_authorization_header_without_credentials(self):
        connector = HttpConnector(self.url, "jh0003", None, None, None, None, None)
        self.route("GET", "/x", 200)
        connector.get("/x")
        self.assertNotIn("Authorization", self.server.requests[0]["headers"])

    def test_get_buffers_body_only_when_asked(self):
        self.route("GET", "/sdc2/rest/v1/screen", 200, b'{"resources": []}')
        self.connector.get("/sdc2/rest/v1/screen")
        self.assertEqual(b"", self.connector.buffer.getvalue())
        self.connector.get("/sdc2/rest/v1/screen", with_buffer=True)
        self.assertEqual(b'{"resources": []}', self.connector.buffer.getvalue())

    def test_post_sends_string_body(self):
        self.route("POST", "/sdc2/rest/v1/catalog/lock", 200)
        self.assertEqual(200, self.connector.post("/sdc2/rest/v1/catalog/lock", "true"))
        request = self.server.requests[0]
        self.assertEqual(b"true", request["body"])
        self.assertEqual("application/json", request["headers"]["Content-Type"])

    def test_post_file_sends_the_parts_pycurl_sent(self):
        self.route("POST", "/sdc2/rest/v1/catalog/upload/multipart", 201, b'{"ok": true}')

        status = self.connector.post_file("/sdc2/rest/v1/catalog/upload/multipart", [
            ("resourceMetadata", '{"name": "root"}'),
            ("resourceZip", file_part(self.zip_path)),
            ("model", (None, '{"name": "m"}', "application/json")),
        ])

        self.assertEqual(201, status)
        request = self.server.requests[0]
        self.assertNotIn("Accept", request["headers"])
        self.assertEqual("jh0003", request["headers"]["USER_ID"])
        parts = _multipart_parts(request)
        self.assertEqual(b"PK\x03\x04zip", parts["resourceZip"].get_payload(decode=True))
        self.assertEqual("types.zip", parts["resourceZip"].get_filename())
        self.assertEqual("application/octet-stream", parts["resourceZip"].get_content_type())
        self.assertNotIn("Content-Type", parts["resourceMetadata"])
        self.assertIsNone(parts["model"].get_filename())
        self.assertEqual("application/json", parts["model"].get_content_type())
        self.assertEqual(b'{"ok": true}', self.connector.buffer.getvalue())

    def test_put_file_uses_put_and_caller_buffer(self):
        self.route("PUT", "/sdc2/rest/v1/catalog/model/imports", 200, b'{"updated": true}')
        buffer = BytesIO()
        self.assertEqual(200, self.connector.put_file("/sdc2/rest/v1/catalog/model/imports",
                                                      [("modelName", "m")], buffer))
        self.assertEqual("PUT", self.server.requests[0]["method"])
        self.assertEqual(b'{"updated": true}', buffer.getvalue())

    def test_http_error_returns_status_and_body(self):
        self.route("POST", "/sdc2/rest/v1/catalog/upload/multipart", 409, b'{"messageId": "SVC4050"}')
        status = self.connector.post_file("/sdc2/rest/v1/catalog/upload/multipart", [("a", "1")])
        self.assertEqual(409, status)
        self.assertEqual(b'{"messageId": "SVC4050"}', self.connector.buffer.getvalue())

    def test_buffer_holds_only_latest_body(self):
        self.route("POST", "/first", 201, b'{"n": 1}')
        self.route("POST", "/second", 201, b'{"n": 2}')
        self.connector.post_file("/first", [("a", "1")])
        self.connector.post_file("/second", [("a", "1")])
        proxy = SdcBeProxy("127.0.0.1", self.server.server_port, None, "http", None, None, None, None,
                           connector=self.connector)
        self.assertEqual({"n": 2}, json.loads(proxy.get_response_from_buffer()))
        self.assertEqual(b"", self.connector.buffer.getvalue())

    def test_redirect_is_not_followed(self):
        self.route("POST", "/old", 302, b"", {"Location": "/new"})
        self.assertEqual(302, self.connector.post("/old", "true"))
        self.assertEqual(["/old"], [request["path"] for request in self.server.requests])

    def test_server_error_is_not_retried(self):
        self.route("GET", "/flaky", 503)
        self.assertEqual(503, self.connector.get("/flaky"))
        self.assertEqual(1, len(self.server.requests))

    def test_connection_refused_returns_111(self):
        with socket.socket() as probe:
            probe.bind(("127.0.0.1", 0))
            port = probe.getsockname()[1]
        dead = HttpConnector("http://127.0.0.1:%d" % port, "jh0003", None, None, None, None, None)
        self.assertEqual(CONNECTION_FAILED, dead.get("/x"))
        self.assertEqual(CONNECTION_FAILED, dead.post("/x", "true"))
        self.assertEqual(CONNECTION_FAILED, dead.post_file("/x", [("a", "1")]))
        with self.assertRaises(urllib3.exceptions.HTTPError):
            dead.put_file("/x", [("a", "1")])


class FilePartTest(unittest.TestCase):

    def test_reads_file_with_basename_and_octet_stream(self):
        with tempfile.TemporaryDirectory() as directory:
            path = os.path.join(directory, "types.yml")
            with open(path, "wb") as file:
                file.write(b"x: 1")
            self.assertEqual(("types.yml", b"x: 1", "application/octet-stream"), file_part(path))

    def test_missing_file_raises(self):
        with self.assertRaises(FileNotFoundError):
            file_part("/nonexistent/types.zip")


class TlsOptionsTest(unittest.TestCase):

    def test_no_verification_without_ca(self):
        self.assertEqual({"cert_reqs": "CERT_NONE"}, build_tls_options(None, None, None, None))

    def test_ca_alone_does_not_switch_verification_on(self):
        self.assertEqual({"cert_reqs": "CERT_NONE"}, build_tls_options(None, None, None, "/ca.pem"))

    def test_client_cert_without_ca_is_sent_unverified(self):
        self.assertEqual({"cert_reqs": "CERT_NONE", "cert_file": "/c.pem", "key_file": "/k.pem", "key_password": "pw"},
                         build_tls_options("/c.pem", "/k.pem", "pw", None))

    def test_client_cert_with_ca_verifies(self):
        self.assertEqual({"cert_reqs": "CERT_REQUIRED", "ca_certs": "/ca.pem", "cert_file": "/c.pem",
                          "key_file": "/k.pem", "key_password": None},
                         build_tls_options("/c.pem", "/k.pem", None, "/ca.pem"))


class NoPycurlLeftTest(unittest.TestCase):

    def test_package_does_not_import_pycurl(self):
        package_root = os.path.join(os.path.dirname(__file__), "..")
        offenders = []
        for directory, _, files in os.walk(package_root):
            for name in files:
                path = os.path.join(directory, name)
                if name.endswith(".py") and os.path.abspath(path) != os.path.abspath(__file__):
                    with open(path, encoding="utf-8") as source:
                        if "pycurl" in source.read():
                            offenders.append(os.path.relpath(path, package_root))
        self.assertEqual([], sorted(offenders))


if __name__ == "__main__":
    unittest.main()
