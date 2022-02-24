from sdcBePy.tosca.models.normativeTypeCandidate import NormativeTypeCandidate


def get_normative_type_candidate_list(base_file_location):
    return [
        get_normative(base_file_location),
        get_heat(base_file_location),
        get_nfv(base_file_location),
        get_nfv_2_7_1(base_file_location),
        get_nfv_3_3_1(base_file_location),
        get_nfv_4_1_1(base_file_location),
        get_onap(base_file_location),
        get_sol(base_file_location)
    ]


def get_normative(base_file_location="/"):
    return NormativeTypeCandidate(base_file_location + "normative-types/",
                                  ["root",
                                   "compute",
                                   "softwareComponent",
                                   "webServer",
                                   "webApplication",
                                   "DBMS",
                                   "database",
                                   "objectStorage",
                                   "blockStorage",
                                   "containerRuntime",
                                   "containerApplication",
                                   "loadBalancer",
                                   "port", "network"])


def get_heat(base_file_location="/"):
    return NormativeTypeCandidate(base_file_location + "heat-types/",
                                  ["globalNetwork",
                                   "globalPort",
                                   "globalCompute",
                                   "volume",
                                   "cinderVolume",
                                   "contrailVirtualNetwork",
                                   "neutronNet",
                                   "neutronPort",
                                   "novaServer",
                                   "extVl",
                                   "internalVl",
                                   "extCp",
                                   "vl",
                                   "eline",
                                   "abstractSubstitute",
                                   "Generic_VFC",
                                   "Generic_VF",
                                   "Generic_CR",
                                   "Generic_PNF",
                                   "Generic_Service",
                                   "contrailNetworkRules",
                                   "contrailPort",
                                   "portMirroring",
                                   "serviceProxy",
                                   "contrailV2NetworkRules",
                                   "contrailV2VirtualNetwork",
                                   "securityRules",
                                   "contrailAbstractSubstitute",
                                   "contrailCompute",
                                   "contrailV2VirtualMachineInterface",
                                   "subInterface",
                                   "contrailV2VLANSubInterface",
                                   "multiFlavorVFC",
                                   "vnfConfiguration",
                                   "extCp2",
                                   "extNeutronCP",
                                   "extContrailCP",
                                   "portMirroringByPolicy",
                                   "forwardingPath",
                                   "configuration",
                                   "VRFObject",
                                   "extVirtualMachineInterfaceCP",
                                   "VLANNetworkReceptor",
                                   "VRFEntry",
                                   "subInterfaceV2",
                                   "contrailV2VLANSubInterfaceV2",
                                   "fabricConfiguration"])


def get_nfv(base_file_location="/"):
    return NormativeTypeCandidate(base_file_location + "nfv-types/",
                                  ["underlayVpn",
                                   "overlayTunnel",
                                   "genericNeutronNet",
                                   "allottedResource",
                                   "extImageFile",
                                   "extLocalStorage",
                                   "extZteCP",
                                   "extZteVDU",
                                   "extZteVL",
                                   "NS",
                                   "NSD",
                                   "NsVirtualLink",
                                   "VDU",
                                   "vduCompute",
                                   "Cp",
                                   "vduVirtualStorage",
                                   "vduVirtualBlockStorage",
                                   "vduVirtualFileStorage",
                                   "vduVirtualObjectStorage",
                                   "vduVirtualStorage",
                                   "vnfVirtualLink",
                                   "vnfExtCp",
                                   "vduCp",
                                   "VNF",
                                   "accessConnectivity",
                                   "OntPnf",
                                   "PonUni",
                                   "OltNni",
                                   "OntNni",
                                   "Sap",
                                   "ASD",
                                   "asdInNsd"])
                                   
def get_nfv_2_7_1(base_file_location="/"):
    return NormativeTypeCandidate(base_file_location + "nfv-types/2.7.1/",
                                  ["NfpPositionElement",
                                   "NfpPosition",
                                   "NFP",
                                   "Forwarding",
                                   "vduCompute",
                                   "vduVirtualFileStorage",
                                   "vnfExtCp",
                                   "vduCp",
                                   "vipCp"])

def get_nfv_3_3_1(base_file_location="/"):
    return NormativeTypeCandidate(base_file_location + "nfv-types/3.3.1/",
                                  ["vduVirtualBlockStorage",
                                   "VNF",
                                   "NS"])

def get_nfv_4_1_1(base_file_location="/"):
    return NormativeTypeCandidate(base_file_location + "nfv-types/4.1.1/",
                                  ["VNF",
                                   "osContainer",
                                   "osContainerGroup",
                                   "vduCp",
                                   "virtualCp"])

def get_onap(base_file_location="/"):
    return NormativeTypeCandidate(base_file_location + "onap-types/",
                                  # Add desired type names to the list
                                  [])


def get_sol(base_file_location="/"):
    return NormativeTypeCandidate(base_file_location + "onap-types/",
                                  # Add desired type names to the list
                                  [])


def get_heat1707(base_file_location="/"):
    return NormativeTypeCandidate(base_file_location + "heat-types/",
                                  ["volume",
                                   "cinderVolume",
                                   "extVl",
                                   "extCp",
                                   "Generic_VFC",
                                   "Generic_VF",
                                   "Generic_PNF",
                                   "Generic_Service",
                                   "globalPort",
                                   "globalNetwork",
                                   "contrailV2VirtualMachineInterface",
                                   "contrailV2VLANSubInterface",
                                   "contrailPort",
                                   "contrailV2VirtualNetwork",
                                   "contrailVirtualNetwork",
                                   "neutronNet",
                                   "neutronPort",
                                   "multiFlavorVFC",
                                   "vnfConfiguration"])


def get_heat1702_3537(base_file_location="/"):
    return NormativeTypeCandidate(base_file_location + "heat-types/",
                                  ["contrailPort",
                                   "contrailV2VirtualMachineInterface",
                                   "neutronPort",
                                   "contrailCompute",
                                   "novaServer",
                                   "contrailV2VirtualNetwork",
                                   "contrailVirtualNetwork",
                                   "neutronNet"])


def get_heat_version(base_file_location="/"):
    return NormativeTypeCandidate(base_file_location + "heat_types/",
                                  ["contrailV2VirtualMachineInterface",
                                   "neutronPort"])
