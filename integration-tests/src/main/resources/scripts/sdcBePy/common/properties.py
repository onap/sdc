class Properties:

    def __init__(self, retry_time=0,
                 retry_attempts=0, resource_len=0):
        self.retry_time = retry_time
        self.retry_attempts = retry_attempts
        self.resource_len = resource_len

    @property
    def retry_time(self):
        return self._retry_time

    @retry_time.setter
    def retry_time(self, value):
        self._validate(value)
        self._retry_time = value

    @property
    def retry_attempts(self):
        return self._retry_attempts

    @retry_attempts.setter
    def retry_attempts(self, value):
        self._validate(value)
        self._retry_attempts = value

    @property
    def resource_len(self):
        return self._resource_len

    @resource_len.setter
    def resource_len(self, value):
        self._validate(value)
        self._resource_len = value

    @staticmethod
    def _validate(value):
        if value < 0:
            raise ValueError("Properties below 0 is not possible")


def init_properties(retry_time, retry_attempts, resource_len=0):
    from sdcBePy import properties

    properties.retry_time = retry_time
    properties.retry_attempts = retry_attempts
    properties.resource_len = resource_len
