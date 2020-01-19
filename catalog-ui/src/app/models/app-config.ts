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

'use strict';
// tslint:disable-next-line:interface-name
export interface IApi {
    baseUrl: string;

    GET_SDC_Setup_Data: string;
    GET_component: string;
    PUT_component: string;
    GET_component_validate_name: string;
    POST_changeLifecycleState: string;
    component_api_root: string;
    GET_user: string;
    GET_user_authorize: string;
    GET_all_users: string;
    POST_create_user;
    DELETE_delete_user;
    POST_edit_user_role;
    GET_resource: string;
    GET_resources_latestversion_notabstract: string;
    GET_resources_certified_not_abstract: string;
    GET_resources_certified_abstract: string;
    PUT_resource: string;
    GET_resource_property: string;
    GET_resource_artifact: string;
    GET_download_instance_artifact: string;
    POST_instance_artifact: string;
    GET_resource_additional_information: string;
    GET_service_artifact: string;
    GET_resource_interface_artifact: string;
    GET_resource_api_artifact: string;
    GET_resource_validate_name: string;
    GET_resource_artifact_types: string;
    GET_activity_log: string;
    GET_configuration_ui: string;
    GET_plugins_configuration: string;
    GET_plugin_online_state: string;
    GET_service: string;
    GET_ecomp_menu_items: string;
    GET_service_validate_name: string;
    GET_service_distributions: string;
    GET_service_distributions_components: string;
    POST_service_distribution_deploy: string;
    GET_element: string;
    GET_catalog: string;
    GET_resource_category: string;
    GET_service_category: string;
    resource_instance: string;
    GET_resource_instance_property: string;
    GET_relationship: string;
    GET_lifecycle_state_resource: string;
    GET_lifecycle_state_CHECKIN: string;
    GET_lifecycle_state_UNDOCHECKOUT: string;
    root: string;
    no_proxy_root: string;
    PUT_service: string;
    GET_download_artifact: string;
    GET_SDC_Version: string;
    GET_categories: string;
    POST_category: string;
    POST_subcategory: string;
    POST_change_instance_version: string;
    GET_requirements_capabilities: string;
    GET_onboarding: string;
    GET_component_from_csar_uuid: string;
    POST_GAB_Search: string;
    kibana: string;
    services_api_root: string;
    uicache_root: string;
    GET_uicache_catalog: string;
    GET_uicache_left_palette: string;
}

// tslint:disable-next-line:interface-name
export interface ILogConfig {
    minLogLevel: string;
    prefix: string;
}

// tslint:disable-next-line:interface-name
export interface ICookie {
    junctionName: string;
    prefix: string;
    userIdSuffix: string;
    userFirstName: string;
    userLastName: string;
    userEmail: string;
}

// tslint:disable-next-line:interface-name
export interface IConfigStatuses {
    inDesign: IConfigStatus;
    certified: IConfigStatus;
    distributed: IConfigStatus;
}

// tslint:disable-next-line:interface-name
export interface IConfigStatus {
    name: string;
    values: string[];
}

// tslint:disable-next-line:interface-name
export interface IConfigRoles {
    ADMIN: IConfigRole;
    DESIGNER: IConfigRole;
}

// tslint:disable-next-line:interface-name
export interface IConfigRole {
    changeLifecycleStateButtons: any;
}

// tslint:disable-next-line:interface-name
export interface IConfirmationMessage {
    showComment: boolean;
    title: string;
    message: string;
}

// tslint:disable-next-line:interface-name
export interface IConfirmationMessages {
    checkin: IConfirmationMessage;
    checkout: IConfirmationMessage;
    certify: IConfirmationMessage;
    failCertification: IConfirmationMessage;
    certificationRequest: IConfirmationMessage;
    approve: IConfirmationMessage;
    reject: IConfirmationMessage;
}

// tslint:disable-next-line:interface-name
export interface IAlertMessage {
    title: string;
    message: string;
}

// tslint:disable-next-line:interface-name
export interface IAlertMessages {
    deleteInstance: IAlertMessage;
    exitWithoutSaving: IConfirmationMessage;
    okButton: string;
}

class ConfigMenuItem {
    text: string;
    action: string;
    url: string;
    disable = false;
}

// tslint:disable-next-line:interface-name
export interface IAppConfigurtaion {
    environment: string;
    api: IApi;
    hostedApplications: IHostedApplication[];
    resourceTypesFilter: IResourceTypesFilter;
    logConfig: ILogConfig;
    cookie: ICookie;
    imagesPath: string;
    toscaFileExtension: string;
    csarFileExtension: string;
    testers: ITester[];
    tutorial: any;
    roles: string[];
    cpEndPointInstances: string[];
    openSource: boolean;
    showOutlook: boolean;
    validationConfigPath: string;
}

// tslint:disable-next-line:interface-name
export interface IResourceTypesFilter {
    resource: string[];
}

// tslint:disable-next-line:interface-name
export interface IHostedApplication {
    moduleName: string;
    navTitle: string;
    defaultState: string;
    exists?: boolean;
    state: IHostedApplicationState;
}

// tslint:disable-next-line:interface-name
export interface IHostedApplicationState {
    name: string;
    url: string;
    relativeHtmlPath: string;
    controllerName: string;
}

// tslint:disable-next-line:interface-name
export interface ITester {
    email: string;
}

// tslint:disable-next-line:interface-name
export interface IAppMenu {
    roles: IConfigRoles;
    confirmationMessages: IConfirmationMessages;
    alertMessages: IAlertMessages;
    statuses: IConfigStatuses;
    catalogMenuItem: any;
    categoriesDictionary: any;
    // tslint:disable-next-line:ban-types
    canvas_buttons: Object;
    component_workspace_menu_option: any;
    LifeCycleStatuses: any;
    DistributionStatuses: any;
    ChangeLifecycleStateButton: any;
}
