import json
from io import BytesIO

import pycurl

from sdcBePy.common.helpers import check_arguments_not_none

def get_url(ip, port, protocol):
    return "%s://%s:%s" % (protocol, ip, port)


class SdcBeProxy:

    BODY_SEPARATOR = "\r\n\r\n"
    CHARTSET = 'UTF-8'

    def __init__(self, be_ip, be_port, header, scheme, user_id="jh0003",
                 debug=False, connector=None):
        if not check_arguments_not_none(be_ip, be_port, scheme, user_id):
            raise AttributeError("The be_host, be_port, scheme or admin_user are missing")
        url = get_url(be_ip, be_port, scheme)
        self.con = connector if connector \
            else CurlConnector(url, user_id, header, protocol=scheme, debug=debug)

    def check_backend(self):
        return self.con.get('/sdc2/rest/v1/user/jh0003')

    def check_user(self, user_name):
          return self.con.get("/sdc2/rest/v1/user" + user_name)

    def create_user(self, first_name, last_name, user_id, email, role):

        return self.con.post('/sdc2/rest/v1/user', json.dumps({
            'firstName': first_name,
            'lastName': last_name,
            'userId': user_id,
            'email': email,
            'role': role
        }))

    def check_consumer(self, consumer_name):
        return self.con.get("/sdc2/rest/v1/consumers" + consumer_name)

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
        self.con.buffer.truncate(0)
        self.con.buffer.seek(0)

        response = value.decode(self.CHARTSET).split(self.BODY_SEPARATOR)
        return response[len(response) - 1] if len(response) > 1 else response[0]

class CurlConnector:
    CONTENT_TYPE_HEADER = "Content-Type: application/json"
    ACCEPT_HEADER = "Accept: application/json; charset=UTF-8"

    def __init__(self, url, user_id_header, header, buffer=None, protocol="http", debug=False):
        self.__debug = debug
        self.__protocol = protocol
        self.c = self.__build_default_curl()

        self.user_header = "USER_ID: " + user_id_header
        self.url = url

        if not buffer:
            self.buffer = BytesIO()

        if header is None:
            self.basicauth_header = ""
        else:
            self.basicauth_header = "Authorization: Basic " + header

    def get(self, path, buffer=None, with_buffer=False):
        try:
            self.c.setopt(pycurl.URL, self.url + path)
            self.c.setopt(pycurl.HTTPHEADER, [self.user_header,
                                              CurlConnector.CONTENT_TYPE_HEADER,
                                              CurlConnector.ACCEPT_HEADER,
                                              self.basicauth_header])

            if with_buffer:
                write = self.buffer.write if not buffer else buffer.write
                self.c.setopt(pycurl.WRITEFUNCTION, write)

            self.c.perform()
            return self.c.getinfo(pycurl.RESPONSE_CODE)
        except pycurl.error:
            return 111

    def post(self, path, data):
        try:
            self.c.setopt(pycurl.URL, self.url + path)
            self.c.setopt(pycurl.POST, 1)

            self.c.setopt(pycurl.HTTPHEADER, [self.user_header,
                                              CurlConnector.CONTENT_TYPE_HEADER,
                                              CurlConnector.ACCEPT_HEADER,
                                              self.basicauth_header])

            self.c.setopt(pycurl.POSTFIELDS, data)

            self.c.perform()
            self.c.setopt(pycurl.POST, 0)

            return self.c.getinfo(pycurl.RESPONSE_CODE)
        except pycurl.error:
            return 111

    def post_file(self, path, post_body, buffer=None):
        try:
            self.c.setopt(pycurl.URL, self.url + path)
            self.c.setopt(pycurl.POST, 1)
            self.c.setopt(pycurl.HTTPHEADER, [self.user_header, self.basicauth_header])

            self.c.setopt(pycurl.HTTPPOST, post_body)

            write = self.buffer.write if not buffer else buffer.write
            self.c.setopt(pycurl.WRITEFUNCTION, write)

            self.c.perform()
            self.c.setopt(pycurl.POST, 0)
            return self.c.getinfo(pycurl.RESPONSE_CODE)
        except pycurl.error as ex:
            print(ex)
            return 111

    def put_file(self, path, post_body, response_write_buffer=None):
        curl = self.__build_default_curl()
        curl.setopt(pycurl.URL, self.url + path)
        curl.setopt(pycurl.HTTPHEADER, [self.user_header, self.basicauth_header])
        curl.setopt(pycurl.CUSTOMREQUEST, "PUT")

        curl.setopt(pycurl.HTTPPOST, post_body)

        write = self.buffer.write if not response_write_buffer else response_write_buffer.write
        curl.setopt(pycurl.WRITEFUNCTION, write)

        curl.perform()
        response_code = curl.getinfo(pycurl.RESPONSE_CODE)
        curl.close()
        return response_code

    def __build_default_curl(self):
        curl = pycurl.Curl()
        if not self.__debug:
            curl.setopt(pycurl.WRITEFUNCTION, lambda x: None)
        else:
            curl.setopt(pycurl.VERBOSE, 1)

        if self.__protocol == 'https':
            curl.setopt(pycurl.SSL_VERIFYPEER, 0)
            curl.setopt(pycurl.SSL_VERIFYHOST, 0)
        curl.setopt(pycurl.HEADER, True)
        return curl

    def __del__(self):
        self.c.close()
