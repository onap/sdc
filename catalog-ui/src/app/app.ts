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

import * as _ from "lodash";
import "reflect-metadata";
import 'ng-infinite-scroll';
import './modules/filters.ts';
import './modules/utils.ts';
import './modules/directive-module.ts';
import './modules/service-module';
import './modules/view-model-module.ts';
import {SdcUiCommon, SdcUiComponents, SdcUiServices} from 'onap-ui-angular';
import {CookieService, DataTypesService, EcompHeaderService, LeftPaletteLoaderService} from "./services";
import {CacheService, CatalogService, HomeService} from "./services-ng2";
import {AuthenticationService} from "app/ng2/services/authentication.service";
import {CHANGE_COMPONENT_CSAR_VERSION_FLAG, PREVIOUS_CSAR_COMPONENT, States} from "./utils";
import {IAppConfigurtaion, IAppMenu, IHostedApplication, Resource} from "./models";
import {ComponentFactory} from "./utils/component-factory";
import {Component} from "./models/components/component";
import {IUserProperties} from "./models/user";
import {WorkspaceService} from "./ng2/pages/workspace/workspace.service";

let moduleName: string = 'sdcApp';
let viewModelsModuleName: string = 'Sdc.ViewModels';
let directivesModuleName: string = 'Sdc.Directives';
let servicesModuleName: string = 'Sdc.Services';
let filtersModuleName: string = 'Sdc.Filters';
let utilsModuleName: string = 'Sdc.Utils';

// Load configuration according to environment.
declare var __ENV__: string;
let sdcConfig: IAppConfigurtaion;
let sdcMenu: IAppMenu;
let pathPrefix: string = '';
if (__ENV__ === 'dev') {
  sdcConfig = require('./../../configurations/dev.js');
} else if (__ENV__ === 'prod') {
  sdcConfig = require('./../../configurations/prod.js');
  pathPrefix = 'sdc1/';
} else {
  console.log("ERROR: Environment configuration not found!");
}
sdcMenu = require('./../../configurations/menu.js');

let dependentModules: Array<string> = [
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
let hostedApplications: Array<IHostedApplication> = [
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
let isModuleExists = (moduleName: string): boolean => {
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
_.each(hostedApplications, (hostedApp) => {
  if (isModuleExists(hostedApp.moduleName)) {
    hostedApp['exists'] = true;
  }
});
// ===================== Hosted applications section ====================

export const ng1appModule: ng.IModule = angular.module(moduleName, dependentModules);

ng1appModule.config([
  '$stateProvider',
  '$translateProvider',
  '$urlRouterProvider',
  '$httpProvider',
  'tooltipsConfigProvider',
  'NotificationProvider',
  ($stateProvider: any,
   $translateProvider: any,
   $urlRouterProvider: ng.ui.IUrlRouterProvider,
   $httpProvider: ng.IHttpProvider,
   tooltipsConfigProvider: any,
   NotificationProvider: any): void => {

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
    $urlRouterProvider.otherwise('dashboard');

    $stateProvider.state(
        'dashboard', {
          url: '/dashboard?show&folder&filter.term&filter.status&filter.distributed',
          template: '<home-page></home-page>',
          permissions: ['DESIGNER']
        },
    );


    let componentsParam: Array<any> = ['$stateParams', 'HomeService', 'CatalogService', 'Sdc.Services.CacheService', ($stateParams: any, HomeService: HomeService, CatalogService: CatalogService, cacheService: CacheService) => {
      if (cacheService.get('breadcrumbsComponentsState') === $stateParams.previousState) {
        const breadcrumbsComponents = cacheService.get('breadcrumbsComponents');
        if (breadcrumbsComponents) {
          return breadcrumbsComponents;
        }
      } else {
        let breadcrumbsComponentsObservable;
        if ($stateParams.previousState === 'dashboard') {
          breadcrumbsComponentsObservable = HomeService.getAllComponents(true);
        } else if ($stateParams.previousState === 'catalog') {
          breadcrumbsComponentsObservable = CatalogService.getCatalog();
        } else {
          cacheService.remove('breadcrumbsComponentsState');
          cacheService.remove('breadcrumbsComponents');
          return [];
        }
        breadcrumbsComponentsObservable.subscribe((components) => {
          cacheService.set('breadcrumbsComponentsState', $stateParams.previousState);
          cacheService.set('breadcrumbsComponents', components);
        });
        return breadcrumbsComponentsObservable;
      }
    }];

    const oldWorkspaceController: Array<any> = ['$location', ($location: ng.ILocationService) => {
      // redirect old /workspace/* urls to /catalog/workspace/* url
      const newUrl = '/catalog' + $location.url();
      console.log('old workspace path - redirecting to:', newUrl);
      $location.url(newUrl);
    }];

    $stateProvider.state(
        'workspace-old', {
          url: '/workspace/:id/:type/*workspaceInnerPath',
          controller: oldWorkspaceController
        }
    );

    $stateProvider.state(
        States.TYPE_WORKSPACE, {
          url: '/:previousState/type-workspace/:type/:id/:subPage',
          template: '<app-type-workspace></app-type-workspace>',
      }
    );

    $stateProvider.state(
        States.WORKSPACE, {
          url: '/:previousState/workspace/:id/:type/',
          params: {
            'importedFile': null,
            'componentCsar': null,
            'resourceType': null,
            'disableButtons': null
          },
          templateUrl: './view-models/workspace/workspace-view.html',
          controller: viewModelsModuleName + '.WorkspaceViewModel',
          resolve: {
            injectComponent: ['$stateParams', 'ComponentFactory', 'workspaceService', 'Sdc.Services.CacheService', function ($stateParams, ComponentFactory: ComponentFactory, workspaceService: WorkspaceService, cacheService: CacheService) {
              if ($stateParams.id && $stateParams.id.length) { //need to check length in case ID is an empty string
                return ComponentFactory.getComponentWithMetadataFromServer($stateParams.type.toUpperCase(), $stateParams.id).then(
                    (component: Component) => {
                      if ($stateParams.componentCsar && component.isResource()) {
                        if ((<Resource>component).csarVersion != $stateParams.componentCsar.csarVersion) {
                          cacheService.set(PREVIOUS_CSAR_COMPONENT, angular.copy(component));
                        }
                        component = ComponentFactory.updateComponentFromCsar($stateParams.componentCsar, <Resource>component);
                      }
                      workspaceService.setComponentMetadata(component.componentMetadata);
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
        States.WORKSPACE_INFORMATION_ARTIFACTS, {
          url: 'information_artifacts',
          parent: 'workspace',
          template: '<information-artifact-page></information-artifact-page>'
        }
    );

    $stateProvider.state(
        States.WORKSPACE_TOSCA_ARTIFACTS, {
          url: 'tosca_artifacts',
          parent: 'workspace',
          template: '<tosca-artifact-page></tosca-artifact-page>'
        }
    );


    $stateProvider.state(
        States.WORKSPACE_DEPLOYMENT_ARTIFACTS, {
          url: 'deployment_artifacts',
          parent: 'workspace',
          template: '<deployment-artifact-page></deployment-artifact-page>'
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
        States.WORKSPACE_PROPERTIES_ASSIGNMENT, {
          url: 'properties_assignment',
          params: {'component': null},
          template: '<properties-assignment></properties-assignment>',
          parent: 'workspace',
          resolve: {
            componentData: ['injectComponent', '$stateParams', function (injectComponent: Component, $stateParams) {
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
        States.WORKSPACE_ATTRIBUTES, {
          url: 'attributes',
          parent: 'workspace',
          template: '<attributes></attributes>',
        }
    );

    $stateProvider.state(
        States.WORKSPACE_ATTRIBUTES_OUTPUTS, {
          url: 'attributes_outputs',
          parent: 'workspace',
          template: '<attributes-outputs></attributes-outputs>',
          resolve: {
            componentData: ['injectComponent', '$stateParams', function (injectComponent: Component, $stateParams) {
              $stateParams.component = injectComponent;
              return injectComponent;
            }],
          },
          data: {
            bodyClass: 'attributes-outputs'
          }
        }
    );

    $stateProvider.state(
        States.WORKSPACE_REQUIREMENTS_AND_CAPABILITIES, {
          url: 'req_and_capabilities',
          parent: 'workspace',
          template: '<req-and-capabilities></req-and-capabilities>',
          data: {
            bodyClass: 'attributes'
          }
        }
    );
    $stateProvider.state(
        States.WORKSPACE_REQUIREMENTS_AND_CAPABILITIES_EDITABLE, {
          url: 'req_and_capabilities_editable',
          parent: 'workspace',
          template: '<req-and-capabilities></req-and-capabilities>',
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
        States.WORKSPACE_COMPOSITION, {
          url: 'composition/',
          params: {'component': null},
          parent: 'workspace',
          template: '<composition-page></composition-page>',
          resolve: {
            componentData: ['injectComponent', '$stateParams', function (injectComponent: Component, $stateParams) {
              $stateParams.component = injectComponent;
              return injectComponent;
            }],
          },
          data: {
            bodyClass: 'composition'
          }
        }
    );

    $stateProvider.state(
        States.WORKSPACE_ACTIVITY_LOG, {
          url: 'activity_log/',
          parent: 'workspace',
          template: '<activity-log></activity-log>',
        }
    );

    $stateProvider.state(
        States.WORKSPACE_DISTRIBUTION, {
          url: 'distribution',
          parent: 'workspace',
          template: '<distribution></distribution>',
        }
    );

    $stateProvider.state(
        States.WORKSPACE_DEPLOYMENT, {
          url: 'deployment/',
          parent: 'workspace',
          template: '<deployment-page></deployment-page>',

        }
    );

    $stateProvider.state(
        'workspace.composition.details', {
          url: 'details',
          parent: 'workspace.composition',
          resolve: {
            componentData: ['injectComponent', '$stateParams', function (injectComponent: Component, $stateParams) {
              $stateParams.component = injectComponent;
              return injectComponent;
            }],
          }

        }
    );

    $stateProvider.state(
        'workspace.composition.properties', {
          url: 'properties',
          parent: 'workspace.composition'
        }
    );

    $stateProvider.state(
        'workspace.composition.artifacts', {
          url: 'artifacts',
          parent: 'workspace.composition'

        }
    );

    $stateProvider.state(
        'workspace.composition.relations', {
          url: 'relations',
          parent: 'workspace.composition'
        }
    );

    $stateProvider.state(
        'workspace.composition.structure', {
          url: 'structure',
          parent: 'workspace.composition'
        }
    );
    $stateProvider.state(
        'workspace.composition.lifecycle', {
          url: 'lifecycle',
          parent: 'workspace.composition'
        }
    );

    $stateProvider.state(
        'workspace.composition.api', {
          url: 'api',
          parent: 'workspace.composition'
        }
    );
    $stateProvider.state(
        'workspace.composition.deployment', {
          url: 'deployment',
          parent: 'workspace.composition'
        }
    );

    $stateProvider.state(
        States.WORKSPACE_INTERFACE_OPERATION, {
          url: 'interface_operation',
          parent: 'workspace',
          controller: viewModelsModuleName + '.InterfaceOperationViewModel',
          templateUrl: './view-models/workspace/tabs/interface-operation/interface-operation-view.html',
          data: {
            bodyClass: 'interface_operation'
          }
        }
    );

    $stateProvider.state(
        States.WORKSPACE_INTERFACE_DEFINITION, {
          url: 'interfaceDefinition',
          parent: 'workspace',
          controller: viewModelsModuleName + '.InterfaceDefinitionViewModel',
          templateUrl: './view-models/workspace/tabs/interface-definition/interface-definition-view.html',
          data: {
            bodyClass: 'interfaceDefinition'
          }
        }
    );

    $stateProvider.state(
        'workspace.plugins', {
          url: 'plugins/*path',
          parent: 'workspace',
          template: '<plugin-context-view></plugin-context-view>',
          resolve: {
            componentData: ['injectComponent', '$stateParams', function (injectComponent: Component, $stateParams) {
              $stateParams.component = injectComponent;
              return injectComponent;
            }],
          }

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
          controller: viewModelsModuleName + '.OnboardVendorViewModel'
        }
    );

    $stateProvider.state(
        'plugins', {
          url: '/plugins/*path',
          template: '<plugin-tab-view></plugin-tab-view>'
        }
    );

    // Build the states for all hosted apps dynamically
    _.each(hostedApplications, (hostedApp) => {
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
          url: '/catalog?filter.components&filter.resourceSubTypes&filter.categories&filter.statuses&filter.order&filter.term&filter.active',
          template: '<catalog-page></catalog-page>',
          resolve: {
            auth: ["$q", "AuthenticationServiceNg2", ($q: any, authService: AuthenticationService) => {
              let userInfo: IUserProperties = authService.getLoggedinUser();
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
ng1appModule.value('PropertyNameValidationPattern', /^[a-zA-Z0-9_:-@]{1,100}$/);// DE210977
ng1appModule.value('TagValidationPattern', /^[\s\w_.-]{1,50}$/);
ng1appModule.value('VendorReleaseValidationPattern', /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,25}$/);
ng1appModule.value('VendorNameValidationPattern', /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,60}$/);
ng1appModule.value('VendorModelNumberValidationPattern', /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,65}$/);
ng1appModule.value('ServiceTypeAndRoleValidationPattern', /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,256}$/);
ng1appModule.value('ContactIdValidationPattern', /^[\s\w-]{1,50}$/);
ng1appModule.value('UserIdValidationPattern', /^[\s\w-]{1,50}$/);
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
  'AuthenticationServiceNg2',
  '$state',
  '$rootScope',
  '$location',
  'sdcMenu',
  'Sdc.Services.EcompHeaderService',
  'LeftPaletteLoaderService',
  'Sdc.Services.DataTypesService',
  'AngularJSBridge',
  '$templateCache',
  'ModalServiceSdcUI',
  ($http: ng.IHttpService,
   cacheService: CacheService,
   cookieService: CookieService,
   authService: AuthenticationService,
   $state: ng.ui.IStateService,
   $rootScope: ng.IRootScopeService,
   $location: ng.ILocationService,
   sdcMenu: IAppMenu,
   ecompHeaderService: EcompHeaderService,
   LeftPaletteLoaderService: LeftPaletteLoaderService,
   DataTypesService: DataTypesService,
   AngularJSBridge,
   $templateCache: ng.ITemplateCacheService,
   ModalServiceSdcUI: SdcUiServices.ModalService): void => {
    $templateCache.put('notification-custom-template.html', require('./view-models/shared/notification-custom-template.html'));
    $templateCache.put('notification-custom-template.html', require('./view-models/shared/notification-custom-template.html'));

    // Add hosted applications to sdcConfig
    sdcConfig.hostedApplications = hostedApplications;

    //handle http config
    $http.defaults.withCredentials = true;
    $http.defaults.headers.common[cookieService.getUserIdSuffix()] = cookieService.getUserId();

    DataTypesService.loadDataTypesCache(null);

    //handle stateChangeStart
    let internalDeregisterStateChangeStartWatcher: Function = (): void => {
      if (deregisterStateChangeStartWatcher) {
        deregisterStateChangeStartWatcher();
        deregisterStateChangeStartWatcher = null;
      }
      if (deregisterStateChangeSuccessWatcher) {
        deregisterStateChangeSuccessWatcher();
        deregisterStateChangeSuccessWatcher = null;
      }
    };

    let removeLoader: Function = (): void => {
      $(".sdc-loading-page .main-loader").addClass("animated fadeOut");
      $(".sdc-loading-page .caption1").addClass("animated fadeOut");
      $(".sdc-loading-page .caption2").addClass("animated fadeOut");
      window.setTimeout((): void => {
        $(".sdc-loading-page .main-loader").css("display", "none");
        $(".sdc-loading-page .caption1").css("display", "none");
        $(".sdc-loading-page .caption2").css("display", "none");
        $(".sdc-loading-page").addClass("animated fadeOut");
      }, 1000);
    };

    let onNavigateOut: Function = (toState, toParams): void => {
      let onOk: Function = (): void => {
        $state.current.data.unsavedChanges = false;
        $state.go(toState.name, toParams);
      };

      let data = sdcMenu.alertMessages.exitWithoutSaving;
      const okButton = {
        testId: "OK",
        text: sdcMenu.alertMessages.okButton,
        type: SdcUiCommon.ButtonType.warning,
        callback: onOk,
        closeModal: true
      } as SdcUiComponents.ModalButtonComponent;
      //open notify to user if changes are not saved
      ModalServiceSdcUI.openWarningModal(data.title,
          data.message,
          'navigate-modal',
          [okButton]);
    };

    let onStateChangeStart: Function = (event, toState, toParams, fromState, fromParams): void => {
      console.debug((new Date()).getTime());
      console.debug('$stateChangeStart', toState.name);
      if (toState.name !== 'error-403' && !authService.getLoggedinUser()) {


        authService.authenticate().subscribe((userInfo: IUserProperties) => {
          if (!doesUserHasAccess(toState, userInfo)) {
            $state.go('error-403');
            console.debug('User has no permissions');
            return;
          }
          authService.setLoggedinUser(userInfo);
          setTimeout(function () {

            removeLoader();

            if (authService.getLoggedinUser().role === 'ADMIN') {
              $state.go("adminDashboard", toParams);
              return;
            }

            // After user authorized init categories
            window.setTimeout((): void => {
              if ($state.current.name === '') {
                $state.go(toState.name, toParams);
              }

              console.log("------$state.current.name=" + $state.current.name);

            }, 1000);

          }, 0);

        }, () => {
          $state.go('error-403');
        });
      } else if (authService.getLoggedinUser()) {
        let user: IUserProperties = authService.getLoggedinUser();
        if (!cacheService.contains('user')) {
          cacheService.set('user', user);
        }

        if (!doesUserHasAccess(toState, authService.getLoggedinUser())) {
          event.preventDefault();
          $state.go('error-403');
          console.debug('User has no permissions');
        }

        if (authService.getLoggedinUser().role === 'ADMIN') {
          $state.go("adminDashboard", toParams);
          return;
        }


        //if form is dirty and not save  - notify to user
        if (fromState.data && fromState.data.unsavedChanges && fromParams.id != toParams.id) {
          event.preventDefault();
          onNavigateOut(toState, toParams);
        }
      }

      // if enetering workspace, set the previousState param
      if (toState.name.indexOf('workspace') !== -1) {
        if (!toParams.previousState) {
          const tmpPreviousState1 = fromParams && fromParams.previousState;
          const tmpPreviousState2 = (['dashboard', 'catalog'].indexOf(fromState.name) !== -1) ? fromState.name : 'catalog';
          toParams.previousState = tmpPreviousState1 || tmpPreviousState2;
        }
      }

    };

    let onStateChangeSuccess: Function = (event, toState, toParams, fromState, fromParams): void => {
      console.debug('$stateChangeSuccess', toState.name);

      // Workaround in case we are entering other state then workspace (user move to catalog)
      // remove the changeComponentCsarVersion, user should open again the VSP list and select one for update.
      if (toState.name.indexOf('workspace') === -1) {
        if (cacheService.contains(CHANGE_COMPONENT_CSAR_VERSION_FLAG)) {
          cacheService.remove(CHANGE_COMPONENT_CSAR_VERSION_FLAG);
        }
        if (cacheService.contains(PREVIOUS_CSAR_COMPONENT)) {
          cacheService.remove(PREVIOUS_CSAR_COMPONENT);
        }
      }

      //set body class
      $rootScope['bodyClass'] = 'default-class';
      if (toState.data && toState.data.bodyClass) {
        $rootScope['bodyClass'] = toState.data.bodyClass;
      }
    };

    let doesUserHasAccess: Function = (toState, user): boolean => {

      let isUserHasAccess = true;
      if (toState.permissions && toState.permissions.length > 0) {
        isUserHasAccess = _.includes(toState.permissions, user.role);
      }
      return isUserHasAccess;
    };
    let deregisterStateChangeStartWatcher: Function;
    let deregisterStateChangeSuccessWatcher: Function;

    let registerStateChangeStartWatcher: Function = (): void => {
      internalDeregisterStateChangeStartWatcher();
      console.debug('registerStateChangeStartWatcher $stateChangeStart');
      deregisterStateChangeStartWatcher = $rootScope.$on('$stateChangeStart', (event, toState, toParams, fromState, fromParams): void => {
        onStateChangeStart(event, toState, toParams, fromState, fromParams);
      });
      deregisterStateChangeSuccessWatcher = $rootScope.$on('$stateChangeSuccess', (event, toState, toParams, fromState, fromParams): void => {
        onStateChangeSuccess(event, toState, toParams, fromState, fromParams);
      });
    };
    registerStateChangeStartWatcher();
  }]);

