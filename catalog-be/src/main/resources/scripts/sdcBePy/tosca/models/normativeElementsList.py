from os import path

from sdcBePy.tosca.models.normativeElementCandidate import NormativeElementCandidate


def get_normative_element_candidate_list(base_file_location):
    return [
        get_data(base_file_location),
        get_capability(base_file_location),
        get_relationship(base_file_location),
        get_interface_lifecycle(base_file_location),
        get_categories(base_file_location),
        get_artifacts(base_file_location)
    ]


def get_normative_element_with_metadata_list(base_file_location):
    return [
        get_group(base_file_location),
        get_policy(base_file_location)
    ]


def get_normative_candidate(base_file_location, url, filename, zip_name, with_metadata=False):
    if path.isdir(base_file_location):
        return NormativeElementCandidate(base_file_location, url, filename, zip_name, with_metadata=with_metadata)


def get_data(base_file_location="/"):
    return get_normative_candidate(base_file_location + "data-types/",
                                   "/sdc2/rest/v1/catalog/uploadType/datatypes",
                                   "dataTypes",
                                   "dataTypesZip")


def get_capability(base_file_location="/"):
    return get_normative_candidate(base_file_location + "capability-types/",
                                   "/sdc2/rest/v1/catalog/uploadType/capability",
                                   "capabilityTypes",
                                   "capabilityTypeZip")


def get_relationship(base_file_location="/"):
    return get_normative_candidate(base_file_location + "relationship-types/",
                                   "/sdc2/rest/v1/catalog/uploadType/relationship",
                                   "relationshipTypes",
                                   "relationshipTypeZip")


def get_interface_lifecycle(base_file_location="../../../import/tosca/"):
    return get_normative_candidate(base_file_location + "interface-lifecycle-types/",
                                   "/sdc2/rest/v1/catalog/uploadType/interfaceLifecycle",
                                   "interfaceLifecycleTypes",
                                   "interfaceLifecycleTypeZip")


def get_categories(base_file_location="/"):
    return get_normative_candidate(base_file_location + "categories/",
                                   "/sdc2/rest/v1/catalog/uploadType/categories",
                                   "categoryTypes",
                                   "categoriesZip")


def get_artifacts(base_file_location="/"):
    return get_normative_candidate(base_file_location + "artifact-types/",
                                   "/sdc2/rest/v1/catalog/uploadType/artifactTypes",
                                   "artifactTypes",
                                   "artifactsZip")


def get_group(base_file_location="/"):
    return get_normative_candidate(base_file_location + "group-types/",
                                   "/sdc2/rest/v1/catalog/uploadType/grouptypes",
                                   "groupTypes",
                                   "groupTypesZip",
                                   with_metadata=True)


def get_policy(base_file_location="/"):
    return get_normative_candidate(base_file_location + "policy-types/",
                                   "/sdc2/rest/v1/catalog/uploadType/policytypes",
                                   "policyTypes",
                                   "policyTypesZip",
                                   with_metadata=True)


def get_annotation(base_file_location="/"):
    return get_normative_candidate(base_file_location + "annotation-types/",
                                   "/sdc2/rest/v1/catalog/uploadType/annotationtypes",
                                   "annotationTypes",
                                   "annotationTypesZip")
