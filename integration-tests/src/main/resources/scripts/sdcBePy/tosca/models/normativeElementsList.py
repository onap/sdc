from os import path

from sdcBePy.tosca.models.normativeElementCandidate import NormativeElementCandidate

def get_normative_element_candidate_list(base_file_location):
    return [
        get_data(base_file_location),
        get_categories(base_file_location),
    ]

def get_normative_candidate(base_file_location, url, filename, zip_name, with_metadata=False):
    if path.isdir(base_file_location):
        return NormativeElementCandidate(base_file_location, url, filename, zip_name, with_metadata=with_metadata)

def get_data(base_file_location="/"):
    return get_normative_candidate(base_file_location + "data-types/",
                                   "/sdc2/rest/v1/catalog/uploadType/datatypes",
                                   "dataTypes",
                                   "dataTypesZip")

def get_categories(base_file_location="/"):
    return get_normative_candidate(base_file_location + "categories/",
                                   "/sdc2/rest/v1/catalog/uploadType/categories",
                                   "categoryTypes",
                                   "categoriesZip")
