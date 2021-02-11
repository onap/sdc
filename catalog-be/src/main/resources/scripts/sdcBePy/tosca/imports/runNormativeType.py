#!/usr/bin/env python3

import sys

from sdcBePy.common.normative.toscaTypes import process_and_create_normative_types
from sdcBePy.tosca.main import get_args, usage
from sdcBePy.tosca.models.normativeTypesList import get_normative, get_heat, get_nfv, get_nfv_2_7_1, get_nfv_3_3_1, get_nfv_4_1_1, get_onap, get_sol


def run(candidate, exit_on_success=True):
    scheme, be_host, be_port, admin_user, update_version, debug = get_args()
    try:
        process_and_create_normative_types(candidate,
                                           scheme,
                                           be_host,
                                           be_port,
                                           admin_user,
                                           update_version=update_version,
                                           debug=debug,
                                           exit_on_success=exit_on_success)
    except AttributeError:
        usage()
        sys.exit(3)


def run_import_normative():
    normative_candidate = get_normative()
    run(normative_candidate)


def run_import_heat():
    heat_candidate = get_heat()
    run(heat_candidate)


def run_import_nfv():
    nfv_candidate = get_nfv()
    run(nfv_candidate)

def run_import_nfv_2_7_1():
    nfv_candidate = get_nfv_2_7_1()
    run(nfv_candidate)

def run_import_nfv_3_3_1():
    nfv_candidate = get_nfv_3_3_1()
    run(nfv_candidate)

def run_import_nfv_4_1_1():
    nfv_candidate = get_nfv_4_1_1()
    run(nfv_candidate)

def run_import_onap():
    onap_candidate = get_onap()
    run(onap_candidate)


def run_import_sol():
    sol_candidate = get_sol()
    run(sol_candidate)


if __name__ == '__main__':
    run_import_normative()
    # run_import_heat()
    # run_import_nfv()
    # run_import_onap()
    # run_import_sol()
