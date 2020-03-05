import json

from sdcBePy.tosca.models.normativeTypeCandidate import NormativeTypeCandidate


class TypesToUpdate:

    def __init__(self, files):
        self.types_list = {}
        self.load_files(files)

    def load_files(self, files):
        for file in files:
            with open(file, 'r') as stream:
                _types = json.load(stream)
                for type_key, type_value in _types.items():
                    self.types_list[type_key] = type_value

    def get_type(self, key):
        return self.types_list[key]


def get_heat_and_normative_to_update_list(types, base_file_location):
    return [
        get_heat(types, base_file_location),
        get_normative(types, base_file_location)
    ]


def get_nfv_onap_sol_to_update_list(types, base_file_location):
    return [
        get_nfv(types, base_file_location),
        get_onap(types, base_file_location),
        get_sol(types, base_file_location)
    ]


def get_heat(types, base_location="/"):
    return NormativeTypeCandidate(base_location + "heat-types/",
                                  types.get_type("heat"))


def get_normative(types, base_location="/"):
    return NormativeTypeCandidate(base_location + "normative-types/",
                                  types.get_type("normative"))


def get_nfv(types, base_location="/"):
    return NormativeTypeCandidate(base_location + "nfv-types/",
                                  types.get_type("nfv"))


def get_onap(types, base_location="/"):
    return NormativeTypeCandidate(base_location + "onap-types/",
                                  types.get_type("onap"))


def get_sol(types, base_location="/"):
    return NormativeTypeCandidate(base_location + "sol-types/",
                                  types.get_type("sol"))
