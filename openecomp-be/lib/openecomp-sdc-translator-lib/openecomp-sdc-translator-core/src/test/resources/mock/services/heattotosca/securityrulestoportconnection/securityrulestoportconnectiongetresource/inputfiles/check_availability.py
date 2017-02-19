#!/usr/local/bin/python2.7
import argparse
import requests
import sys
from time import sleep

conductor_url = "http://{0}:8084/MaveriQConductor/isReady"
check_api = "http://{0}:8084/MaveriQManager/api/Inventory/isReady"
check_user = "omniq"
check_password = "radcom"


PARSER = argparse.ArgumentParser()
PARSER.add_argument("ScribeIP", type=str,
                    help="The Stage the application is currently in")
ARGS = PARSER.parse_args()

print "Begining check availability check!"
isConnected = False
while isConnected is False:
    try:
        sys.stdout.write('.')
        sleep(2)
        if requests.get(conductor_url.format(ARGS.ScribeIP),
                        headers={'Connection': 'close'}).status_code is 200 and requests.get(
            check_api.format(ARGS.ScribeIP,
                             headers={'Connection': 'close'}),
            auth=requests.auth.HTTPBasicAuth(check_user,
                                             check_password)).status_code is 200:
            isConnected = True
    except requests.exceptions.ConnectionError as e:
        sleep(2)

print 'Conductor and Scribe are ready!'
