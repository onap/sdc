import json
from io import BytesIO

import pycurl

from sdcBePy.common.helpers import check_arguments_not_none


def get_url(ip, port, protocol):
    return "%s://%s:%s" % (protocol, ip, port)


class SdcBeProxy:

    def __init__(self, be_ip, be_port, scheme, user_id="jh0003",
                 debug=False, connector=None):
        if not check_arguments_not_none(be_ip, be_port, scheme, user_id):
            raise AttributeError("The be_host, be_port, scheme or admin_user are missing")
        url = get_url(be_ip, be_port, scheme)
        self.con = connector if connector \
            else CurlConnector(url, user_id, scheme=scheme, debug=debug)

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

    def post_file(self, path, multi_part_form_data):
        return self.con.post_file(path, multi_part_form_data)


class CurlConnector:
    CONTENT_TYPE_HEADER = "Content-Type: application/json"
    ACCEPT_HEADER = "Accept: application/json; charset=UTF-8"

    def __init__(self, url, user_id_header, buffer=None, scheme="http", debug=False):
        self.c = pycurl.Curl()
        self.c.setopt(pycurl.HEADER, True)

        self.user_header = "USER_ID: " + user_id_header

        if not debug:
            # disable printing not necessary logs in the terminal
            self.c.setopt(pycurl.WRITEFUNCTION, lambda x: None)
        else:
            self.c.setopt(pycurl.VERBOSE, 1)

        if not buffer:
            self.buffer = BytesIO()

        self.url = url
        self._check_schema(scheme)

    def get(self, path):
        self.c.setopt(pycurl.URL, self.url + path)
        self.c.setopt(pycurl.HTTPHEADER, [self.user_header,
                                          CurlConnector.CONTENT_TYPE_HEADER,
                                          CurlConnector.ACCEPT_HEADER])

        self.c.perform()
        return self.c.getinfo(pycurl.RESPONSE_CODE)

    def post(self, path, data):
        self.c.setopt(pycurl.URL, self.url + path)
        self.c.setopt(pycurl.POST, 1)
        self.c.setopt(pycurl.HTTPHEADER, [self.user_header,
                                          CurlConnector.CONTENT_TYPE_HEADER,
                                          CurlConnector.ACCEPT_HEADER])

        self.c.setopt(pycurl.POSTFIELDS, data)

        self.c.perform()
        self.c.setopt(pycurl.POST, 0)

        return self.c.getinfo(pycurl.RESPONSE_CODE)

    def post_file(self, path, post_body, buffer=None):
        self.c.setopt(pycurl.URL, self.url + path)
        self.c.setopt(pycurl.POST, 1)
        self.c.setopt(pycurl.HTTPHEADER, [self.user_header])

        self.c.setopt(pycurl.HTTPPOST, post_body)

        write = self.buffer.write if not buffer else buffer.write
        self.c.setopt(pycurl.WRITEFUNCTION, write)

        self.c.perform()
        self.c.setopt(pycurl.POST, 0)

        return self.c.getinfo(pycurl.RESPONSE_CODE)

    def _check_schema(self, scheme):
        if scheme == 'https':
            self.c.setopt(pycurl.SSL_VERIFYPEER, 0)
            self.c.setopt(pycurl.SSL_VERIFYHOST, 0)

    def __del__(self):
        self.c.close()
