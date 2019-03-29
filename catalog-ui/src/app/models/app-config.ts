/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nokia. All rights reserved.
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
export interface IApi {
    baseUrl:string;

    //***** NEW API *******//
    GET_component:string;
    PUT_component:string;
    GET_component_validate_name:string;
    POST_changeLifecycleState:string;
    component_api_root:string;
    //*********//

    GET_user:string;
    GET_user_authorize:string;
    GET_all_users:string;
    POST_create_user;
    DELETE_delete_user;
    POST_edit_user_role;
    GET_resource:string;
    GET_resources_latestversion_notabstract:string;
    GET_resources_certified_not_abstract:string;
    GET_resources_certified_abstract:string;
    PUT_resource:string;
    GET_resource_property:string;
    GET_resource_artifact:string;
    GET_download_instance_artifact:string;
    POST_instance_artifact:string;
    GET_resource_additional_information:string;
    GET_service_artifact:string;
    GET_resource_interface_artifact:string;
    GET_resource_api_artifact:string;
    GET_resource_validate_name:string;
    GET_resource_artifact_types:string;
    GET_activity_log:string;
    GET_configuration_ui:string;
    GET_plugins_configuration:string;
    GET_plugin_online_state:string;
    GET_service:string;
    GET_ecomp_menu_items:string;
    GET_service_validate_name:string;
    GET_service_distributions:string;
    GET_service_distributions_components:string;
    POST_service_distribution_deploy:string;
    GET_element:string;
    GET_catalog:string;
    GET_resource_category:string;
    GET_service_category:string;
    resource_instance:string;
    GET_resource_instance_property:string;
    GET_relationship:string;
    GET_lifecycle_state_resource:string;
    GET_lifecycle_state_CHECKIN:string;
    GET_lifecycle_state_CERTIFICATIONREQUEST:string;
    GET_lifecycle_state_UNDOCHECKOUT:string;
    root:string;
    no_proxy_root:string;
    PUT_service:string;
    GET_download_artifact:string;
    GET_SDC_Version:string;
    GET_categories:string;
    POST_category:string;
    POST_subcategory:string;
    POST_change_instance_version:string;
    GET_requirements_capabilities:string;
    GET_onboarding:string;
    GET_component_from_csar_uuid:string;
    POST_GAB_Search:string;
    kibana:string;
    services_api_root:string;
}

export interface ILogConfig {
    minLogLevel:string;
    prefix:string;
}

export interface ICookie {
    junctionName:string;
    prefix:string;
    userIdSuffix:string;
    userFirstName:string;
    userLastName:string;
    userEmail:string;
}
export interface IUserTypes {
    admin:any;
    designer:any;
    tester:any;
}

export interface IConfigStatuses {
    inDesign:IConfigStatus;
    readyForCertification:IConfigStatus;
    inCertification:IConfigStatus;
    certified:IConfigStatus;
    distributed:IConfigStatus;
}

export interface IConfigStatus {
    name:string;
    values:Array<string>;
}

export interface IConfigRoles {
    ADMIN:IConfigRole;
    DESIGNER:IConfigRole;
    TESTER:IConfigRole;
    OPS:IConfigRole;
    GOVERNOR:IConfigRole;
}

export interface IConfigRole {
    changeLifecycleStateButtons:any;
}

export interface IConfigState {
    NOT_CERTIFIED_CHECKOUT:Array<IConfigDistribution>;
    NOT_CERTIFIED_CHECKIN:Array<IConfigDistribution>;
    READY_FOR_CERTIFICATION:Array<IConfigDistribution>;
    CERTIFICATION_IN_PROGRESS:Array<IConfigDistribution>;
    CERTIFIED:Array<IConfigDistribution>;
}

export interface IConfigDistribution {
    DISTRIBUTION_NOT_APPROVED:Array<ConfigMenuItem>;
    DISTRIBUTION_APPROVED:Array<ConfigMenuItem>;
    DISTRIBUTED:Array<ConfigMenuItem>;
    DISTRIBUTION_REJECTED:Array<ConfigMenuItem>;
}

export interface IConfirmationMessage {
    showComment:boolean;
    title:string;
    message:string;
}

export interface IConfirmationMessages {
    checkin:IConfirmationMessage;
    checkout:IConfirmationMessage;
    certify:IConfirmationMessage;
    failCertification:IConfirmationMessage;
    certificationRequest:IConfirmationMessage;
    approve:IConfirmationMessage;
    reject:IConfirmationMessage;
}

export interface IAlertMessage {
    title:string;
    message:string;
}

export interface IAlertMessages {
    deleteInstance:IAlertMessage;
    exitWithoutSaving:IConfirmationMessage;
}

class ConfigMenuItem {
    text:string;
    action:string;
    url:string;
    disable:boolean = false;
}

export interface IAppConfigurtaion {
    environment:string;
    api:IApi;
    hostedApplications:Array<IHostedApplication>;
    resourceTypesFilter:IResourceTypesFilter;
    logConfig:ILogConfig;
    cookie:ICookie;
    imagesPath:string;
    toscaFileExtension:string;
    csarFileExtension:string;
    testers:Array<ITester>
    tutorial:any;
    roles:Array<string>;
    cpEndPointInstances:Array<string>;
    openSource:boolean;
    showOutlook:boolean;
    validationConfigPath:string;
}

export interface IResourceTypesFilter {
    resource:Array<string>;
}

export interface IHostedApplication {
    moduleName:string;
    navTitle:string;
    defaultState:string;
    exists?:boolean;
    state:IHostedApplicationState;
}

export interface IHostedApplicationState {
    name:string;
    url:string;
    relativeHtmlPath:string;
    controllerName:string;
}

export interface ITester {
    email:string;
}

export interface IComponentType {
    RESOURCE:any;
    SERVICE:any;
}

export interface IAppMenu {
    roles:IConfigRoles;
    confirmationMessages:IConfirmationMessages;
    alertMessages:IAlertMessages;
    statuses:IConfigStatuses;
    catalogMenuItem:any;
    categoriesDictionary:any;
    canvas_buttons:Object;
    component_workspace_menu_option:any;
    LifeCycleStatuses:any;
    DistributionStatuses:any;
    ChangeLifecycleStateButton:any;
}
