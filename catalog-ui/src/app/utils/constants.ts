/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

/**
 * Created by obarda on 2/18/2016.
 */

export let DEFAULT_ICON = 'defaulticon';
export let CP_END_POINT = 'CpEndPoint';
export let CHANGE_COMPONENT_CSAR_VERSION_FLAG = 'changeComponentCsarVersion';

export class ComponentType {
    static SERVICE = 'SERVICE';
    static RESOURCE = 'RESOURCE';
    static SERVICE_PROXY = 'ServiceProxy'
}

export class ServerTypeUrl {
    static RESOURCES = 'resources/';
    static SERVICES = 'services/';
}

export class ResourceType {
    static VF = 'VF';
    static VL = 'VL';
    static CP = 'CP';
    static VFC = 'VFC';
    static VFCMT = 'VFCMT';
    static PNF = 'PNF';
    static CVFC = 'CVFC';
    static CONFIGURATION = 'Configuration';
}

export class ComponentState {
    static CERTIFICATION_IN_PROGRESS = 'CERTIFICATION_IN_PROGRESS';
    static CERTIFIED = 'CERTIFIED';
    static NOT_CERTIFIED_CHECKOUT = 'NOT_CERTIFIED_CHECKOUT';
    static NOT_CERTIFIED_CHECKIN = 'NOT_CERTIFIED_CHECKIN';
    static READY_FOR_CERTIFICATION = 'READY_FOR_CERTIFICATION';
}

export class DistributionStatus {
    DISTRIBUTION_NOT_APPROVED = 'DISTRIBUTION_NOT_APPROVED';
    DISTRIBUTION_APPROVED = 'DISTRIBUTION_APPROVED';
    DISTRIBUTED = 'DISTRIBUTED';
    DISTRIBUTION_REJECTED = 'DISTRIBUTION_REJECTED';
}

export class ArtifactGroupType {
    static DEPLOYMENT = "DEPLOYMENT";
    static INFORMATION = "INFORMATIONAL";
    static SERVICE_API = "SERVICE_API";
}

export class ArtifactType {
    static HEAT = "HEAT";
    static HEAT_VOL = "HEAT_VOL";
    static HEAT_NET = "HEAT_NET";
    static VF_LICENSE = "VF_LICENSE";
    static VENDOR_LICENSE = "VENDOR_LICENSE";
    static THIRD_PARTY_RESERVED_TYPES = {
        WORKFLOW: "WORKFLOW",
        NETWORK_CALL_FLOW: "NETWORK_CALL_FLOW",
        AAI_SERVICE_MODEL: "AAI_SERVICE_MODEL",
        AAI_VF_MODEL: "AAI_VF_MODEL",
        AAI_VF_MODULE_MODEL: "AAI_VF_MODULE_MODEL",
        AAI_VF_INSTANCE_MODEL: "AAI_VF_INSTANCE_MODEL"
    };
    static TOSCA = {TOSCA_TEMPLATE: "TOSCA_TEMPLATE", TOSCA_CSAR: "TOSCA_CSAR"};
}

export class SEVERITY {
    public static DEBUG = 'DEBUG';
    public static INFO = 'INFO';
    public static WARNING = 'WARNING';
    public static ERROR = 'ERROR';
}

export class PROPERTY_TYPES {
    public static STRING = 'string';
    public static INTEGER = 'integer';
    public static FLOAT = 'float';
    public static BOOLEAN = 'boolean';
    public static JSON = 'json';
    public static MAP = 'map';
    public static LIST = 'list';
    public static SCALAR = 'scalar-unit';
    public static SCALAR_FREQUENCY = 'scalar-unit.frequency';
    public static SCALAR_SIZE = 'scalar-unit.size';
    public static SCALAR_TIME = 'scalar-unit.time';
}

export class SOURCES {
    public static A_AND_AI = 'A&AI';
    public static ORDER = 'Order';
    public static RUNTIME = 'Runtime';
}

export class PROPERTY_DATA {
    public static TYPES = [PROPERTY_TYPES.STRING, PROPERTY_TYPES.INTEGER, PROPERTY_TYPES.FLOAT, PROPERTY_TYPES.BOOLEAN, PROPERTY_TYPES.JSON, PROPERTY_TYPES.SCALAR, PROPERTY_TYPES.SCALAR_FREQUENCY, PROPERTY_TYPES.SCALAR_SIZE, PROPERTY_TYPES.SCALAR_TIME, PROPERTY_TYPES.LIST, PROPERTY_TYPES.MAP];
    public static SIMPLE_TYPES = [PROPERTY_TYPES.STRING, PROPERTY_TYPES.INTEGER, PROPERTY_TYPES.FLOAT, PROPERTY_TYPES.BOOLEAN, PROPERTY_TYPES.JSON, PROPERTY_TYPES.SCALAR, PROPERTY_TYPES.SCALAR_FREQUENCY, PROPERTY_TYPES.SCALAR_SIZE, PROPERTY_TYPES.SCALAR_TIME];
    public static SCALAR_TYPES = [PROPERTY_TYPES.SCALAR, PROPERTY_TYPES.SCALAR_FREQUENCY, PROPERTY_TYPES.SCALAR_SIZE, PROPERTY_TYPES.SCALAR_TIME];
    public static ROOT_DATA_TYPE = "tosca.datatypes.Root";
    public static OPENECOMP_ROOT = "org.openecomp.datatypes.Root";
    public static SUPPLEMENTAL_DATA = "supplemental_data";
    public static SOURCES = [SOURCES.A_AND_AI, SOURCES.ORDER, SOURCES.RUNTIME];
}

export class PROPERTY_VALUE_CONSTRAINTS {
    public static MAX_LENGTH = 2500;
    public static JSON_MAX_LENGTH = 4096;
}

export class Role {
    public static ADMIN = 'ADMIN';
    public static DESIGNER = 'DESIGNER';
    public static TESTER = 'TESTER';
    public static OPS = 'OPS';
    public static GOVERNOR = 'GOVERNOR';
}

export enum FormState{
    CREATE,
    UPDATE,
    IMPORT,
    VIEW
}

export class WorkspaceMode {
    public static CREATE = 'create';
    public static EDIT = 'edit';
    public static IMPORT = 'import';
    public static VIEW = 'view';
}

export class ImagesUrl {
    public static RESOURCE_ICONS = '/assets/styles/images/resource-icons/';
    public static SERVICE_ICONS = '/assets/styles/images/service-icons/';
    public static SERVICE_PROXY_ICONS = '/assets/styles/images/service-proxy-icons/';
    public static SELECTED_UCPE_INSTANCE = '/assets/styles/images/resource-icons/selectedUcpeInstance.png';
    public static SELECTED_CP_INSTANCE = '/assets/styles/images/resource-icons/selectedCPInstance.png';
    public static SELECTED_VL_INSTANCE = '/assets/styles/images/resource-icons/selectedVLInstance.png';
    public static CANVAS_PLUS_ICON = '/assets/styles/images/resource-icons/canvasPlusIcon.png';
    public static MODULE_ICON = '/assets/styles/images/resource-icons/module.png';
    public static OPEN_MODULE_ICON = '/assets/styles/images/resource-icons/openModule.png';
    public static OPEN_MODULE_HOVER_ICON = '/assets/styles/images/resource-icons/openModuleHover.png';
    public static CLOSE_MODULE_ICON = '/assets/styles/images/resource-icons/closeModule.png';
    public static CLOSE_MODULE_HOVER_ICON = '/assets/styles/images/resource-icons/closeModuleHover.png';
}

export class ModalType {
    static STANDARD = 'standard';
    static ERROR = 'error';
    static ALERT = 'alert';
}

export class ServerErrors {
    static ERROR_TITLE = 'Error';
    static DEFAULT_ERROR = 'Error getting response from server';
    static MESSAGE_ERROR = 'Wrong error format from server';
}

export class GraphColors {
    public static NOT_CERTIFIED_LINK = 'rgb(218,31,61)';
    public static VL_LINK = 'rgb(216,216,216)';
    public static ACTIVE_LINK = '#30bdf2';
    public static BASE_LINK = 'rgb(55,55,55)';
    public static NODE_BACKGROUND_COLOR = 'rgba(46, 162, 157, 0.24)';
    public static NODE_SHADOW_COLOR = 'rgba(198, 230, 228, 0.7)';
    public static NODE_OVERLAPPING_BACKGROUND_COLOR = 'rgba(179, 10, 60, 0.24)';
    public static NODE_OVERLAPPING_SHADOW_COLOR = 'rgba(236, 194, 206, 0.7)';
    public static NODE_UCPE_CP = '#9063cd';
    public static NODE_UCPE = '#fbfbfb';
    public static NODE_SELECTED_BORDER_COLOR = '#30bdf2';
}
export class GraphTransactionLogText {
    public static REMOVE_TEMP_LINK = "remove tempLink";
    public static DELETE_LINK = "delete link";
    public static ADD_LINK = "delete link";
    public static ADD_NODE = "adding node";
}

export class GraphUIObjects {
    public static HANDLE_SIZE = 18; 
    public static NODE_OVERLAP_MIN_SIZE = 30; 
    public static DEFAULT_RESOURCE_WIDTH = 65;
    public static SMALL_RESOURCE_WIDTH = 21;
    public static LINK_MENU_HEIGHT = 420;
    public static TOP_HEADER_HEIGHT = 200;
    public static TOOLTIP_OFFSET_X = 50;
    public static TOOLTIP_OFFSET_Y = 145;
    public static TOOLTIP_LINK_OFFSET_X = 35;
    public static TOOLTIP_LINK_OFFSET_Y = 75;
    public static MENU_LINK_VL_HEIGHT_OFFSET = 250;
    public static MENU_LINK_VL_WIDTH_OFFSET = 200;
    public static MENU_LINK_SIMPLE_HEIGHT_OFFSET = 180;
    public static MENU_LINK_SIMPLE_WIDTH_OFFSET = 130;
    public static DIAGRAM_RIGHT_WIDTH_OFFSET = 248;
    public static DIAGRAM_HEADER_OFFSET = 103;
    public static DIAGRAM_PALETTE_WIDTH_OFFSET = 247;
    // public static COMPOSITION_HEADER_OFFSET = 50;
    // public static COMPOSITION_NODE_MENU_WIDTH = 230;
    // public static COMPOSITION_NODE_MENU_HEIGHT = 200;
    // public static COMPOSITION_RIGHT_PANEL_OFFSET = 300;
}


export class States {
    public static WORKSPACE_GENERAL = 'workspace.general';
    public static WORKSPACE_ACTIVITY_LOG = 'workspace.activity_log';
    public static WORKSPACE_DEPLOYMENT_ARTIFACTS = 'workspace.deployment_artifacts';
    public static WORKSPACE_PROPERTIES = 'workspace.properties';
    public static WORKSPACE_SERVICE_INPUTS = 'workspace.service_inputs';
    public static WORKSPACE_RESOURCE_INPUTS = 'workspace.resource_inputs';
    public static WORKSPACE_ATTRIBUTES = 'workspace.attributes';
    public static WORKSPACE_INFORMATION_ARTIFACTS = 'workspace.information_artifacts';
    public static WORKSPACE_TOSCA_ARTIFACTS = 'workspace.tosca_artifacts';
    public static WORKSPACE_COMPOSITION = 'workspace.composition';
    public static WORKSPACE_NETWORK_CALL_FLOW = 'workspace.network_call_flow';
    public static WORKSPACE_MANAGEMENT_WORKFLOW = 'workspace.management_workflow';
    public static WORKSPACE_DEPLOYMENT = 'workspace.deployment';
    public static WORKSPACE_DISTRIBUTION = 'workspace.distribution';
    public static WORKSPACE_PROPERTIES_ASSIGNMENT = 'workspace.properties_assignment';
    public static WORKSPACE_REQUIREMENTS_AND_CAPABILITIES = 'workspace.reqAndCap';
    public static WORKSPACE_NG2 = 'workspace.ng2';
}

export class EVENTS {
    static RESOURCE_LEFT_PALETTE_UPDATE_EVENT = "resourceLeftPanelUpdateEvent";
    static RESOURCE_PNF_LEFT_PALETTE_UPDATE_EVENT = "resourcePNFLeftPanelUpdateEvent";
    static SERVICE_LEFT_PALETTE_UPDATE_EVENT = "serviceLeftPanelUpdateEvent";
    static VL_LEFT_PALETTE_UPDATE_EVENT = "vlLeftPanelUdateEvent";
    static ON_CSAR_LOADING = "onCsarLoading";
    static DOWNLOAD_ARTIFACT_FINISH_EVENT = "downloadArtifactFinishEvent";
    static ON_WORKSPACE_SAVE_BUTTON_CLICK = "onWorkspaceSaveButtonClick";
    static ON_WORKSPACE_SAVE_BUTTON_SUCCESS = "onWorkspaceSaveButtonSuccess";
    static ON_WORKSPACE_SAVE_BUTTON_ERROR = "onWorkspaceSaveButtonError";
    static ON_CHECKOUT = "onCheckout";
    static ON_REVERT = "onRevert";

    //Loader events
    static SHOW_LOADER_EVENT = "showLoaderEvent";
    static HIDE_LOADER_EVENT = "hideLoaderEvent";
}


export class UNIQUE_GROUP_PROPERTIES_NAME {
    public static MIN_VF_MODULE_INSTANCES = 'min_vf_module_instances';
    public static MAX_VF_MODULE_INSTANCES = 'max_vf_module_instances';
    public static INITIAL_COUNT = 'initial_count';
    public static IS_BASE = 'isBase';
    public static VF_MODULE_TYPE = 'vf_module_type';
    public static VF_MODULE_LABEL = 'vf_module_label';
    public static VF_MODULE_DESCRIPTION = 'vf_module_description';
    public static VOLUME_GROUP = 'volume_group';
}


export class GRAPH_EVENTS {
    static ON_COMPOSITION_GRAPH_DATA_LOADED = 'onCompositionGraphDataLoaded';
    static ON_DEPLOYMENT_GRAPH_DATA_LOADED = 'onDeploymentGraphDataLoaded';
    static ON_NODE_SELECTED = "onNodeSelected";
    static ON_GRAPH_BACKGROUND_CLICKED = "onGraphBackgroundClicked";
    static ON_PALETTE_COMPONENT_HOVER_IN = 'onPaletteComponentHoverIn';
    static ON_PALETTE_COMPONENT_HOVER_OUT = 'onPaletteComponentHoverOut';
    static ON_PALETTE_COMPONENT_DRAG_START = 'onPaletteComponentDragStart';
    static ON_PALETTE_COMPONENT_DRAG_ACTION = 'onPaletteComponentDragAction';
    static ON_COMPONENT_INSTANCE_NAME_CHANGED = 'onComponentInstanceNameChanged';
    static ON_DELETE_COMPONENT_INSTANCE = 'onDeleteComponentInstance';
    static ON_DELETE_MULTIPLE_COMPONENTS = 'onDeleteMultipleComponents';
    static ON_DELETE_EDGE = 'onDeleteEdge';
    static ON_INSERT_NODE_TO_UCPE = 'onInsertNodeToUCPE';
    static ON_REMOVE_NODE_FROM_UCPE = 'onRemoveNodeFromUCPE';
    static ON_VERSION_CHANGED = 'onVersionChanged';
    static ON_CREATE_COMPONENT_INSTANCE = 'onCreateComponentInstance';
}


export class COMPONENT_FIELDS {
    static COMPONENT_INSTANCES_PROPERTIES = "componentInstancesProperties";
    static COMPONENT_INSTANCES_ATTRIBUTES = "componentInstancesAttributes";
    static COMPONENT_ATTRIBUTES = "attributes";
    static COMPONENT_INSTANCES = "componentInstances";
    static COMPONENT_INSTANCES_RELATION = "componentInstancesRelations";
    static COMPONENT_INPUTS = "inputs";
    static COMPONENT_METADATA = "metadata";
    static COMPONENT_DEPLOYMENT_ARTIFACTS = "deploymentArtifacts";
    static COMPONENT_INFORMATIONAL_ARTIFACTS = "artifacts";
    static COMPONENT_PROPERTIES = "properties";
    static COMPONENT_CAPABILITIES = "capabilities";
    static COMPONENT_REQUIREMENTS = "requirements";
    static COMPONENT_TOSCA_ARTIFACTS = "toscaArtifacts";
    static COMPONENT_GROUPS = "groups";

}
export class API_QUERY_PARAMS {
    static INCLUDE = "include";
}
