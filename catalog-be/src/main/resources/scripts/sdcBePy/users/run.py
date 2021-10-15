#!/usr/bin/env python3

import json
import os
import time
from argparse import ArgumentParser

from sdcBePy import properties
from sdcBePy.common.bColors import BColors
from sdcBePy.common.healthCheck import check_backend
from sdcBePy.common.properties import init_properties
from sdcBePy.common.sdcBeProxy import SdcBeProxy

colors = BColors()


def load_users(conf_path):
    with open(conf_path, 'r', encoding='utf-8') as f:
        return json.load(f)


def be_user_init(be_ip, be_port, header, protocol, conf_path):
    sdc_be_proxy = SdcBeProxy(be_ip, be_port, header, protocol)
    if check_backend(sdc_be_proxy, properties.retry_attempts):
        users = load_users(conf_path)
        for user in users:
            if sdc_be_proxy.check_user(user['userId']) != 200:
                result = sdc_be_proxy.create_user(user['firstName'],
                                                  user['lastName'],
                                                  user['userId'],
                                                  user['email'],
                                                  user['role'])
                if result == 201:
                    print('[INFO]: ' + user['userId'] +
                          ' created, result: [' + str(result) + ']')
                else:
                    print('[ERROR]: ' + colors.FAIL + user['userId'] + colors.END_C +
                          ' error creating , result: [' + str(result) + ']')
            else:
                print('[INFO]: ' + user['userId'] + ' already exists')
    else:
        print('[ERROR]: ' + time.strftime('%Y/%m/%d %H:%M:%S') + colors.FAIL
              + 'Backend is DOWN :-(' + colors.END_C)
        raise Exception("Cannot communicate with the backend!")


def get_args():
    parser = ArgumentParser()

    parser.add_argument('-i', '--ip', required=True)
    parser.add_argument('-p', '--port', required=True)
    parser.add_argument('--header')
    parser.add_argument('--https', action='store_true')
    path = os.path.dirname(__file__)
    parser.add_argument('--conf', default=os.path.join(path, 'data', 'users.json'))

    args = parser.parse_args()

    init_properties(10, 10)
    return [args.ip, args.port, args.header, 'https' if args.https else 'http', args.conf]


def main():
    be_user_init(*get_args())


if __name__ == "__main__":
    main()
