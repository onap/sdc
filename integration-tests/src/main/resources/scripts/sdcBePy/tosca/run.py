import json

from sdcBePy.common.logger import print_and_exit

from sdcBePy.tosca.imports.run import main as import_main
from sdcBePy.tosca.main import parse_and_create_proxy


def run():
    sdc_be_proxy = parse_and_create_proxy()

    response = sdc_be_proxy.get_normatives()

    resources = []
    if response == 200:
        resources = json.loads(sdc_be_proxy.get_response_from_buffer())["resources"]
    else:
        print_and_exit(response, "Can't get normatives!")

    if not contains(resources, lambda resource: "model" in resource and resource["model"] == "TEST MODEL"):
        import_main(sdc_be_proxy)
    else:
        print_and_exit(403, "TEST MODEL already created")

def contains(list, filter):
    for x in list:
        if filter(x):
            return True
    return False

if __name__ == '__main__':
    run()
