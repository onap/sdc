#!/usr/bin/env python3
import sys
import time
from argparse import ArgumentParser
from datetime import datetime

from sdcBePy import properties
from sdcBePy.common.bColors import BColors
from sdcBePy.common.properties import init_properties
from sdcBePy.common.sdcBeProxy import SdcBeProxy

colors = BColors()


def check_backend(sdc_be_proxy=None, reply_append_count=1, be_host=None, be_port=None, header=None, scheme=None, debug=False, ca_cert=None, tls_cert=None, tls_key=None, tls_key_pw=None):
    if sdc_be_proxy is None:
        sdc_be_proxy = SdcBeProxy(be_host, be_port, header, scheme, tls_cert, tls_key, tls_key_pw, ca_cert, debug=debug)

    for i in range(1, reply_append_count + 1):
        if sdc_be_proxy.check_backend() == 200:
            print('[INFO]: Backend is up and running')
            return True
        else:
            print('[WARRING]: ' + datetime.now().strftime('%Y/%m/%d %H:%M:%S') + colors.FAIL
                  + ' Backend not responding, try #' + str(i) + colors.END_C)
            time.sleep(properties.retry_time)

    return False


def run(be_host, be_port, header, protocol, tls_key, tls_cert, tls_key_pw, ca_cert):
    if not check_backend(reply_append_count=properties.retry_attempts, be_host=be_host,
                         be_port=be_port, header=header, scheme=protocol, ca_cert=ca_cert, tls_cert=tls_cert, tls_key=tls_key, tls_key_pw=tls_key_pw):
        print('[ERROR]: ' + time.strftime('%Y/%m/%d %H:%M:%S') + colors.FAIL + ' Backend is DOWN :-(' + colors.END_C)
        sys.exit()


def get_args():
    parser = ArgumentParser()

    parser.add_argument('-i', '--ip', required=True)
    parser.add_argument('-p', '--port', required=True)
    parser.add_argument('--header')
    parser.add_argument('--https', action='store_true')
    parser.add_argument('--tls_key')
    parser.add_argument('--tls_cert')
    parser.add_argument('--tls_key_pw')
    parser.add_argument('--ca_cert')

    args = parser.parse_args()

    init_properties(10, 10)
    return [args.ip, args.port, args.header, 'https' if args.https else 'http', args.tls_key, args.tls_cert, args.tls_key_pw, args.ca_cert]


def main():
    run(*get_args())


if __name__ == '__main__':
    main()
