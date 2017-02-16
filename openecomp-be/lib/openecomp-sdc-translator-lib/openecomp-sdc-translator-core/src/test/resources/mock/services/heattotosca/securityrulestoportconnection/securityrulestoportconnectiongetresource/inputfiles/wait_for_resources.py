import argparse
import json
import netifaces
import os
import sys
import time

TIME_INTERVAL = 10


def parse_json_file(json_path):
    with open(json_path, 'r') as json_file:
        data = json.load(json_file)
    return data


def check_network_interfaces():
    for interface in netifaces.interfaces():
        if(sys.platform != 'win32' or netifaces.ifaddresses(interface)[-1000][0]['addr'] != '00:00:00:00:00:00:00:e0'):
            while 2 not in netifaces.ifaddresses(interface).keys() and 23 not in netifaces.ifaddresses(interface).keys():
                print "Still waiting for interface:", interface
                time.sleep(TIME_INTERVAL)


def check_connectivity():
    if sys.platform.startswith('linux'):
        ping_str = "ping -c 1 "
    elif sys.platform == 'cygwin' or sys.platform == 'win32':
        ping_str = "ping -n 1 "

    while os.system(ping_str + component_ip) != 0:
        print "No connectivity to", component_ip, "waiting", TIME_INTERVAL, "seconds"
        time.sleep(TIME_INTERVAL)


def check_cinder_mounts():
    if sys.platform.startswith('linux'):
        meta_data = parse_json_file('/meta.js')
    elif sys.platform == 'cygwin' or sys.platform == 'win32':
        meta_data = parse_json_file('c:\\meta.js')

    cinder_count = 0

    for info in meta_data:
        if info.startswith('mount'):
            cinder_count += 1

    if sys.platform.startswith('linux'):
        cinder_attached = os.popen('ls /dev/disk/by-id/virtio* | wc -l').read()
    elif sys.platform == 'cygwin' or sys.platform == 'win32':
        cinder_attached = os.popen("wmic diskdrive get DeviceID | find /i \"PHYSICALDRIVE\" | find /V \"0\" /C").read()

    while (int(cinder_attached) < cinder_count) and (cinder_count != 0):
        print "Missing a cinder mount, waiting", TIME_INTERVAL, "seconds"
        time.sleep(TIME_INTERVAL)

        if sys.platform.startswith('linux'):
            cinder_attached = os.popen('ls /dev/disk/by-id/virtio* | wc -l').read()

        elif sys.platform == 'cygwin' or sys.platform == 'win32':
            cinder_attached = os.popen(
                "wmic diskdrive get DeviceID | find /i \"PHYSICALDRIVE\" | find /V \"0\" /C").read()

        if int(cinder_attached) == cinder_count:
            print "All cinder are attached and ready to be formatted and mounted"


def main():
    check_network_interfaces()
    check_cinder_mounts()

    if component_ip is not None:
        check_connectivity()

    print "All resources are ready"


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='This script is waiting for network and volume resources to come up')
    parser.add_argument('-m', '--component_ip', metavar='component_ip', type=str, help='The component ip', required=False)
    args = parser.parse_args()
    component_ip = args.component_ip
    globals().update(args.__dict__)
    main()
