import json
import os
import sys
from argparse import ArgumentParser


def usage():
    print(sys.argv[0],
          '[-s <scheme> | --scheme=<scheme> ] [-i <be host> | --ip=<be host>] [-p <be port> | '
          '--port=<be port> ] [-u <user userId> | --user=<user userId> ] [-d <true|false> | '
          '--debug=<true|false>] [-v <true|false> | --updateVersion=<true|false>]')


def load_be_config(conf_path):
    return json.load(open(conf_path, 'r'))


def parse_param():
    parser = ArgumentParser()

    path = os.path.dirname(__file__)
    parser.add_argument('--conf', default=os.path.join(path, 'data', 'beConfig.json'))

    parser.add_argument('--ip', "-i")
    parser.add_argument('--port', "-p")
    parser.add_argument('--adminUser', "-a")
    parser.add_argument('--https', action='store_true')
    parser.add_argument('--updateVersion', action='store_false')
    parser.add_argument('--debug', action='store_true')

    args, _ = parser.parse_known_args()

    return [args.conf, 'https' if args.https else 'http',
            args.ip, args.port, args.adminUser, args.updateVersion,
            args.debug]


def get_args():
    print('Number of arguments:', len(sys.argv), 'arguments.')

    conf_path, scheme, be_host, be_port, admin_user, update_version, debug = parse_param()
    defaults = load_be_config(conf_path)

    # Use defaults if param not provided by the user
    if be_host is None:
        be_host = defaults["beHost"]
    if be_port is None:
        be_port = defaults["bePort"]
    if admin_user is None:
        admin_user = defaults["adminUser"]

    print('scheme =', scheme, ',be host =', be_host, ', be port =', be_port, ', user =', admin_user,
          ', debug =', debug, ', update_version =', update_version)

    return scheme, be_host, be_port, admin_user, update_version, debug
