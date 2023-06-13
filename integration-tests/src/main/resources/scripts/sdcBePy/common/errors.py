
class ResourceCreationError(Exception):

    def __init__(self, message, error_code, resource_name=None):
        self.message = message
        self.error_code = error_code
        self.resource_name = resource_name
