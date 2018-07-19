import sys

from importCommon import parse_cmd_line_params
from importNormativeElements import import_element

IMPORT_ANNOTATION_URL = "/sdc2/rest/v1/catalog/uploadType/annotationtypes"
ANNOTATION_FILE_PATH = "../../../import/tosca/annotation-types/"
ANNOTATION_ZIP_FILE = "annotationTypesZip"
ANNOTATION_ELEMENT_NAME = "annotationTypes"

#####################################################################################################################################################################################
#																																		       										#
# Import tosca data types																										   													#
# 																																			   										#
# activation :																																   										#
#       python importAnnotaionTypes.py [-s <scheme> | --scheme=<scheme> ] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]  #
#																																		  	   										#
# shortest activation (be host = localhost, be port = 8080): 																				   										#
#		python importAnnotaionTypes.py [-f <input file> | --ifile=<input file> ]												 				           							#
#																																		       										#
#####################################################################################################################################################################################


def import_annotation_types(scheme, be_host, be_port, admin_user, exit_on_success):
    import_element(scheme, be_host, be_port, admin_user, exit_on_success, ANNOTATION_FILE_PATH, IMPORT_ANNOTATION_URL,
                   ANNOTATION_ELEMENT_NAME, ANNOTATION_ZIP_FILE)


def main(argv):
    scheme, be_host, be_port, admin_user = parse_cmd_line_params(argv)
    import_annotation_types(scheme, be_host, be_port, admin_user, True)


if __name__ == "__main__":
    main(sys.argv[1:])
