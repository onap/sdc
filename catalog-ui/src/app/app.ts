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

//import 'restangular';
//import 'angular-ui-router';
import "reflect-metadata";
import 'ng-infinite-scroll';
import './modules/filters.ts';
import './modules/utils.ts';
import './modules/directive-module.ts';
import './modules/service-module';
import './modules/view-model-module.ts';

import {
    DataTypesService,
    LeftPaletteLoaderService,
    EcompHeaderService,
    CookieService,
    ConfigurationUiService,
    CacheService,
    SdcVersionService,
    ICategoryResourceClass,
    EntityService
} from "./services";
import { UserService } from "./ng2/services/user.service";
import {forwardRef} from '@angular/core';
import {UpgradeAdapter} from '@angular/upgrade';
import {CHANGE_COMPONENT_CSAR_VERSION_FLAG, States} from "./utils";
import {IAppConfigurtaion, IAppMenu, IMainCategory, Resource, IHostedApplication} from "./models";
import {ComponentFactory} from "./utils/component-factory";
import {ModalsHandler} from "./utils/modals-handler";
import {downgradeComponent} from "@angular/upgrade/static";

import {AppModule} from './ng2/app.module';
import {PropertiesAssignmentComponent} from "./ng2/pages/properties-assignment/properties-assignment.page.component";
import {Component} from "./models/components/component";
import {ComponentServiceNg2} from "./ng2/services/component-services/component.service";
import {ComponentMetadata} from "./models/component-metadata";
import {Categories} from "./models/categories";
import {IUserProperties} from "./models/user";
import {SearchWithAutoCompleteComponent} from "./ng2/components/ui/search-with-autocomplete/search-with-autocomplete.component";


let moduleName:string = 'sdcApp';
let viewModelsModuleName:string = 'Sdc.ViewModels';
let directivesModuleName:string = 'Sdc.Directives';
let servicesModuleName:string = 'Sdc.Services';
let filtersModuleName:string = 'Sdc.Filters';
let utilsModuleName:string = 'Sdc.Utils';

// Load configuration according to environment.
declare var __ENV__:string;
let sdcConfig:IAppConfigurtaion;
let sdcMenu:IAppMenu;
let pathPrefix:string = '';
if (__ENV__ === 'dev') {
    sdcConfig = require('./../../configurations/dev.js');
} else if (__ENV__ === 'prod') {
    sdcConfig = require('./../../configurations/prod.js');
    pathPrefix = 'sdc1/';
} else {
    console.log("ERROR: Environment configuration not found!");
}
sdcMenu = require('./../../configurations/menu.js');

let dependentModules:Array<string> = [
    'ui.router',
    'ui.bootstrap',
    'ui.bootstrap.tpls',
    'ngDragDrop',
    'ui-notification',
    'ngResource',
    'ngSanitize',
    'naif.base64',
    'base64',
    'uuid4',
    'checklist-model',
    'angular.filter',
    'pascalprecht.translate',
    '720kb.tooltips',
    'restangular',
    'angular-clipboard',
    'angularResizable',
    'infinite-scroll',
    viewModelsModuleName,
    directivesModuleName,
    servicesModuleName,
    filtersModuleName,
    utilsModuleName
];

// ===================== Hosted applications section ====================
// Define here new hosted apps
let hostedApplications:Array<IHostedApplication> = [
    {
        "moduleName": "dcaeApp",
        "navTitle": "DCAE",
        "defaultState": 'dcae.app.home',
        "state": {
            "name": "dcae",
            "url": "/dcae",
            "relativeHtmlPath": 'dcae-app/dcae-app-view.html',
            "controllerName": '.DcaeAppViewModel'
        }
    }
];

// Check if module exists (in case the javascript was not loaded).
let isModuleExists = (moduleName:string):boolean => {
    try {
        angular.module(moduleName);
        dependentModules.push(moduleName);
        return true;
    } catch (e) {
        console.log('Module ' + moduleName + ' does not exists');
        return false;
    }
};

// Check which hosted applications exists
_.each(hostedApplications, (hostedApp)=> {
    if (isModuleExists(hostedApp.moduleName)) {
        hostedApp['exists'] = true;
    }
});
// ===================== Hosted applications section ====================

export const ng1appModule:ng.IModule = angular.module(moduleName, dependentModules);
angular.module('sdcApp').directive('propertiesAssignment', downgradeComponent({component: PropertiesAssignmentComponent}) as angular.IDirectiveFactory);
angular.module('sdcApp').directive('ng2SearchWithAutocomplete',
    downgradeComponent({
        component: SearchWithAutoCompleteComponent,
        inputs: ['searchPlaceholder', 'searchBarClass', 'autoCompleteValues'],
        outputs: ['searchChanged', 'searchButtonClicked']
    }) as angular.IDirectiveFactory);


ng1appModule.config([
    '$stateProvider',
    '$translateProvider',
    '$urlRouterProvider',
    '$httpProvider',
    'tooltipsConfigProvider',
    'NotificationProvider',
    ($stateProvider:any,
     $translateProvider:any,
     $urlRouterProvider:ng.ui.IUrlRouterProvider,
     $httpProvider:ng.IHttpProvider,
     tooltipsConfigProvider:any,
     NotificationProvider:any):void => {

        NotificationProvider.setOptions({
            delay: 5000,
            startTop: 10,
            startRight: 10,
            closeOnClick: true,
            verticalSpacing: 20,
            horizontalSpacing: 20,
            positionX: 'right',
            positionY: 'top'
        });
        NotificationProvider.options.templateUrl = 'notification-custom-template.html';

        $translateProvider.useStaticFilesLoader({
            prefix: pathPrefix + 'assets/languages/',
            langKey: '',
            suffix: '.json?d=' + (new Date()).getTime()
        });
        $translateProvider.useSanitizeValueStrategy('escaped');
        $translateProvider.preferredLanguage('en_US');

        $httpProvider.interceptors.push('Sdc.Services.HeaderInterceptor');
        $httpProvider.interceptors.push('Sdc.Services.HttpErrorInterceptor');
        $urlRouterProvider.otherwise('welcome');

        $stateProvider.state(
            'dashboard', {
                url: '/dashboard?show&folder',
                templateUrl: "./view-models/dashboard/dashboard-view.html",
                controller: viewModelsModuleName + '.DashboardViewModel',
            }
        );

        $stateProvider.state(
            'welcome', {
                url: '/welcome',
                templateUrl: "./view-models/welcome/welcome-view.html",
                controller: viewModelsModuleName + '.WelcomeViewModel'
            }
        );

        let componentsParam:Array<any> = ['$stateParams', 'Sdc.Services.EntityService', 'Sdc.Services.CacheService', ($stateParams:any, EntityService:EntityService, cacheService:CacheService) => {
            if (cacheService.get('breadcrumbsComponents')) {
                return cacheService.get('breadcrumbsComponents');
            } else {
                return EntityService.getCatalog();
            }
        }];

        $stateProvider.state(
            'workspace', {
                url: '/workspace/:id/:type/',
                params: {'importedFile': null, 'componentCsar': null, 'resourceType': null, 'disableButtons': null},
                templateUrl: './view-models/workspace/workspace-view.html',
                controller: viewModelsModuleName + '.WorkspaceViewModel',
                resolve: {
                    injectComponent: ['$stateParams', 'ComponentFactory', 'ComponentServiceNg2', function ($stateParams, ComponentFactory:ComponentFactory, ComponentServiceNg2:ComponentServiceNg2) {
                        if ($stateParams.id) {
                            return ComponentFactory.getComponentWithMetadataFromServer($stateParams.type.toUpperCase(), $stateParams.id).then(
                                (component:Component)=> {
                                if ($stateParams.componentCsar){
                                    component = ComponentFactory.updateComponentFromCsar($stateParams.componentCsar, <Resource>component);
                                }
                                return component;
                            });
                        } else if ($stateParams.componentCsar && $stateParams.componentCsar.csarUUID) {
                            return $stateParams.componentCsar;
                        } else {
                            let emptyComponent = ComponentFactory.createEmptyComponent($stateParams.type.toUpperCase());
                            if (emptyComponent.isResource() && $stateParams.resourceType) {
                                // Set the resource type
                                (<Resource>emptyComponent).resourceType = $stateParams.resourceType;
                            }
                            if ($stateParams.importedFile) {
                                (<Resource>emptyComponent).importedFile = $stateParams.importedFile;
                            }
                            return emptyComponent;
                        }
                    }],
                    components: componentsParam
                }
            }
        );

        $stateProvider.state(
            States.WORKSPACE_GENERAL, {
                url: 'general',
                parent: 'workspace',
                controller: viewModelsModuleName + '.GeneralViewModel',
                templateUrl: './view-models/workspace/tabs/general/general-view.html',
                data: {unsavedChanges: false, bodyClass: 'general'}
            }
        );

        $stateProvider.state(
            States.WORKSPACE_ACTIVITY_LOG, {
                url: 'activity_log',
                parent: 'workspace',
                controller: viewModelsModuleName + '.ActivityLogViewModel',
                templateUrl: './view-models/workspace/tabs/activity-log/activity-log.html',
                data: {unsavedChanges: false}
            }
        );

        $stateProvider.state(
            States.WORKSPACE_DEPLOYMENT_ARTIFACTS, {
                url: 'deployment_artifacts',
                parent: 'workspace',
                controller: viewModelsModuleName + '.DeploymentArtifactsViewModel',
                templateUrl: './view-models/workspace/tabs/deployment-artifacts/deployment-artifacts-view.html',
                data: {
                    bodyClass: 'deployment_artifacts'
                }
            }
        );

        $stateProvider.state(
            States.WORKSPACE_INFORMATION_ARTIFACTS, {
                url: 'information_artifacts',
                parent: 'workspace',
                controller: viewModelsModuleName + '.InformationArtifactsViewModel',
                templateUrl: './view-models/workspace/tabs/information-artifacts/information-artifacts-view.html',
                data: {
                    bodyClass: 'information_artifacts'
                }
            }
        );

        $stateProvider.state(
            States.WORKSPACE_TOSCA_ARTIFACTS, {
                url: 'tosca_artifacts',
                parent: 'workspace',
                controller: viewModelsModuleName + '.ToscaArtifactsViewModel',
                templateUrl: './view-models/workspace/tabs/tosca-artifacts/tosca-artifacts-view.html',
                data: {
                    bodyClass: 'tosca_artifacts'
                }
            }
        );

        $stateProvider.state(
            States.WORKSPACE_PROPERTIES, {
                url: 'properties',
                parent: 'workspace',
                controller: viewModelsModuleName + '.PropertiesViewModel',
                templateUrl: './view-models/workspace/tabs/properties/properties-view.html',
                data: {
                    bodyClass: 'properties'
                }
            }
        );

        $stateProvider.state(
            States.WORKSPACE_SERVICE_INPUTS, {
                url: 'service_inputs',
                parent: 'workspace',
                controller: viewModelsModuleName + '.ServiceInputsViewModel',
                templateUrl: './view-models/workspace/tabs/inputs/service-input/service-inputs-view.html',
                data: {
                    bodyClass: 'workspace-inputs'
                }
            }
        );

        $stateProvider.state(
            States.WORKSPACE_PROPERTIES_ASSIGNMENT, {
                url: 'properties_assignment',
                params: {'component': null},
                template: '<properties-assignment></properties-assignment>',
                parent: 'workspace',
                resolve: {
                    componentData: ['injectComponent', '$stateParams', function (injectComponent:Component, $stateParams) {
                        //injectComponent.componentService = null; // this is for not passing the service so no one will use old api and start using new api
                        $stateParams.component = injectComponent;
                        return injectComponent;
                    }],
                },
                data: {
                    bodyClass: 'properties-assignment'
                }
            }
        );

        $stateProvider.state(
            States.WORKSPACE_RESOURCE_INPUTS, {
                url: 'resource_inputs',
                parent: 'workspace',
                controller: viewModelsModuleName + '.ResourceInputsViewModel',
                templateUrl: './view-models/workspace/tabs/inputs/resource-input/resource-inputs-view.html',
                data: {
                    bodyClass: 'workspace-inputs'
                }
            }
        );

        $stateProvider.state(
            States.WORKSPACE_ATTRIBUTES, {
                url: 'attributes',
                parent: 'workspace',
                controller: viewModelsModuleName + '.AttributesViewModel',
                templateUrl: './view-models/workspace/tabs/attributes/attributes-view.html',
                data: {
                    bodyClass: 'attributes'
                }
            }
        );

        $stateProvider.state(
            States.WORKSPACE_REQUIREMENTS_AND_CAPABILITIES, {
                url: 'req_and_capabilities',
                parent: 'workspace',
                controller: viewModelsModuleName + '.ReqAndCapabilitiesViewModel',
                templateUrl: './view-models/workspace/tabs/req-and-capabilities/req-and-capabilities-view.html',
                data: {
                    bodyClass: 'attributes'
                }
            }
        );


        $stateProvider.state(
            States.WORKSPACE_MANAGEMENT_WORKFLOW, {
                parent: 'workspace',
                url: 'management_workflow',
                templateUrl: './view-models/workspace/tabs/management-workflow/management-workflow-view.html',
                controller: viewModelsModuleName + '.ManagementWorkflowViewModel'
            }
        );

        $stateProvider.state(
            States.WORKSPACE_NETWORK_CALL_FLOW, {
                parent: 'workspace',
                url: 'network_call_flow',
                templateUrl: './view-models/workspace/tabs/network-call-flow/network-call-flow-view.html',
                controller: viewModelsModuleName + '.NetworkCallFlowViewModel'
            }
        );

        $stateProvider.state(
            States.WORKSPACE_DISTRIBUTION, {
                parent: 'workspace',
                url: 'distribution',
                templateUrl: './view-models/workspace/tabs/distribution/distribution-view.html',
                controller: viewModelsModuleName + '.DistributionViewModel'
            }
        );

        $stateProvider.state(
            States.WORKSPACE_COMPOSITION, {
                url: 'composition/',
                parent: 'workspace',
                controller: viewModelsModuleName + '.CompositionViewModel',
                templateUrl: './view-models/workspace/tabs/composition/composition-view.html',
                data: {
                    bodyClass: 'composition'
                }
            }
        );

        // $stateProvider.state(
        //     States.WORKSPACE_NG2, {
        //         url: 'ng2/',
        //        component: downgradeComponent({component: NG2Example2Component}), //viewModelsModuleName + '.NG2Example',
        //        templateUrl: './ng2/view-ng2/ng2.example2/ng2.example2.component.html'
        //     }
        // );

        $stateProvider.state(
            States.WORKSPACE_DEPLOYMENT, {
                url: 'deployment/',
                parent: 'workspace',
                templateUrl: './view-models/workspace/tabs/deployment/deployment-view.html',
                controller: viewModelsModuleName + '.DeploymentViewModel',
                data: {
                    bodyClass: 'composition'
                }
            }
        );

        $stateProvider.state(
            'workspace.composition.details', {
                url: 'details',
                parent: 'workspace.composition',
                templateUrl: './view-models/workspace/tabs/composition/tabs/details/details-view.html',
                controller: viewModelsModuleName + '.DetailsViewModel'
            }
        );

        $stateProvider.state(
            'workspace.composition.properties', {
                url: 'properties',
                parent: 'workspace.composition',
                templateUrl: './view-models/workspace/tabs/composition/tabs/properties-and-attributes/properties-view.html',
                controller: viewModelsModuleName + '.ResourcePropertiesViewModel'
            }
        );

        $stateProvider.state(
            'workspace.composition.artifacts', {
                url: 'artifacts',
                parent: 'workspace.composition',
                templateUrl: './view-models/workspace/tabs/composition/tabs/artifacts/artifacts-view.html',
                controller: viewModelsModuleName + '.ResourceArtifactsViewModel'
            }
        );

        $stateProvider.state(
            'workspace.composition.relations', {
                url: 'relations',
                parent: 'workspace.composition',
                templateUrl: './view-models/workspace/tabs/composition/tabs/relations/relations-view.html',
                controller: viewModelsModuleName + '.RelationsViewModel'
            }
        );

        $stateProvider.state(
            'workspace.composition.structure', {
                url: 'structure',
                parent: 'workspace.composition',
                templateUrl: './view-models/workspace/tabs/composition/tabs/structure/structure-view.html',
                controller: viewModelsModuleName + '.StructureViewModel'
            }
        );
        $stateProvider.state(
            'workspace.composition.lifecycle', {
                url: 'lifecycle',
                parent: 'workspace.composition',
                templateUrl: './view-models/workspace/tabs/composition/tabs/artifacts/artifacts-view.html',
                controller: viewModelsModuleName + '.ResourceArtifactsViewModel'
            }
        );

        $stateProvider.state(
            'workspace.composition.api', {
                url: 'api',
                parent: 'workspace.composition',
                templateUrl: './view-models/workspace/tabs/composition/tabs/artifacts/artifacts-view.html',
                controller: viewModelsModuleName + '.ResourceArtifactsViewModel'
            }
        );
        $stateProvider.state(
            'workspace.composition.deployment', {
                url: 'deployment',
                parent: 'workspace.composition',
                templateUrl: './view-models/workspace/tabs/composition/tabs/artifacts/artifacts-view.html',
                controller: viewModelsModuleName + '.ResourceArtifactsViewModel'
            }
        );

        $stateProvider.state(
            'adminDashboard', {
                url: '/adminDashboard',
                templateUrl: './view-models/admin-dashboard/admin-dashboard-view.html',
                controller: viewModelsModuleName + '.AdminDashboardViewModel',
                permissions: ['ADMIN']
            }
        );

        $stateProvider.state(
            'onboardVendor', {
                url: '/onboardVendor',
                templateUrl: './view-models/onboard-vendor/onboard-vendor-view.html',
                controller: viewModelsModuleName + '.OnboardVendorViewModel'//,
            }
        );

        // Build the states for all hosted apps dynamically
        _.each(hostedApplications, (hostedApp)=> {
            if (hostedApp.exists) {
                $stateProvider.state(
                    hostedApp.state.name, {
                        url: hostedApp.state.url,
                        templateUrl: './view-models/dcae-app/dcae-app-view.html',
                        controller: viewModelsModuleName + hostedApp.state.controllerName
                    }
                );
            }
        });

        $stateProvider.state(
            'catalog', {
                url: '/catalog',
                templateUrl: './view-models/catalog/catalog-view.html',
                controller: viewModelsModuleName + '.CatalogViewModel',
                resolve: {
                    auth: ["$q", "UserServiceNg2", ($q:any, userService:UserService) => {
                        let userInfo:IUserProperties = userService.getLoggedinUser();
                        if (userInfo) {
                            return $q.when(userInfo);
                        } else {
                            return $q.reject({authenticated: false});
                        }
                    }]
                }
            }
        );

        $stateProvider.state(
            'support', {
                url: '/support',
                templateUrl: './view-models/support/support-view.html',
                controller: viewModelsModuleName + '.SupportViewModel'
            }
        );

        $stateProvider.state(
            'error-403', {
                url: '/error-403',
                templateUrl: "./view-models/modals/error-modal/error-403-view.html",
                controller: viewModelsModuleName + '.ErrorViewModel'
            }
        );

        tooltipsConfigProvider.options({
            side: 'bottom',
            delay: '600',
            class: 'tooltip-custom',
            lazy: 0,
            try: 0
        });

    }
]);

ng1appModule.value('ValidationPattern', /^[\s\w\&_.:-]{1,1024}$/);
ng1appModule.value('ComponentNameValidationPattern', /^(?=.*[^. ])[\s\w\&_.:-]{1,1024}$/); //DE250513 - same as ValidationPattern above, plus requirement that name not consist of dots and/or spaces alone.
ng1appModule.value('PropertyNameValidationPattern', /^[a-zA-Z0-9_:-]{1,50}$/);// DE210977
ng1appModule.value('TagValidationPattern', /^[\s\w_.-]{1,50}$/);
ng1appModule.value('VendorReleaseValidationPattern', /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,25}$/);
ng1appModule.value('VendorNameValidationPattern', /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,60}$/);
ng1appModule.value('VendorModelNumberValidationPattern', /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,65}$/);
ng1appModule.value('ServiceTypeAndRoleValidationPattern', /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,256}$/);
ng1appModule.value('ContactIdValidationPattern', /^[\s\w-]{1,50}$/);
ng1appModule.value('UserIdValidationPattern', /^[\s\w-]{1,50}$/);
ng1appModule.value('ProjectCodeValidationPattern', /^[\s\w-]{5,50}$/);
ng1appModule.value('LabelValidationPattern', /^[\sa-zA-Z0-9+-]{1,25}$/);
ng1appModule.value('UrlValidationPattern', /^(https?|ftp):\/\/(((([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([A-Za-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([A-Za-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([A-Za-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([A-Za-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([A-Za-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/);
ng1appModule.value('IntegerValidationPattern', /^(([-+]?\d+)|([-+]?0x[0-9a-fA-F]+))$/);
ng1appModule.value('IntegerNoLeadingZeroValidationPattern', /^(0|[-+]?[1-9][0-9]*|[-+]?0x[0-9a-fA-F]+|[-+]?0o[0-7]+)$/);
ng1appModule.value('FloatValidationPattern', /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?f?$/);
ng1appModule.value('NumberValidationPattern', /^((([-+]?\d+)|([-+]?0x[0-9a-fA-F]+))|([-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?))$/);
ng1appModule.value('KeyValidationPattern', /^[\s\w-]{1,50}$/);
ng1appModule.value('CommentValidationPattern', /^[\u0000-\u00BF]*$/);
ng1appModule.value('BooleanValidationPattern', /^([Tt][Rr][Uu][Ee]|[Ff][Aa][Ll][Ss][Ee])$/);
ng1appModule.value('MapKeyValidationPattern', /^[\w]{1,50}$/);

ng1appModule.constant('sdcConfig', sdcConfig);
ng1appModule.constant('sdcMenu', sdcMenu);

ng1appModule.run([
    '$http',
    'Sdc.Services.CacheService',
    'Sdc.Services.CookieService',
    'Sdc.Services.ConfigurationUiService',
    'UserServiceNg2',
    'Sdc.Services.CategoryResourceService',
    'Sdc.Services.SdcVersionService',
    '$state',
    '$rootScope',
    '$location',
    'sdcMenu',
    'ModalsHandler',
    'Sdc.Services.EcompHeaderService',
    'LeftPaletteLoaderService',
    'Sdc.Services.DataTypesService',
    'AngularJSBridge',
    '$templateCache',
    ($http:ng.IHttpService,
     cacheService:CacheService,
     cookieService:CookieService,
     ConfigurationUi:ConfigurationUiService,
     userService:UserService,
     categoryResourceService:ICategoryResourceClass,
     sdcVersionService:SdcVersionService,
     $state:ng.ui.IStateService,
     $rootScope:ng.IRootScopeService,
     $location:ng.ILocationService,
     sdcMenu:IAppMenu,
     ModalsHandler:ModalsHandler,
     ecompHeaderService:EcompHeaderService,
     LeftPaletteLoaderService:LeftPaletteLoaderService,
     DataTypesService:DataTypesService,
     AngularJSBridge,
     $templateCache:ng.ITemplateCacheService):void => {
        $templateCache.put('notification-custom-template.html', require('./view-models/shared/notification-custom-template.html'));
        $templateCache.put('notification-custom-template.html', require('./view-models/shared/notification-custom-template.html'));
        //handle cache data - version
        let initAsdcVersion:Function = ():void => {

            let onFailed = (response) => {
                console.info('onFailed initAsdcVersion', response);
                cacheService.set('version', 'N/A');
            };

            let onSuccess = (version:any) => {
                let tmpVerArray = version.version.split(".");
                let ver = tmpVerArray[0] + "." + tmpVerArray[1] + "." + tmpVerArray[2];
                cacheService.set('version', ver);
            };

            sdcVersionService.getVersion().then(onSuccess, onFailed);

        };

        let initEcompMenu:Function = (user):void => {
            ecompHeaderService.getMenuItems(user.userId).then((data)=> {
                $rootScope['menuItems'] = data;
            });
        };

        let initConfigurationUi:Function = ():void => {
            ConfigurationUi
                .getConfigurationUi()
                .then((configurationUi:any) => {
                    cacheService.set('UIConfiguration', configurationUi);
                });
        };

        let initCategories:Function = ():void => {
            let onError = ():void => {
                console.log('Failed to init categories');
            };

            categoryResourceService.getAllCategories((categories: Categories):void => {
                cacheService.set('serviceCategories', categories.serviceCategories);
                cacheService.set('resourceCategories', categories.resourceCategories);
            }, onError);
        };

        // Add hosted applications to sdcConfig
        sdcConfig.hostedApplications = hostedApplications;

        //handle http config
        $http.defaults.withCredentials = true;
        $http.defaults.headers.common.Authorization = 'Basic YmVlcDpib29w';
        $http.defaults.headers.common[cookieService.getUserIdSuffix()] = cookieService.getUserId();

        initAsdcVersion();
        initConfigurationUi();
       // initLeftPalette();
        DataTypesService.initDataTypes();

        //handle stateChangeStart
        let internalDeregisterStateChangeStartWatcher:Function = ():void => {
            if (deregisterStateChangeStartWatcher) {
                deregisterStateChangeStartWatcher();
                deregisterStateChangeStartWatcher = null;
            }
        };

        let removeLoader:Function = ():void => {
            $(".sdc-loading-page .main-loader").addClass("animated fadeOut");
            $(".sdc-loading-page .caption1").addClass("animated fadeOut");
            $(".sdc-loading-page .caption2").addClass("animated fadeOut");
            window.setTimeout(():void=> {
                $(".sdc-loading-page .main-loader").css("display", "none");
                $(".sdc-loading-page .caption1").css("display", "none");
                $(".sdc-loading-page .caption2").css("display", "none");
                $(".sdc-loading-page").addClass("animated fadeOut");
            }, 1000);
        };

        let onNavigateOut:Function = (toState, toParams):void => {
            let onOk = ():void => {
                $state.current.data.unsavedChanges = false;
                $state.go(toState.name, toParams);
            };

            let data = sdcMenu.alertMessages.exitWithoutSaving;
            //open notify to user if changes are not saved
            ModalsHandler.openAlertModal(data.title, data.message).then(onOk);
        };

        let onStateChangeStart:Function = (event, toState, toParams, fromState, fromParams):void => {
            console.info((new Date()).getTime());
            console.info('$stateChangeStart', toState.name);
            //set body class
            $rootScope['bodyClass'] = 'default-class';
            if (toState.data && toState.data.bodyClass) {
                $rootScope['bodyClass'] = toState.data.bodyClass;
            }

            // Workaround in case we are entering other state then workspace (user move to catalog)
            // remove the changeComponentCsarVersion, user should open again the VSP list and select one for update.
            if (toState.name.indexOf('workspace') === -1) {
                if (cacheService.contains(CHANGE_COMPONENT_CSAR_VERSION_FLAG)) {
                    cacheService.remove(CHANGE_COMPONENT_CSAR_VERSION_FLAG);
                }
            }

            //saving last state to params , for breadcrumbs
            if (['dashboard', 'catalog', 'onboardVendor'].indexOf(fromState.name) > -1) {
                toParams.previousState = fromState.name;
            } else {
                toParams.previousState = fromParams.previousState;
            }

            if (toState.name !== 'error-403' && !userService.getLoggedinUser()) {
                internalDeregisterStateChangeStartWatcher();
                event.preventDefault();

                userService.authorize().subscribe((userInfo:IUserProperties) => {
                    if (!doesUserHasAccess(toState, userInfo)) {
                        $state.go('error-403');
                        console.info('User has no permissions');
                        registerStateChangeStartWatcher();
                        return;
                    }
                    userService.setLoggedinUser(userInfo);
                    cacheService.set('user', userInfo);
                    initCategories();
                    //   initEcompMenu(userInfo);
                    setTimeout(function () {

                        removeLoader();

                        // initCategories();
                        if (userService.getLoggedinUser().role === 'ADMIN') {
                            // toState.name = "adminDashboard";
                            $state.go("adminDashboard", toParams);
                            registerStateChangeStartWatcher();
                            return;
                        }

                        // After user authorized init categories
                        window.setTimeout(():void=> {
                            if ($state.current.name === '') {
                                $state.go(toState.name, toParams);
                            }

                            console.log("------$state.current.name=" + $state.current.name);
                            console.info('-----registerStateChangeStartWatcher authorize $stateChangeStart');
                            registerStateChangeStartWatcher();

                        }, 1000);

                    }, 0);

                }, () => {
                    $state.go('error-403');

                    console.info('registerStateChangeStartWatcher error-403 $stateChangeStart');
                    registerStateChangeStartWatcher();
                });
            }
            else if (userService.getLoggedinUser()) {
                internalDeregisterStateChangeStartWatcher();
                if (!doesUserHasAccess(toState, userService.getLoggedinUser())) {
                    event.preventDefault();
                    $state.go('error-403');
                    console.info('User has no permissions');
                }
                if (toState.name === "welcome") {
                    $state.go("dashboard");
                }
                registerStateChangeStartWatcher();
                //if form is dirty and not save  - notify to user
                if (fromState.data && fromState.data.unsavedChanges && fromParams.id != toParams.id) {
                    event.preventDefault();
                    onNavigateOut(toState, toParams);
                }
            }

        };

        let doesUserHasAccess:Function = (toState, user):boolean => {

            let isUserHasAccess = true;
            if (toState.permissions && toState.permissions.length > 0) {
                isUserHasAccess = _.includes(toState.permissions, user.role);
            }
            return isUserHasAccess;
        };
        let deregisterStateChangeStartWatcher:Function;

        let registerStateChangeStartWatcher:Function = ():void => {
            internalDeregisterStateChangeStartWatcher();
            console.info('registerStateChangeStartWatcher $stateChangeStart');
            deregisterStateChangeStartWatcher = $rootScope.$on('$stateChangeStart', (event, toState, toParams, fromState, fromParams):void => {
                onStateChangeStart(event, toState, toParams, fromState, fromParams);
            });
        };

        registerStateChangeStartWatcher();
    }]);

