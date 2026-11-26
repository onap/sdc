import json
import logging
import os
from io import BytesIO

import urllib3

from sdcBePy.common.helpers import check_arguments_not_none

CONNECTION_FAILED = 111
FILE_CONTENT_TYPE = "application/octet-stream"

def get_url(ip, port, protocol):
    return "%s://%s:%s" % (protocol, ip, port)


class SdcBeProxy:

    CHARTSET = 'UTF-8'

    def __init__(self, be_ip, be_port, header, scheme, tls_cert, tls_key, tls_key_pw, ca_cert, user_id="jh0003",
                 debug=False, connector=None):
        if not check_arguments_not_none(be_ip, be_port, scheme, user_id):
            raise AttributeError("The be_host, be_port, scheme or admin_user are missing")
        url = get_url(be_ip, be_port, scheme)
        self.con = connector if connector \
            else HttpConnector(url, user_id, header, tls_cert, tls_key, tls_key_pw, ca_cert, protocol=scheme, debug=debug)

    def check_backend(self):
        return self.con.get('/sdc2/rest/v1/user/jh0003')

    def check_user(self, user_name):
        return self.con.get("/sdc2/rest/v1/user/" + user_name)

    def create_user(self, first_name, last_name, user_id, email, role):

        return self.con.post('/sdc2/rest/v1/user', json.dumps({
            'firstName': first_name,
            'lastName': last_name,
            'userId': user_id,
            'email': email,
            'role': role
        }))

    def check_consumer(self, consumer_name):
        return self.con.get("/sdc2/rest/v1/consumers/" + consumer_name)

    def create_consumer(self, consumer_name, slat, password):
        return self.con.post("/sdc2/rest/v1/consumers", json.dumps({
            'consumerName': consumer_name,
            'consumerSalt': slat,
            'consumerPassword': password
        }))

    def disable_locking(self, disable):
        return self.con.post("/sdc2/rest/v1/catalog/lock", disable)

    def get_normatives(self):
        return self.con.get("/sdc2/rest/v1/screen", with_buffer=True)

    def get_model_list(self):
        return self.con.get("/sdc2/rest/v1/catalog/model", with_buffer=True)

    def post_file(self, path, multi_part_form_data, buffer=None):
        return self.con.post_file(path, multi_part_form_data, buffer)

    def put_file(self, path, multi_part_form_data, buffer=None):
        return self.con.put_file(path, multi_part_form_data, buffer)

    def get_response_from_buffer(self):
        value = self.con.buffer.getvalue()
        self.con.buffer.seek(0)
        self.con.buffer.truncate()
        return value.decode(self.CHARTSET)


def file_part(path):
    with open(path, "rb") as file:
        return os.path.basename(path), file.read(), FILE_CONTENT_TYPE


def build_tls_options(tls_cert, tls_key, tls_key_pw, ca_cert):
    has_client_cert = tls_cert is not None and tls_key is not None
    options = {"cert_reqs": "CERT_REQUIRED" if has_client_cert and ca_cert is not None else "CERT_NONE"}
    if options["cert_reqs"] == "CERT_REQUIRED":
        options["ca_certs"] = ca_cert
    if has_client_cert:
        options.update(cert_file=tls_cert, key_file=tls_key, key_password=tls_key_pw)
    return options


class HttpConnector:
    JSON_HEADERS = {"Content-Type": "application/json", "Accept": "application/json; charset=UTF-8"}

    def __init__(self, url, user_id_header, header, tls_cert, tls_key, tls_key_pw, ca_cert, buffer=None,
                 protocol="http", debug=False):
        self.url = url
        self.buffer = buffer if buffer is not None else BytesIO()
        self.__identity = {"USER_ID": user_id_header}
        # toscaTypes logs this attribute directly
        self.user_header = "USER_ID: " + user_id_header
        if header is not None:
            self.__identity["Authorization"] = "Basic " + header

        tls_options = build_tls_options(tls_cert, tls_key, tls_key_pw, ca_cert) if protocol == "https" else {}
        if tls_options.get("cert_reqs") == "CERT_NONE":
            urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
        if debug:
            urllib3.add_stderr_logger(logging.DEBUG)
        self.__pool = urllib3.PoolManager(retries=False, **tls_options)

    def get(self, path, buffer=None, with_buffer=False):
        target = (buffer or self.buffer) if with_buffer else None
        try:
            return self.__send("GET", path, target, headers={**self.__identity, **self.JSON_HEADERS})
        except urllib3.exceptions.HTTPError:
            return CONNECTION_FAILED

    def post(self, path, data):
        try:
            return self.__send("POST", path, None, body=data.encode("utf-8"),
                               headers={**self.__identity, **self.JSON_HEADERS})
        except urllib3.exceptions.HTTPError:
            return CONNECTION_FAILED

    def post_file(self, path, post_body, buffer=None):
        try:
            return self.__send("POST", path, buffer or self.buffer, fields=post_body, headers=self.__identity)
        except urllib3.exceptions.HTTPError as ex:
            print(ex)
            return CONNECTION_FAILED

    def put_file(self, path, post_body, response_write_buffer=None):
        return self.__send("PUT", path, response_write_buffer or self.buffer, fields=post_body,
                           headers=self.__identity)

    def __send(self, method, path, buffer, **kwargs):
        response = self.__pool.request(method, self.url + path, redirect=False, **kwargs)
        if buffer is not None:
            buffer.seek(0)
            buffer.truncate()
            buffer.write(response.data)
        return response.status
