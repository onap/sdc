import json

from sdcBePy.tosca.models.normativeTypeCandidate import NormativeTypeCandidate


class TypesToUpdate:

    def __init__(self, files):
        self.types_list = {}
        self.load_files(files)

    def load_files(self, files):
        for file in files:
            with open(file, 'r', encoding='utf-8') as stream:
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


def get_onap_sol_to_update_list(types, base_file_location):
    return [
        get_onap(types, base_file_location),
        get_sol(types, base_file_location)
    ]

def get_nfv_to_update_list(types, base_file_location):
    return [
        get_nfv(types, base_file_location),
        get_nfv_2_7_1(types, base_file_location),
        get_nfv_3_3_1(types, base_file_location),
        get_nfv_4_1_1(types, base_file_location),
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
                                  
def get_nfv_2_7_1(types, base_location="/"):
    return NormativeTypeCandidate(base_location + "nfv-types/2.7.1/",
                                  types.get_type("nfv_2_7_1"))

def get_nfv_3_3_1(types, base_location="/"):
    return NormativeTypeCandidate(base_location + "nfv-types/3.3.1/",
                                  types.get_type("nfv_3_3_1"))

def get_nfv_4_1_1(types, base_location="/"):
    return NormativeTypeCandidate(base_location + "nfv-types/4.1.1/",
                                  types.get_type("nfv_4_1_1"))

def get_onap(types, base_location="/"):
    return NormativeTypeCandidate(base_location + "onap-types/",
                                  types.get_type("onap"))


def get_sol(types, base_location="/"):
    return NormativeTypeCandidate(base_location + "sol-types/",
                                  types.get_type("sol"))
