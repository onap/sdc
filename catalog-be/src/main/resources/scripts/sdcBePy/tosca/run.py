import json

from sdcBePy import properties
from sdcBePy.common.logger import print_and_exit
from sdcBePy.common import logger

from sdcBePy.tosca.imports.run import main as import_main
from sdcBePy.tosca.main import parse_and_create_proxy
from sdcBePy.tosca.upgrade.run import main as upgrade_main


def run():
    logger.log("sdcinit starting")
    sdc_be_proxy, update_version = parse_and_create_proxy()

    response = sdc_be_proxy.get_normatives()

    resources = []
    if response == 200:
        resources = json.loads(sdc_be_proxy.get_response_from_buffer())["resources"]
    else:
        print_and_exit(response, "Can't get normatives!")

    if len(resources) < properties.resource_len:
        logger.log("Running IMPORT path (found {} resources, threshold {})".format(
            len(resources), properties.resource_len))
        import_main(sdc_be_proxy, update_version)
    else:
        logger.log("Running UPGRADE path (found {} resources, threshold {})".format(
            len(resources), properties.resource_len))
        upgrade_main(sdc_be_proxy)

    logger.log("sdcinit finished")


if __name__ == '__main__':
    run()
