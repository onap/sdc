import argparse
import json
import re
import requests
from socket import getfqdn
from sys import platform
from time import sleep


PARSER = argparse.ArgumentParser()
PARSER.add_argument("manager_ip", help="The IPv4 Address where one can read the MaveriQConductor.")
PARSER.add_argument("--mockupfile", type=str, help="The path of the json mockupfile to use.")
ARGS = PARSER.parse_args()

URL = "http://{0}:8084/MaveriQConductor/machine/create".format(ARGS.manager_ip)
URL_AVAIL = "http://{0}:8084/MaveriQConductor/isReady".format(ARGS.manager_ip)
HEADERS = {
    'Accept': 'text/plain',
    'Content-type': 'application/json',
    'Connection': 'close'
}

NETWORK_MAP = {
    "$$OAM_NET_IP$$": "oam_private_network_ip",
    "$$BACKEND_NET_IP$$": "backend_interconnect_network_ip",
    "$$PACKET_MIRROR_NET_IP$$": "packet_mirror_network_ip",
    "$$CDR_NET_IP$$": "cdr_network_ip",
    "$$VERTICA_NET_IP$$": "vertica_private_network_ip",
    "$$PACKET_INTERNAL_NET_IP$$": "packet_internal_network_ip",
    "$$OAM_PROTECTED_NET_IP$$": "oam_protected_network_ip"
}


def map_ips_to_networks(p_meta_data):
    network_to_ip = {}
    for network_name in NETWORK_MAP.keys():
        if NETWORK_MAP[network_name] in p_meta_data:
            network_to_ip[network_name] = str(p_meta_data[NETWORK_MAP[network_name]])
    return network_to_ip


def check_availability():
    is_connected = False
    while is_connected is False:
        try:
            if requests.get(URL_AVAIL, headers={'Connection': 'close'}).status_code is 200:
                is_connected = True
            sleep(2)
        except requests.exceptions.ConnectionError:
            sleep(2)


def post_request(p_json_data, p_headers):
    req = requests.post(url=URL, data=p_json_data, headers=p_headers)
    return req.status_code


def multiple_replace(regex_dictionary, text):
    regex = re.compile("(%s)" % "|".join(map(re.escape, regex_dictionary.keys())))
    return regex.sub(
        lambda x: regex_dictionary[x.string[x.start():x.end()]], text
    )


def main():
    # Depending on platform, load the dependencies and meta.js files.
    if ARGS.mockupfile:
        with open(ARGS.mockupfile, 'r') as mockup_file:
            mockup_file_data = mockup_file.read()
        return post_request(mockup_file_data, HEADERS)
    else:
        if platform.startswith('linux'):
            with open(r'/root/dependencies.json', 'r') as json_file:
                json_data = json_file.read()
            with open(r'/meta.js', 'r') as json_file:
                meta_data = json.load(json_file)

        elif platform == 'cygwin' or platform == 'win32':
            with open(r'c:\\dependencies.json', 'r') as json_file:
                json_data = json_file.read()
            with open(r'c:\\meta.js', 'r') as json_file:
                meta_data = json.load(json_file)
        else:
            json_data = {}
            meta_data = {}
            raise Exception('Unsupported platform')
        # Build dictionary mapping each IP to network.
        regex_dict = map_ips_to_networks(meta_data)
        regex_dict['$$HOSTNAME$$'] = getfqdn()

        # Perform any replacement needed.
        json_data = multiple_replace(regex_dict, json_data)
        print json_data
        check_availability()
        return post_request(p_json_data=json_data, p_headers=HEADERS)

print main()
