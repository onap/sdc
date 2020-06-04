from sdcBePy.common.helpers import check_arguments_not_none


class ConsumerCandidate:

    def __init__(self, consumer_name, slat, password):
        if not check_arguments_not_none(consumer_name, slat, password):
            raise AttributeError("The consumer_name, slat or password are missing!")

        self.consumer_name = consumer_name
        self.slat = slat
        self.password = password

    def get_parameters(self):
        return {
            'consumerName': self.consumer_name,
            'consumerSalt': self.slat,
            'consumerPassword': self.password,
        }
