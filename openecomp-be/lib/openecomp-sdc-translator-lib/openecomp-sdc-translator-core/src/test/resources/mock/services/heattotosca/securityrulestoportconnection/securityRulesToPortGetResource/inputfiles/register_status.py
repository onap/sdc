#!/usr/local/bin/python2.7
"""
This script is a combination of the AddComponentScript and the OnBoardingStatus
scripts. Depending on the arguments given, it will either post an
"addMachineCommand" or an "logOnBoardingInfo" request.
"""
import argparse
import json
import netifaces
import requests
from socket import getfqdn
import sys
from time import time


parser = argparse.ArgumentParser()
parser.add_argument("scribe_ip", type=str,
                    help="The IP where the Scribe can be reached.")
parser.add_argument("--component-type", type=str,
                    help="The component type.", required=False)
parser.add_argument("--component-version", type=str,
                    help="The component version.", required=False)
parser.add_argument("--stage", type=str, required=False)
parser.add_argument("--status", required=False,
                    choices=["WARNING", "INFO", "ERROR", "OK", "FAILURE"])
parser.add_argument("--description", type=str, required=False)
args = parser.parse_args()

add_machine_ip = "http://{0}:8084/MaveriQManager/api/Inventory/addComponent".format(args.scribe_ip)
log_onboarding_info_ip = "http://{0}:8084/MaveriQManager/api/Inventory/logOnBoardingInfo".format(args.scribe_ip)
user = 'omniq'
password = 'radcom'

REGION = ""
TENANT = ""
CLUSTER_NAME = ""
VERSION_NUMBER = ""
PROBE_ID = ""
OAM_DIRECT_IP = ""
MACHINE_TYPE = args.component_type
MACHINE_NAME = getfqdn()
HEALTH_STATUS = {}
ADD_COMPONENT_BODY = {}


def read_metadata():
    """Read the instance metadata"""
    global REGION
    global TENANT
    global CLUSTER_NAME
    global VERSION_NUMBER
    global PROBE_ID
    global OAM_DIRECT_IP
    with open('/meta.js', 'r') as json_file:
        json_data = json.loads(json_file.read())
        TENANT = json_data["tenant"]
        REGION = json_data["region"]
        CLUSTER_NAME = json_data["cluster_name"]
        VERSION_NUMBER = json_data["version_number"]
        OAM_DIRECT_IP = json_data["oam_private_network_ip"]
        if MACHINE_TYPE == 'vProbe':
            PROBE_ID = json_data["probe_id"]


def build_health_json():
    """Builds the actual health status"""
    HEALTH_STATUS["Region"] = REGION
    HEALTH_STATUS["Tenant"] = TENANT
    HEALTH_STATUS["MachineType"] = args.component_type
    HEALTH_STATUS["MachineName"] = getfqdn()
    HEALTH_STATUS["MachineIP"] = OAM_DIRECT_IP
    HEALTH_STATUS["Time"] = long(time())
    HEALTH_STATUS["Description"] = args.description
    HEALTH_STATUS["Status"] = args.status
    HEALTH_STATUS["Stage"] = args.stage
    return HEALTH_STATUS


def build_add_json():
    """Builds the actual health status"""
    ADD_COMPONENT_BODY["region"] = REGION
    ADD_COMPONENT_BODY["tenant"] = TENANT
    ADD_COMPONENT_BODY["componentType"] = args.component_type
    ADD_COMPONENT_BODY["clusterName"] = CLUSTER_NAME
    ADD_COMPONENT_BODY["componentVersionNumber"] = VERSION_NUMBER
    ADD_COMPONENT_BODY["machineName"] = MACHINE_NAME
    ADD_COMPONENT_BODY["machineNetworkInterfaces"] = []
    ADD_COMPONENT_BODY["OAM_IP"] = OAM_DIRECT_IP

    for interface in netifaces.interfaces():
        ADD_COMPONENT_BODY["machineNetworkInterfaces"].append({"name": interface, "value": netifaces.ifaddresses(interface)[2][0]['addr']})

    if PROBE_ID is not "":
        ADD_COMPONENT_BODY["machineID"] = REGION + '_' + TENANT + '_' +\
            CLUSTER_NAME + '_' + MACHINE_NAME + '_' + PROBE_ID
    else:
        ADD_COMPONENT_BODY["machineID"] = REGION + '_' + TENANT + '_' +\
            CLUSTER_NAME + '_' + MACHINE_NAME
    return ADD_COMPONENT_BODY


def send_postage(p_url, p_url_user, p_url_password, p_json_data):
    json_header = {'Content-type': 'application/json'}
    request = requests.post(p_url, json.dumps(p_json_data), json_header, auth=requests.auth.HTTPBasicAuth(p_url_user, p_url_password))
    print request.status_code
    if (request.status_code != 200):
        sys.exit(1)
    return request.status_code


def post_health():
    read_metadata()
    return send_postage(log_onboarding_info_ip, user, password,
                        build_health_json())


def post_add_machine():
    read_metadata()
    return send_postage(add_machine_ip, user, password, build_add_json())

if args.stage is None and args.status is None and args.description is None:
    print "adding machine"
    print post_add_machine()
else:
    print "logging health"
    print post_health()
