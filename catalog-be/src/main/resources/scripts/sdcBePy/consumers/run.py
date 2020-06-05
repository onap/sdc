import time

from sdcBePy import properties
from sdcBePy.common.healthCheck import check_backend, get_args
from sdcBePy.common.sdcBeProxy import SdcBeProxy
from sdcBePy.consumers.models.consumerCandidateList import get_consumers
from sdcBePy.users.run import colors


def be_consumers_init(be_ip, be_port, protocol, consumer_candidate_list):
    sdc_be_proxy = SdcBeProxy(be_ip, be_port, protocol)
    if check_backend(sdc_be_proxy, properties.retry_attempts):
        for consumer in consumer_candidate_list:
            if sdc_be_proxy.check_user(consumer.consumer_name) != 200:
                result = sdc_be_proxy.create_consumer(*consumer.get_parameters())
                if result == 201:
                    print('[INFO]: ' + consumer.consumer_name +
                          ' created, result: [' + str(result) + ']')
                else:
                    print('[ERROR]: ' + colors.FAIL + consumer.consumer_name + colors.END_C +
                          ' error creating , result: [' + str(result) + ']')
            else:
                print('[INFO]: ' + consumer.consumer_name + ' already exists')
    else:
        print('[ERROR]: ' + time.strftime('%Y/%m/%d %H:%M:%S') + colors.FAIL
              + ' Backend is DOWN :-(' + colors.END_C)
        raise Exception("Cannot communicate with the backend!")


def main():
    be_ip, be_port, protocol = get_args()
    be_consumers_init(be_ip, be_port, protocol, get_consumers())


if __name__ == '__main__':
    main()
