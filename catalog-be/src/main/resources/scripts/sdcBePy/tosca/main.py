import json
import os
import sys
from argparse import ArgumentParser

from sdcBePy.common import logger
from sdcBePy.common.properties import init_properties
from sdcBePy.common.sdcBeProxy import SdcBeProxy


def usage():
    print(sys.argv[0],
          '[-s <scheme> | --scheme=<scheme> ] [-i <be host> | --ip=<be host>] [-p <be port> | '
          '--port=<be port> ] --header=<header> ] [-u <user userId> | --user=<user userId> ] [-d <true|false> | '
          '--debug=<true|false>] [-v <true|false> | --updateVersion=<true|false>]')


def load_be_config(conf_path):
    with open(conf_path, 'r', encoding='utf-8') as f:
        return json.load(f)


def parse_param():
    parser = ArgumentParser()

    path = os.path.dirname(__file__)
    parser.add_argument('--conf', default=os.path.join(path, 'data', 'beConfig.json'))

    parser.add_argument('--ip', "-i")
    parser.add_argument('--port', "-p")
    parser.add_argument('--header')
    parser.add_argument('--adminUser', "-a")
    parser.add_argument('--https', action='store_true')
    parser.add_argument('--updateVersion', action='store_false')
    parser.add_argument('--debug', action='store_true')

    args, _ = parser.parse_known_args()

    return [args.conf, 'https' if args.https else 'http',
            args.ip, args.port, args.header, args.adminUser, args.updateVersion,
            args.debug]


def get_args():
    print('Number of arguments:', len(sys.argv), 'arguments.')

    conf_path, scheme, be_host, be_port, header, admin_user, update_version, debug = parse_param()
    defaults = load_be_config(conf_path)

    # Use defaults if param not provided by the user
    if be_host is None:
        be_host = defaults["beHost"]
    if be_port is None:
        be_port = defaults["bePort"]
    if admin_user is None:
        admin_user = defaults["adminUser"]

    if header is None:
        print('scheme =', scheme, ',be host =', be_host, ', be port =', be_port, ', user =', admin_user,
          ', debug =', debug, ', update_version =', update_version)
    else:
        print('scheme =', scheme, ',be host =', be_host, ', be port =', be_port, ', header =', header, ', user =', admin_user,
              ', debug =', debug, ', update_version =', update_version)

    init_properties(defaults["retryTime"], defaults["retryAttempt"], defaults["resourceLen"])
    return scheme, be_host, be_port, header, admin_user, update_version, debug


def parse_and_create_proxy():
    scheme, be_host, be_port, header, admin_user, update_version, debug = get_args()

    if debug is False:
        print('Disabling debug mode')
        logger.debugFlag = debug

    try:
        sdc_be_proxy = SdcBeProxy(be_host, be_port, header, scheme, admin_user, debug=debug)
    except AttributeError:
        usage()
        sys.exit(3)

    return sdc_be_proxy, update_version

