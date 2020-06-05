import json

from sdcBePy import properties
from sdcBePy.common.logger import print_and_exit

from sdcBePy.tosca.imports.run import main as import_main
from sdcBePy.tosca.main import parse_and_create_proxy
from sdcBePy.tosca.upgrade.run import main as upgrade_main


def run():
    sdc_be_proxy, update_version = parse_and_create_proxy()

    response = sdc_be_proxy.get_normatives()

    resources = []
    if response == 200:
        resources = json.loads(sdc_be_proxy.get_response_from_buffer())["resources"]
    else:
        print_and_exit(response, "Can't get normatives!")

    if len(resources) < properties.resource_len:
        import_main(sdc_be_proxy, update_version)
    else:
        upgrade_main(sdc_be_proxy)


if __name__ == '__main__':
    run()
