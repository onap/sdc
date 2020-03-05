#!/usr/bin/env python3

import sys

from sdcBePy.common.normative.toscaElements import process_and_create_normative_element
from sdcBePy.tosca.main import get_args, usage
from sdcBePy.tosca.models.normativeElementsList import get_capability, get_data, get_relationship, \
    get_interface_lifecycle, get_categories, get_group, get_policy, get_annotation


def run(candidate):
    scheme, be_host, be_port, admin_user, _, debug = get_args()
    try:
        process_and_create_normative_element(candidate,
                                             scheme, be_host, be_port, admin_user,
                                             debug=debug,
                                             exit_on_success=True)
    except AttributeError:
        usage()
        sys.exit(3)


def run_import_data():
    data_candidate = get_data()
    run(data_candidate)


def run_import_capabilities():
    capability_candidate = get_capability()
    run(capability_candidate)


def run_import_relationship():
    relationship_candidate = get_relationship()
    run(relationship_candidate)


def run_import_interface_lifecycle():
    interface_lifecycle_candidate = get_interface_lifecycle()
    run(interface_lifecycle_candidate)


def run_import_categories():
    categories_candidate = get_categories()
    run(categories_candidate)


def run_import_group():
    group_candidate = get_group()
    run(group_candidate)


def run_import_policy():
    policy_candidate = get_policy()
    run(policy_candidate)


def run_import_annotation():
    annotation_candidate = get_annotation()
    run(annotation_candidate)


if __name__ == '__main__':
    run_import_data()
    # run_import_capabilities()
    # run_import_relationship()
    # run_import_interface_lifecycle()
    # run_import_categories()
    # run_import_group()
    # run_import_policy()
    # run_import_annotation()
