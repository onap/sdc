import os
import sys
from argparse import ArgumentParser

from sdcBePy.common.normative.toscaTypes import process_and_create_normative_types
from sdcBePy.tosca.main import usage, get_args
from sdcBePy.tosca.models.normativeTypeCandidate import NormativeTypeCandidate


def get_normative_prams():
    parser = ArgumentParser()

    path = os.path.dirname(__file__)
    parser.add_argument('--location', default=path + os.path.sep)
    parser.add_argument('--element', "-e", required=True)

    args = parser.parse_args()

    return args.location, [args.element]


def main():
    scheme, be_host, be_port, admin_user, _, debug = get_args()

    candidate = NormativeTypeCandidate(*get_normative_prams())
    try:
        process_and_create_normative_types(candidate,
                                           scheme, be_host, be_port, admin_user,
                                           debug=debug,
                                           exit_on_success=True)
    except AttributeError:
        usage()
        sys.exit(3)


if __name__ == '__main__':
    main()
