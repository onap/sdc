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
/// <reference path="../../../../references"/>
module Sdc.ViewModels {
    'use strict';

    export interface IDeploymentViewModelScope extends IWorkspaceViewModelScope {

        currentComponent: Models.Components.Component;
        selectedComponent: Models.Components.Component;
        isLoading: boolean;
        sharingService:Sdc.Services.SharingService;
        sdcMenu:Models.IAppMenu;
        version:string;
        isViewOnly:boolean;
        tabs:Array<Models.Tab>;

        setComponent(component: Models.Components.Component);
        isComponentInstanceSelected():boolean;
        updateSelectedComponent(): void
        openUpdateModal();
        deleteSelectedComponentInstance():void;
        onBackgroundClick():void;
        setSelectedInstance(componentInstance: Models.ComponentsInstances.ComponentInstance): void;
        printScreen():void;

    }

    export class DeploymentViewModel {

        static '$inject' = [
            '$scope',
            'sdcMenu',
            'MenuHandler',
            '$modal',
            '$templateCache',
            '$state',
            'Sdc.Services.SharingService',
            '$filter',
            'Sdc.Services.CacheService',
            'ComponentFactory',
            'ChangeLifecycleStateHandler',
            'LeftPaletteLoaderService',
            'ModalsHandler'
        ];

        constructor(private $scope:IDeploymentViewModelScope,
                    private sdcMenu:Models.IAppMenu,
                    private MenuHandler: Utils.MenuHandler,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $templateCache:ng.ITemplateCacheService,
                    private $state:ng.ui.IStateService,
                    private sharingService:Services.SharingService,
                    private $filter:ng.IFilterService,
                    private cacheService:Services.CacheService,
                    private ComponentFactory: Utils.ComponentFactory,
                    private ChangeLifecycleStateHandler: Sdc.Utils.ChangeLifecycleStateHandler,
                    private LeftPaletteLoaderService: Services.Components.LeftPaletteLoaderService,
                    private ModalsHandler: Sdc.Utils.ModalsHandler) {

            this.$scope.setValidState(true);
            this.initScope();
            this.$scope.updateSelectedMenuItem();
        }


        private initComponent = ():void => {

            this.$scope.currentComponent = this.$scope.component;
            this.$scope.selectedComponent = this.$scope.currentComponent;
            this.updateUuidMap();
            this.$scope.isViewOnly = this.$scope.isViewMode();
        };


        private updateUuidMap = ():void => {
            /**
             * In case user press F5, the page is refreshed and this.sharingService.currentEntity will be undefined,
             * but after loadService or loadResource this.sharingService.currentEntity will be defined.
             * Need to update the uuidMap with the new resource or service.
             */
            this.sharingService.addUuidValue(this.$scope.currentComponent.uniqueId,this.$scope.currentComponent.uuid);
        };

        private initRightTabs = ()=> {
            if(this.$scope.currentComponent.groups){

                let hierarchyTab = new Models.Tab('/app/scripts/view-models/tabs/hierarchy/hierarchy-view.html', 'Sdc.ViewModels.HierarchyViewModel', 'hierarchy', this.$scope.currentComponent, 'hierarchy');
                this.$scope.tabs = Array<Models.Tab>();
                this.$scope.tabs.push(hierarchyTab)
            }

        }
        private initScope = ():void => {

            this.$scope.sharingService = this.sharingService;
            this.$scope.sdcMenu = this.sdcMenu;
            this.$scope.isLoading = false;

            this.$scope.version = this.cacheService.get('version');
            this.initComponent();

            this.$scope.setComponent = (component: Models.Components.Product):void => {
                this.$scope.currentComponent = component;
            }
            
            this.initRightTabs();
        }
    }
}
