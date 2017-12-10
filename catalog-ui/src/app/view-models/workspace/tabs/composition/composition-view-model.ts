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
import {Component, ComponentInstance, IAppMenu} from "app/models";
import {SharingService, CacheService, EventListenerService, LeftPaletteLoaderService} from "app/services";
import {ModalsHandler, GRAPH_EVENTS, ComponentFactory, ChangeLifecycleStateHandler, MenuHandler, EVENTS} from "app/utils";
import {IWorkspaceViewModelScope} from "../../workspace-view-model";
import {ComponentServiceNg2} from "app/ng2/services/component-services/component.service";
import {ComponentGenericResponse} from "app/ng2/services/responses/component-generic-response";
import {Resource} from "app/models/components/resource";
import {ResourceType,ComponentType} from "../../../../utils/constants";

export interface ICompositionViewModelScope extends IWorkspaceViewModelScope {

    currentComponent:Component;
    selectedComponent: Component;
    componentInstanceNames: Array<string>;
    isLoading:boolean;
    graphApi:any;
    sharingService:SharingService;
    sdcMenu:IAppMenu;
    version:string;
    isViewOnly:boolean;
    isLoadingRightPanel:boolean;
    onComponentInstanceVersionChange(component:Component);
    isComponentInstanceSelected():boolean;
    updateSelectedComponent():void
    openUpdateModal();
    deleteSelectedComponentInstance():void;
    onBackgroundClick():void;
    setSelectedInstance(componentInstance:ComponentInstance):void;
    printScreen():void;
    isPNF():boolean;
    isConfiguration():boolean;

    cacheComponentsInstancesFullData:Component;
}

export class CompositionViewModel {

    static '$inject' = [
        '$scope',
        '$log',
        'sdcMenu',
        'MenuHandler',
        '$uibModal',
        '$state',
        'Sdc.Services.SharingService',
        '$filter',
        'Sdc.Services.CacheService',
        'ComponentFactory',
        'ChangeLifecycleStateHandler',
        'LeftPaletteLoaderService',
        'ModalsHandler',
        'EventListenerService',
        'ComponentServiceNg2'
    ];

    constructor(private $scope:ICompositionViewModelScope,
                private $log:ng.ILogService,
                private sdcMenu:IAppMenu,
                private MenuHandler:MenuHandler,
                private $uibModal:ng.ui.bootstrap.IModalService,
                private $state:ng.ui.IStateService,
                private sharingService:SharingService,
                private $filter:ng.IFilterService,
                private cacheService:CacheService,
                private ComponentFactory:ComponentFactory,
                private ChangeLifecycleStateHandler:ChangeLifecycleStateHandler,
                private LeftPaletteLoaderService:LeftPaletteLoaderService,
                private ModalsHandler:ModalsHandler,
                private eventListenerService:EventListenerService,
                private ComponentServiceNg2: ComponentServiceNg2) {

        this.$scope.setValidState(true);
        this.initScope();
        this.initGraphData();
        this.registerGraphEvents(this.$scope);
    }


    private initGraphData = ():void => {
        if(!this.$scope.component.componentInstances || !this.$scope.component.componentInstancesRelations ) {
            this.$scope.isLoading = true;
            this.ComponentServiceNg2.getComponentInstancesAndRelation(this.$scope.component).subscribe((response:ComponentGenericResponse) => {
                this.$scope.component.componentInstances = response.componentInstances;
                this.$scope.component.componentInstancesRelations = response.componentInstancesRelations;
                this.$scope.isLoading = false;
                this.initComponent();
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_COMPOSITION_GRAPH_DATA_LOADED);
            });
        } else {
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_COMPOSITION_GRAPH_DATA_LOADED);
        }
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_COMPOSITION_GRAPH_DATA_LOADED);
    };


    private cacheComponentsInstancesFullData:Array<Component>;

    private initComponent = ():void => {

        this.$scope.currentComponent = this.$scope.component;
        this.$scope.selectedComponent = this.$scope.currentComponent;
        this.updateUuidMap();
        this.$scope.isViewOnly = this.$scope.isViewMode();
    };
    private registerGraphEvents = (scope:ICompositionViewModelScope):void => {

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_NODE_SELECTED, scope.setSelectedInstance);
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED, scope.onBackgroundClick);

    };

    private openUpdateComponentInstanceNameModal = ():void => {
        this.ModalsHandler.openUpdateComponentInstanceNameModal(this.$scope.currentComponent).then(()=> {
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_COMPONENT_INSTANCE_NAME_CHANGED, this.$scope.currentComponent.selectedInstance);
        });
    };

    private removeSelectedComponentInstance = ():void => {
        this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_DELETE_MULTIPLE_COMPONENTS);
        this.$scope.currentComponent.selectedInstance = null;
        this.$scope.selectedComponent = this.$scope.currentComponent;
    };

    private updateUuidMap = ():void => {
        /**
         * In case user press F5, the page is refreshed and this.sharingService.currentEntity will be undefined,
         * but after loadService or loadResource this.sharingService.currentEntity will be defined.
         * Need to update the uuidMap with the new resource or service.
         */
        this.sharingService.addUuidValue(this.$scope.currentComponent.uniqueId, this.$scope.currentComponent.uuid);
    };

    private initScope = ():void => {

        this.$scope.sharingService = this.sharingService;
        this.$scope.sdcMenu = this.sdcMenu;
        this.$scope.isLoading = false;
        this.$scope.isLoadingRightPanel = false;
        this.$scope.graphApi = {};
        this.$scope.version = this.cacheService.get('version');
        this.initComponent();

        this.cacheComponentsInstancesFullData = new Array<Component>();

        this.$scope.isComponentInstanceSelected = ():boolean => {
            return this.$scope.currentComponent && this.$scope.currentComponent.selectedInstance != undefined && this.$scope.currentComponent.selectedInstance != null;
        };

        this.$scope.updateSelectedComponent = ():void => {
            if (this.$scope.currentComponent.selectedInstance) {
                let parentComponentUid = this.$scope.currentComponent.selectedInstance.componentUid
                if(this.$scope.currentComponent.selectedInstance.originType === ComponentType.SERVICE_PROXY){
                    parentComponentUid = this.$scope.currentComponent.selectedInstance.sourceModelUid;
                }
                let componentParent = _.find(this.cacheComponentsInstancesFullData, (component) => {
                    return component.uniqueId === parentComponentUid;
                });
                if (componentParent) {
                    this.$scope.selectedComponent = componentParent;
                }
                else {
                    try {
                        let onSuccess = (component:Component) => {
                            this.$scope.isLoadingRightPanel = false;
                            this.$scope.selectedComponent = component;
                            this.cacheComponentsInstancesFullData.push(component);
                        };
                        let onError = (component:Component) => {
                            console.log("Error updating selected component");
                            this.$scope.isLoadingRightPanel = false;
                        };
                        this.ComponentFactory.getComponentFromServer(this.$scope.currentComponent.selectedInstance.originType, parentComponentUid).then(onSuccess, onError);
                    } catch (e) {
                        console.log("Error updating selected component", e);
                        this.$scope.isLoadingRightPanel = false;
                    }
                }
            }
            else {

                this.$scope.selectedComponent = this.$scope.currentComponent;
            }
        };

        this.$scope.setSelectedInstance = (selectedComponent:ComponentInstance):void => {

            this.$log.debug('composition-view-model::onNodeSelected:: with id: ' + selectedComponent.uniqueId);
            this.$scope.currentComponent.setSelectedInstance(selectedComponent);
            this.$scope.updateSelectedComponent();

            if (this.$state.current.name === 'workspace.composition.api') {
                this.$state.go('workspace.composition.details');
            }
        };

        this.$scope.onBackgroundClick = ():void => {
            this.$scope.currentComponent.selectedInstance = null;
            this.$scope.selectedComponent = this.$scope.currentComponent;

            if (this.$state.current.name === 'workspace.composition.api') {
                this.$state.go('workspace.composition.details');
            }

            if(this.$scope.selectedComponent.isService() && this.$state.current.name === 'workspace.composition.relations'){
                this.$state.go('workspace.composition.api');
            }
        };

        this.$scope.openUpdateModal = ():void => {
            this.openUpdateComponentInstanceNameModal();
        };
    
        this.$scope.deleteSelectedComponentInstance = ():void => {
            let state = "deleteInstance";
            let onOk = ():void => {
                this.removeSelectedComponentInstance();
                //this.$scope.graphApi.deleteSelectedNodes();
            };
            let title:string = this.$scope.sdcMenu.alertMessages[state].title;
            let message:string = this.$scope.sdcMenu.alertMessages[state].message.format([this.$scope.currentComponent.selectedInstance.name]);
            this.ModalsHandler.openAlertModal(title, message).then(onOk);
        };

        this.$scope.onComponentInstanceVersionChange = (component:Component):void => {
            this.$scope.currentComponent = component;
            this.$scope.setComponent(this.$scope.currentComponent);
            this.$scope.updateSelectedComponent();
        };

        this.$scope.isPNF = (): boolean => {
            return this.$scope.selectedComponent.isResource() && (<Resource>this.$scope.selectedComponent).resourceType === ResourceType.PNF;
        };

        this.$scope.isConfiguration = (): boolean => {
            return this.$scope.selectedComponent.isResource() && (<Resource>this.$scope.selectedComponent).resourceType === ResourceType.CONFIGURATION;
        };

        this.eventListenerService.registerObserverCallback(EVENTS.ON_CHECKOUT, this.$scope.reload);

    }
}
