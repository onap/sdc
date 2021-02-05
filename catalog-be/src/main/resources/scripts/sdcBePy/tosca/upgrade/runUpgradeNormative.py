#!/usr/bin/env python3

from sdcBePy.tosca.imports.runNormativeType import run
from sdcBePy.tosca.models.normativeToUpdateList import get_heat, get_normative, get_nfv, get_2_7_1, get_nfv_3_3_1, get_nfv_4_1_1, get_onap, get_sol
from sdcBePy.tosca.models.normativeTypesList import get_heat1707, get_heat1702_3537, get_heat_version
from sdcBePy.tosca.upgrade.run import get_all_types

all_types = get_all_types()


def run_upgrade_heat():
    normative_candidate = get_heat(all_types)
    run(normative_candidate)


def run_upgrade_normative():
    normative_candidate = get_normative(all_types)
    run(normative_candidate)


def run_upgrade_nfv():
    normative_candidate = get_nfv(all_types)
    run(normative_candidate)
    
def run_upgrade_nfv_2_7_1():
    normative_candidate = get_nfv_2_7_1(all_types)
    run(normative_candidate)

def run_upgrade_nfv_3_3_1():
    normative_candidate = get_nfv_3_3_1(all_types)
    run(normative_candidate)

def run_upgrade_nfv_4_1_1():
    normative_candidate = get_nfv_4_1_1(all_types)
    run(normative_candidate)

def run_upgrade_onap():
    normative_candidate = get_onap(all_types)
    run(normative_candidate)


def run_upgrade_sol():
    normative_candidate = get_sol(all_types)
    run(normative_candidate)


def run_upgrade_heat1707():
    normative_candidate = get_heat1707()
    run(normative_candidate)


def run_upgrade_heat1707_3537():
    normative_candidate = get_heat1702_3537()
    run(normative_candidate)


def run_upgrade_heat_version():
    normative_candidate = get_heat_version()
    run(normative_candidate)


if __name__ == '__main__':
    run_upgrade_heat()
    # run_upgrade_normative()
    # run_upgrade_nfv()
    # run_upgrade_onap()
    # run_upgrade_sol()
    # run_upgrade_heat1707()
    # run_upgrade_heat1707_3537()
    # run_upgrade_heat_version()
