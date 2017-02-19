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
/// <reference path="./references"/>
/*
 SD&C Web Portal Wireframes –  Designer Home Page and Create New Service Flow
 */
//libraries variables to prevent compile errors
declare let jsPDF:any;

module Sdc {
    import User = Sdc.Models.User;
    import UserResourceService = Sdc.Services.UserResourceService;

    'use strict';
    import Resource = Sdc.Models.Components.Resource;
    let moduleName:string = 'sdcApp';
    let viewModelsModuleName:string = 'Sdc.ViewModels';
    let directivesModuleName:string = 'Sdc.Directives';
    let servicesModuleName:string = 'Sdc.Services';
    let filtersModuleName:string = 'Sdc.Filters';
    let utilsModuleName: string =  'Sdc.Utils';
    let dependentModules:Array<string> = [
        'ui.router',
        'ui.bootstrap',
        'ngDragDrop',
        'ui-notification',
        'ngResource',
        'ngSanitize',
        'Sdc.Config',
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

    let appModule:ng.IModule = angular.module(moduleName, dependentModules);

    appModule.config([
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
                delay: 10000,
                startTop: 10,
                startRight: 10,
                closeOnClick: true,
                verticalSpacing: 20,
                horizontalSpacing: 20,
                positionX: 'right',
                positionY: 'top'
            });

            let viewModelsHtmlBasePath:string = '/app/scripts/view-models/';
            console.info('appModule.config: ', viewModelsHtmlBasePath);

            $translateProvider.useStaticFilesLoader({
                prefix: 'languages/',
                langKey: '',
                suffix: '.json?d=' + (new Date()).getTime()
            });
            $translateProvider.useSanitizeValueStrategy('escaped');
            $translateProvider.preferredLanguage('en_US_OS'); // For open source changed to en_US_OS

            $httpProvider.interceptors.push('Sdc.Services.HeaderInterceptor');
            $httpProvider.interceptors.push('Sdc.Services.HttpErrorInterceptor');

            $urlRouterProvider.otherwise('welcome');

            $stateProvider.state(
                'dashboard', {
                    url: '/dashboard?show&folder',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'dashboard/dashboard-view.html');
                    }],
                    controller: viewModelsModuleName + '.DashboardViewModel',

                }
            );

            $stateProvider.state(
                'welcome', {
                    url: '/welcome',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'welcome/welcome-view.html');
                    }],
                    controller: viewModelsModuleName + '.WelcomeViewModel'
                }
            );

            $stateProvider.state(
                'dashboard.cover', {
                    url: '/cover',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'dashboard/cover/dashboard-cover-view.html');
                    }],
                    controller: viewModelsModuleName + '.DashboardCoverViewModel'
                }
            );

            $stateProvider.state(
                'dashboard.tutorial-end', {
                    url: '/tutorial-end',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'tutorial-end/tutorial-end.html');
                    }],
                    controller: viewModelsModuleName + '.TutorialEndViewModel'
                }
            );

            $stateProvider.state(
                'additionalInformation', {
                    url: '/additionalInformation',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'additional-information/additional-information-view.html');
                    }],
                    controller: viewModelsModuleName + '.AdditionalInformationViewModel'
                }
            );

            let componentsParam:Array<any> = ['$stateParams', 'Sdc.Services.EntityService','Sdc.Services.CacheService' , ($stateParams:any, EntityService:Sdc.Services.EntityService, cacheService:Services.CacheService) => {
                if(cacheService.get('breadcrumbsComponents')){
                    return  cacheService.get('breadcrumbsComponents');
                } else {
                    return EntityService.getCatalog(); //getAllComponents() doesnt return components from catalog
                }
            }];


            $stateProvider.state (
                'workspace', {
                    url: '/workspace/:id/:type/',
                    params: {'importedFile':null,'componentCsar':null,'resourceType': null, 'disableButtons': null}, //'vspComponent': null,
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/workspace-view.html');
                    }],
                    controller: viewModelsModuleName + '.WorkspaceViewModel',
                    resolve: {
                        injectComponent: ['$stateParams', 'ComponentFactory' , function ($stateParams, ComponentFactory) {
                            /*
                            if($stateParams.vspComponent){
                                return $stateParams.vspComponent;
                            } else
                            */
                            if($stateParams.id){
                                return ComponentFactory.getComponentFromServer($stateParams.type.toUpperCase(), $stateParams.id);
                            } else if ($stateParams.componentCsar && $stateParams.componentCsar.csarUUID) {
                                return $stateParams.componentCsar;
                            } else {
                                let emptyComponent = ComponentFactory.createEmptyComponent($stateParams.type.toUpperCase());
                                if (emptyComponent.isResource() && $stateParams.resourceType){
                                    // Set the resource type
                                    (<Resource>emptyComponent).resourceType = $stateParams.resourceType;
                                }
                                if($stateParams.importedFile){
                                    (<Models.Components.Resource>emptyComponent).importedFile = $stateParams.importedFile;
                                }
                                return emptyComponent;
                            }
                        }],
                        components: componentsParam
                    }
                }
            );

            $stateProvider.state(
               Utils.Constants.States.WORKSPACE_GENERAL, {
                    url: 'general',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.GeneralViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/general/general-view.html');
                    }],
                    data: {unsavedChanges:false,bodyClass:'general'}
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_ICONS, {
                    url: 'icons',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.IconsViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {

                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/icons/icons-view.html');
                    }],
                    data: {unsavedChanges:false,bodyClass:'icons'}

                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_ACTIVITY_LOG, {
                    url: 'activity_log',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.ActivityLogViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/activity-log/activity-log.html');
                    }],
                    data: {unsavedChanges:false}
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_DEPLOYMENT_ARTIFACTS, {
                    url: 'deployment_artifacts',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.DeploymentArtifactsViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {

                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/deployment-artifacts/deployment-artifacts-view.html');
                    }],
                    data:{
                        bodyClass:'deployment_artifacts'
                    }
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_HIERARCHY, {
                    url: 'hierarchy',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.ProductHierarchyViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {

                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/product-hierarchy/product-hierarchy-view.html');
                    }]

                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_INFORMATION_ARTIFACTS, {
                    url: 'information_artifacts',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.InformationArtifactsViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {

                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/information-artifacts/information-artifacts-view.html');
                    }],
                    data:{
                        bodyClass:'information_artifacts'
                    }
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_TOSCA_ARTIFACTS, {
                    url: 'tosca_artifacts',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.ToscaArtifactsViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {

                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/tosca-artifacts/tosca-artifacts-view.html');
                    }],
                    data:{
                        bodyClass:'tosca_artifacts'
                    }
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_PROPERTIES, {
                    url: 'properties',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.PropertiesViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/properties/properties-view.html');
                    }],
                    data:{
                        bodyClass:'properties'
                    }
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_SERVICE_INPUTS, {
                    url: 'service_inputs',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.ServiceInputsViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/inputs/service-input/service-inputs-view.html');
                    }],
                    data:{
                        bodyClass:'workspace-inputs'
                    }
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_RESOURCE_INPUTS, {
                    url: 'resource_inputs',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.ResourceInputsViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/inputs/resource-input/resource-inputs-view.html');
                    }],
                    data:{
                        bodyClass:'workspace-inputs'
                    }
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_ATTRIBUTES, {
                    url: 'attributes',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.AttributesViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/attributes/attributes-view.html');
                    }],
                    data:{
                        bodyClass:'attributes'
                    }
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_REQUIREMENTS_AND_CAPABILITIES, {
                    url: 'req_and_capabilities',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.ReqAndCapabilitiesViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/req-and-capabilities/req-and-capabilities-view.html');
                    }],
                    data:{
                        bodyClass:'attributes'
                    }
                }
            );


            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_MANAGEMENT_WORKFLOW, {
                    parent: 'workspace',
                    url: 'management_workflow',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/management-workflow/management-workflow-view.html');
                    }],
                    controller: viewModelsModuleName + '.ManagementWorkflowViewModel'
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_NETWORK_CALL_FLOW, {
                    parent: 'workspace',
                    url: 'network_call_flow',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/network-call-flow/network-call-flow-view.html');
                    }],
                    controller: viewModelsModuleName + '.NetworkCallFlowViewModel'
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_DISTRIBUTION, {
                    parent: 'workspace',
                    url: 'distribution',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/distribution/distribution-view.html');
                    }],
                    controller: viewModelsModuleName + '.DistributionViewModel'
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_COMPOSITION, {
                    url: 'composition/',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.CompositionViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {

                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/composition/composition-view.html');
                    }],
                    data:{
                        bodyClass:'composition'
                    }
                }
            );

            $stateProvider.state(
                Utils.Constants.States.WORKSPACE_DEPLOYMENT, {
                    url: 'deployment/',
                    parent: 'workspace',
                    controller: viewModelsModuleName + '.DeploymentViewModel',
                    templateProvider: ['$templateCache', ($templateCache:ng.ITemplateCacheService):string => {

                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/deployment/deployment-view.html');
                    }],
                    data:{
                        bodyClass:'composition'
                    }
                }
            );

            $stateProvider.state(
                'workspace.composition.details', {
                    url: 'details',
                    parent: 'workspace.composition',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/composition/tabs/details/details-view.html');
                    }],
                    controller: viewModelsModuleName + '.DetailsViewModel'
                }
            );

            $stateProvider.state(
                'workspace.composition.properties', {
                    url: 'properties',
                    parent: 'workspace.composition',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/composition/tabs/properties-and-attributes/properties-view.html');
                    }],
                    controller: viewModelsModuleName + '.ResourcePropertiesViewModel'
                }
            );

            $stateProvider.state(
                'workspace.composition.artifacts', {
                    url: 'artifacts',
                    parent: 'workspace.composition',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/composition/tabs/artifacts/artifacts-view.html');
                    }],
                    controller: viewModelsModuleName + '.ResourceArtifactsViewModel'
                }
            );

            $stateProvider.state(
                'workspace.composition.relations', {
                    url: 'relations',
                    parent: 'workspace.composition',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/composition/tabs/relations/relations-view.html');
                    }],
                    controller: viewModelsModuleName + '.RelationsViewModel'
                }
            );

            $stateProvider.state(
                'workspace.composition.relationships', {
                    url: 'relationships',
                    parent: 'workspace.composition',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'resource-relationships/resource-relationships-view.html');
                    }],
                    controller: viewModelsModuleName + '.ResourceRelationshipsViewModel'
                }
            );

            $stateProvider.state(
                'workspace.composition.structure', {
                    url: 'structure',
                    parent: 'workspace.composition',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/composition/tabs/structure/structure-view.html');
                    }],
                    controller: viewModelsModuleName + '.StructureViewModel'
                }
            );
            $stateProvider.state(
                'workspace.composition.lifecycle', {
                    url: 'lifecycle',
                    parent: 'workspace.composition',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/composition/tabs/artifacts/artifacts-view.html');
                    }],
                    controller: viewModelsModuleName + '.ResourceArtifactsViewModel'
                }
            );

            $stateProvider.state(
                'workspace.composition.api', {
                    url: 'api',
                    parent: 'workspace.composition',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/composition/tabs/artifacts/artifacts-view.html');
                    }],
                    controller: viewModelsModuleName + '.ResourceArtifactsViewModel'
                }
            );
            $stateProvider.state(
                'workspace.composition.deployment', {
                    url: 'deployment',
                    parent: 'workspace.composition',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'workspace/tabs/composition/tabs/artifacts/artifacts-view.html');
                    }],
                    controller: viewModelsModuleName + '.ResourceArtifactsViewModel'
                }
            );

            $stateProvider.state(
                'edit-resource', {
                    url: '/edit-resource/:id',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'entity-handler/resource-form/resource-form-view.html');
                    }],
                    controller: viewModelsModuleName + '.ResourceFormViewModel'
                }
            );

             $stateProvider.state(
                'edit-product', {
                    url: '/edit-product/:id',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'entity-handler/product-form/product-form-view.html');
                    }],
                    controller: viewModelsModuleName + '.ProductFormViewModel'
                }
            );

            $stateProvider.state(
                'adminDashboard', {
                    url: '/adminDashboard',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'admin-dashboard/admin-dashboard-view.html');
                    }],
                    controller: viewModelsModuleName + '.AdminDashboardViewModel',
                    permissions: ['ADMIN']
                }
            );

            $stateProvider.state(
                'onboardVendor', {
                    url: '/onboardVendor',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'onboard-vendor/onboard-vendor-view.html');
                    }],
                    controller: viewModelsModuleName + '.OnboardVendorViewModel'//,
                    //resolve: {
                    //    auth: ["$q", "Sdc.Services.UserResourceService", function ($q:any, userResourceService:Sdc.Services.IUserResourceClass) {
                    //        let userInfo:Sdc.Services.IUserResource = userResourceService.getLoggedinUser();
                    //        if (userInfo) {
                    //            return $q.when(userInfo);
                    //        } else {
                    //            return $q.reject({authenticated: false});
                    //        }
                    //    }]
                    //}
                }
            );

            $stateProvider.state(
                'catalog', {
                    url: '/catalog',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'catalog/catalog-view.html');
                    }],
                    controller: viewModelsModuleName + '.CatalogViewModel',
                    resolve: {
                        auth: ["$q", "Sdc.Services.UserResourceService", function ($q:any, userResourceService:Sdc.Services.IUserResourceClass) {
                            let userInfo:Sdc.Services.IUserResource = userResourceService.getLoggedinUser();
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
                'distribution', {
                    url: '/distribution',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'distribution/distribution-view.html');
                    }],
                    controller: viewModelsModuleName + '.DistributionViewModel'
                }
            );

            $stateProvider.state(
                'support', {
                    url: '/support',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'support/support-view.html');
                    }],
                    controller: viewModelsModuleName + '.SupportViewModel'
                }
            );

            $stateProvider.state(
                'error-403', {
                    url: '/error-403',
                    templateProvider: ['$templateCache', ($templateCache):string => {
                        return $templateCache.get(viewModelsHtmlBasePath + 'modals/error-modal/error-403-view.html');
                    }],
                    controller: viewModelsModuleName + '.ErrorViewModel'
                }
            );

            tooltipsConfigProvider.options({

                side:'bottom',
                delay: '600',
                class: 'tooltip-custom',
                lazy:0,
                try:0

            });

        }
    ])
        .run(['AngularJSBridge', (AngularJSBridge)=>{

        }]);
    appModule.value('ValidationPattern', /^[\s\w\&_.:-]{1,1024}$/);
    appModule.value('PropertyNameValidationPattern', /^[a-zA-Z0-9_:-]{1,50}$/);// DE210977
    appModule.value('TagValidationPattern', /^[\s\w_.-]{1,50}$/);
   // appModule.value('VendorValidationPattern', /^[^?\\<>:"/|*]{1,25}$/);
    appModule.value('VendorValidationPattern', /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,25}$/);
    appModule.value('ContactIdValidationPattern', /^[\s\w-]{1,50}$/);
    appModule.value('UserIdValidationPattern',/^[\s\w-]{1,50}$/);
    appModule.value('ProjectCodeValidationPattern', /^[\s\w-]{1,50}$/);
    appModule.value('LabelValidationPattern', /^[\sa-zA-Z0-9+-]{1,25}$/);
    appModule.value('UrlValidationPattern', /^(https?|ftp):\/\/(((([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([A-Za-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([A-Za-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([A-Za-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([A-Za-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([A-Za-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([A-Za-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/);
    appModule.value('IntegerValidationPattern', /^(([-+]?\d+)|([-+]?0x[0-9a-fA-F]+))$/);
    appModule.value('IntegerNoLeadingZeroValidationPattern', /^(0|[-+]?[1-9][0-9]*|[-+]?0x[0-9a-fA-F]+|[-+]?0o[0-7]+)$/);
    appModule.value('FloatValidationPattern', /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?f?$/);
    appModule.value('NumberValidationPattern', /^((([-+]?\d+)|([-+]?0x[0-9a-fA-F]+))|([-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?))$/);
    appModule.value('KeyValidationPattern', /^[\s\w-]{1,50}$/);
    appModule.value('CommentValidationPattern', /^[\u0000-\u00BF]*$/);
    appModule.value('BooleanValidationPattern', /^([Tt][Rr][Uu][Ee]|[Ff][Aa][Ll][Ss][Ee])$/);


    appModule.run([
        '$http',
        'Sdc.Services.CacheService',
        'Sdc.Services.CookieService',
        'Sdc.Services.ConfigurationUiService',
        'Sdc.Services.UserResourceService',
        'Sdc.Services.CategoryResourceService',
        'Sdc.Services.SdcVersionService',
        '$state',
        '$rootScope',
        '$location',
        'sdcConfig',
        'sdcMenu',
        'ModalsHandler',
	    'Sdc.Services.EcompHeaderService',
        'LeftPaletteLoaderService',
        ($http:ng.IHttpService,
         cacheService:Services.CacheService,
         cookieService:Services.CookieService,
         ConfigurationUi:Services.ConfigurationUiService,
         UserResourceClass:Services.IUserResourceClass,
         categoryResourceService:Sdc.Services.ICategoryResourceClass,
         sdcVersionService:Services.SdcVersionService,
         $state:ng.ui.IStateService,
         $rootScope:ng.IRootScopeService,
         $location: ng.ILocationService,
         sdcConfig: Models.IAppConfigurtaion,
         sdcMenu: Models.IAppMenu,
         ModalsHandler:Utils.ModalsHandler,
         ecompHeaderService:Sdc.Services.EcompHeaderService,
         LeftPaletteLoaderService:Services.Components.LeftPaletteLoaderService
         ):void => {

            //handle cache data - version
            let initSdcVersion:Function = ():void => {

                let onFailed = (response) => {
                    console.info('onFailed initSdcVersion', response);
                    cacheService.set('version', 'N/A');
                };

                let onSuccess = (version:any) => {
                    console.log("Version returned from server: " + version);
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

                categoryResourceService.getAllCategories({types: 'services'}, (categories:Array<Models.IMainCategory>):void => {
                    cacheService.set('serviceCategories', categories);
                }, onError);

                categoryResourceService.getAllCategories({types: 'resources'}, (categories:Array<Models.IMainCategory>):void => {
                    cacheService.set('resourceCategories', categories);
                }, onError);

                categoryResourceService.getAllCategories({types: 'products'}, (categories:Array<Models.IMainCategory>):void => {
                    cacheService.set('productCategories', categories);
                }, onError);
            };

            let initBaseUrl:Function = ():void => {
                let env:string = sdcConfig.environment;
                let baseUrl:string = $location.absUrl();
                console.log("baseUrl="+baseUrl);

                if(baseUrl) {
                    sdcConfig.api.baseUrl = baseUrl;

                    if(env==='prod'){
                        //let tempUrl = $location.absUrl().split('/sdc1/');
                        var mainUrl = location.protocol+'//'+location.hostname+(location.port ? ':'+location.port: '');
                        console.log("mainUrl="+mainUrl);
                        sdcConfig.api.root = mainUrl +  sdcConfig.api.root;
                        console.log("sdcConfig.api.root="+sdcConfig.api.root);
                    }
                }
            };

            let initLeftPalette:Function = ():void => {
                LeftPaletteLoaderService.loadLeftPanel();
            };

            //handle http config
            $http.defaults.withCredentials = true;
            $http.defaults.headers.common[cookieService.getUserIdSuffix()] = cookieService.getUserId();

            initBaseUrl();
            initSdcVersion();
            initConfigurationUi();
            Utils.Constants.IMAGE_PATH = sdcConfig.imagesPath;
            initLeftPalette();

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
                window.setTimeout(():void=>{
                    $(".sdc-loading-page .main-loader").css("display", "none");
                    $(".sdc-loading-page .caption1").css("display", "none");
                    $(".sdc-loading-page .caption2").css("display", "none");
                    $(".sdc-loading-page").addClass("animated fadeOut");
                },1000);
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
                if(toState.data && toState.data.bodyClass){
                    $rootScope['bodyClass'] = toState.data.bodyClass;
                }

                // Workaround in case we are entering other state then workspace (user move to catalog)
                // remove the changeComponentCsarVersion, user should open again the VSP list and select one for update.
                if (toState.name.indexOf('workspace') === -1) {
                    if (cacheService.contains(Utils.Constants.CHANGE_COMPONENT_CSAR_VERSION_FLAG)){
                        cacheService.remove(Utils.Constants.CHANGE_COMPONENT_CSAR_VERSION_FLAG);
                    }
                }

                //saving last state to params , for breadcrumbs
                if (['dashboard', 'catalog', 'onboardVendor'].indexOf(fromState.name) > -1) {
                    toParams.previousState = fromState.name;
                } else {
                    toParams.previousState = fromParams.previousState;
                }

                if (toState.name !== 'error-403' && !UserResourceClass.getLoggedinUser()) {
                    internalDeregisterStateChangeStartWatcher();
                    event.preventDefault();

                    UserResourceClass.authorize().$promise.then((user:Services.IUserResource) => {
                        if(!doesUserHasAccess(toState, user)){
                                $state.go('error-403');
                                console.info('User has no permissions');
                                registerStateChangeStartWatcher();
                                return;
                        }
                        UserResourceClass.setLoggedinUser(user);
                        cacheService.set('user', user);
                        initCategories();
                     //   initEcompMenu(user);
                        setTimeout(function () {

                            removeLoader();

                            // initCategories();
                            if(UserResourceClass.getLoggedinUser().role === 'ADMIN'){
                               // toState.name = "adminDashboard";
                                $state.go("adminDashboard", toParams);
                                registerStateChangeStartWatcher();
                                return;
                            }

                            // After user authorized init categories
                            window.setTimeout(():void=>{
                                //if ($state.current.name==='' || $state.current.name==='preloading') {
                                if ($state.current.name === "welcome" && sdcConfig.openSource) {
                                    event.preventDefault();
                                    $state.go("dashboard");
                                    registerStateChangeStartWatcher();
                                }
                                else if ($state.current.name==='') {
                                    $state.go(toState.name, toParams);
                                }

                                console.log("------$state.current.name=" + $state.current.name);
                                console.info('-----registerStateChangeStartWatcher authorize $stateChangeStart');
                                registerStateChangeStartWatcher();

                            },1000);

                        }, 0);

                    }, () => {
                        $state.go('error-403');

                        console.info('registerStateChangeStartWatcher error-403 $stateChangeStart');
                        registerStateChangeStartWatcher();
                    });
                }
                else if(UserResourceClass.getLoggedinUser()){
                    internalDeregisterStateChangeStartWatcher();
                    if(!doesUserHasAccess(toState, UserResourceClass.getLoggedinUser())){
                        event.preventDefault();
                        $state.go('error-403');
                        console.info('User has no permissions');
                    }
                    if(toState.name === "welcome") {
                        $state.go("dashboard");
                    }
                    registerStateChangeStartWatcher();
                    //if form is dirty and not save  - notify to user
                    if(fromState.data && fromState.data.unsavedChanges && fromParams.id != toParams.id){
                        event.preventDefault();
                        onNavigateOut(toState, toParams);
                    }
                }

            };

            let doesUserHasAccess:Function = (toState, user):boolean =>{

                let isUserHasAccess = true;
                if(toState.permissions && toState.permissions.length > 0) {
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



}

