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
import { Component, ComponentInstance, IAppMenu, Requirement, Capability, ButtonModel } from "app/models";
import { SharingService, CacheService, EventListenerService, LeftPaletteLoaderService } from "app/services";
import { ModalsHandler, GRAPH_EVENTS, ComponentFactory, ChangeLifecycleStateHandler, MenuHandler, EVENTS, ComponentInstanceFactory } from "app/utils";
import { IWorkspaceViewModelScope } from "../../workspace-view-model";
import { ComponentGenericResponse } from "app/ng2/services/responses/component-generic-response";
import { Resource } from "app/models/components/resource";
import { ResourceType, ComponentType } from "app/utils/constants";
import { ComponentServiceFactoryNg2 } from "app/ng2/services/component-services/component.service.factory";
import { ServiceGenericResponse } from "app/ng2/services/responses/service-generic-response";
import { Service } from "app/models/components/service";
import { ZoneInstance } from "app/models/graph/zones/zone-instance";
import { ComponentServiceNg2 } from "app/ng2/services/component-services/component.service";
import { ModalService as ModalServiceSdcUI} from "sdc-ui/lib/angular/modals/modal.service"
import { IModalConfig, IModalButtonComponent } from "sdc-ui/lib/angular/modals/models/modal-config";
import { ValueEditComponent } from "app/ng2/components/ui/forms/value-edit/value-edit.component";
import { UnsavedChangesComponent } from "../../../../ng2/components/ui/forms/unsaved-changes/unsaved-changes.component";
import { ModalButtonComponent } from "sdc-ui/lib/angular/components";



export interface ICompositionViewModelScope extends IWorkspaceViewModelScope {

    currentComponent:Component;

    //Added for now, in the future need to remove and use only id and type to pass to tabs.
    selectedComponent: Component;
    selectedZoneInstance: ZoneInstance;

    componentInstanceNames: Array<string>;
    isLoading:boolean;
    graphApi:any;
    sharingService:SharingService;
    sdcMenu:IAppMenu;
    version:string;
    isViewOnly:boolean;
    isCanvasTagging:boolean;
    isLoadingRightPanel:boolean;
    disabledTabs:boolean;
    openVersionChangeModal(pathsToDelete:string[]):ng.IPromise<any>;
    onComponentInstanceVersionChange(component:Component);
    isComponentInstanceSelected():boolean;
    updateSelectedComponent():void;
    openUpdateModal();
    deleteSelectedComponentInstance():void;
    onBackgroundClick():void;
    setSelectedInstance(componentInstance:ComponentInstance):void;
    setSelectedZoneInstance(zoneInstance: ZoneInstance):void;
    changeZoneInstanceName(newName:string):void;
    printScreen():void;
    isPNF():boolean;
    isConfiguration():boolean;
    preventMoveTab(state: boolean):void;
    registerCreateInstanceEvent(callback: Function):void;
    unregisterCreateInstanceEvent():void;
    registerChangeComponentInstanceNameEvent(callback: Function):void;
    unregisterChangeComponentInstanceNameEvent():void;

    ComponentServiceNg2:ComponentServiceNg2,
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
        'ModalServiceSdcUI',
        'EventListenerService',
        'ComponentServiceFactoryNg2',
        'ComponentServiceNg2',
        'Notification'
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
                private ModalServiceSdcUI: ModalServiceSdcUI,
                private eventListenerService:EventListenerService,
                private ComponentServiceFactoryNg2: ComponentServiceFactoryNg2,
                private ComponentServiceNg2:ComponentServiceNg2,
                private Notification:any
    ) {

        this.$scope.setValidState(true);
        this.initScope();
        this.initGraphData();
        this.registerGraphEvents(this.$scope);
    }


    private initGraphData = ():void => {
        if(!this.hasCompositionGraphData(this.$scope.component)) {
            this.$scope.isLoading = true;
            let service = this.ComponentServiceFactoryNg2.getComponentService(this.$scope.component);
            service.getComponentCompositionData(this.$scope.component).subscribe((response:ComponentGenericResponse) => {
                if (this.$scope.component.isService()) {
                    (<Service> this.$scope.component).forwardingPaths = (<ServiceGenericResponse>response).forwardingPaths;
                }
                this.$scope.component.componentInstances = response.componentInstances || [];
                this.$scope.component.componentInstancesRelations = response.componentInstancesRelations || [];
                this.$scope.component.policies = response.policies || [];
                this.$scope.component.groupInstances = response.groupInstances || [];
                this.$scope.isLoading = false;
                this.initComponent();
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_COMPOSITION_GRAPH_DATA_LOADED);
            });
        } else {
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_COMPOSITION_GRAPH_DATA_LOADED);
        }
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_COMPOSITION_GRAPH_DATA_LOADED);
    };

    private hasCompositionGraphData = (component:Component):boolean => {
        return !!(component.componentInstances && component.componentInstancesRelations && component.policies && component.groupInstances);
    };

    private cacheComponentsInstancesFullData:Array<Component>;

    private initComponent = ():void => {
        this.$scope.currentComponent = this.$scope.component;
        this.$scope.selectedComponent = this.$scope.currentComponent;
        this.$scope.selectedZoneInstance = null;
        this.updateUuidMap();
        this.$scope.isViewOnly = this.$scope.isViewMode();
    };

    private registerGraphEvents = (scope:ICompositionViewModelScope):void => {
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_NODE_SELECTED, scope.setSelectedInstance);
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_ZONE_INSTANCE_SELECTED, scope.setSelectedZoneInstance);
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED, scope.onBackgroundClick);
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_CANVAS_TAG_START, () => {
            scope.isCanvasTagging = true;
            this.eventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, true, this.showUnsavedChangesAlert);
        });
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_CANVAS_TAG_END, () => {
            scope.isCanvasTagging = false;
            this.resetUnsavedChanges();
        });
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_ZONE_INSTANCE_NAME_CHANGED, scope.changeZoneInstanceName);
        this.eventListenerService.registerObserverCallback(EVENTS.UPDATE_PANEL, this.removeSelectedZoneInstance);
    };

    private showUnsavedChangesAlert = (afterSave?:Function):Promise<any> => {
        let deferred = new Promise<any>((resolve, reject)=> {
            const modal = this.ModalServiceSdcUI.openCustomModal(
                {
                    title: "Unsaved Changes",
                    size: 'sm',
                    type: 'custom',

                    buttons: [
                        {id: 'cancelButton', text: 'Cancel', type: 'secondary', size: 'xsm', closeModal: true, callback: () => reject()},
                        {id: 'discardButton', text: 'Discard', type: 'secondary', size: 'xsm', closeModal: true, callback: () => { this.resetUnsavedChanges(); resolve()}},
                        {id: 'saveButton', text: 'Save', type: 'primary', size: 'xsm', closeModal: true, callback: () => {  reject(); this.saveUnsavedChanges(afterSave);  }}
                    ] as IModalButtonComponent[]
                }, UnsavedChangesComponent, { isValidChangedData: true});
        });

        return deferred;
    }

    private unRegisterGraphEvents = (scope: ICompositionViewModelScope):void => {
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_NODE_SELECTED, scope.setSelectedInstance);
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_ZONE_INSTANCE_SELECTED, scope.setSelectedZoneInstance);
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED, scope.onBackgroundClick);
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_CANVAS_TAG_START);
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_CANVAS_TAG_END);
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_ZONE_INSTANCE_NAME_CHANGED, scope.changeZoneInstanceName);
        this.eventListenerService.unRegisterObserver(EVENTS.UPDATE_PANEL, this.removeSelectedZoneInstance);

    };

    private resetUnsavedChanges = () => {
        this.eventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, false);
    }

    private saveUnsavedChanges = (afterSaveFunction?:Function):void => {
        this.$scope.selectedZoneInstance.forceSave.next(afterSaveFunction);
        this.eventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, false);
    }

    private openUpdateComponentInstanceNameModal = ():void => {

        let modalConfig:IModalConfig = {
            title: "Edit Name",
            size: "sm",
            type: "custom",
            testId: "renameInstanceModal",
            buttons: [
                {id: 'saveButton', text: 'OK', size: 'xsm', callback: this.saveInstanceName, closeModal: false},
                {id: 'cancelButton', text: 'Cancel', size: 'sm', closeModal: true}
            ]
        };

        this.ModalServiceSdcUI.openCustomModal(modalConfig, ValueEditComponent, {name: this.$scope.currentComponent.selectedInstance.name, validityChangedCallback: this.enableOrDisableSaveButton});

    };


    private enableOrDisableSaveButton = (shouldEnable: boolean): void => {
        let saveButton: ModalButtonComponent = this.ModalServiceSdcUI.getCurrentInstance().getButtonById('saveButton');
        saveButton.disabled = !shouldEnable;
    }

    private saveInstanceName = () => {
        let currentModal = this.ModalServiceSdcUI.getCurrentInstance();
        let nameFromModal:string = currentModal.innerModalContent.instance.name;

        if(nameFromModal != this.$scope.currentComponent.selectedInstance.name){
            currentModal.buttons[0].disabled = true;
            let componentInstanceModel:ComponentInstance = ComponentInstanceFactory.createComponentInstance(this.$scope.currentComponent.selectedInstance);
            componentInstanceModel.name = nameFromModal;

            let onFailed = (error) => {
                currentModal.buttons[0].disabled = false;
            };
            let onSuccess = (componentInstance:ComponentInstance) => {

                this.$scope.currentComponent.selectedInstance.name = componentInstance.name;
                //update requirements and capabilities owner name
                _.forEach(this.$scope.currentComponent.selectedInstance.requirements, (requirementsArray:Array<Requirement>) => {
                    _.forEach(requirementsArray, (requirement:Requirement):void => {
                        requirement.ownerName = componentInstance.name;
                    });
                });

                _.forEach(this.$scope.currentComponent.selectedInstance.capabilities, (capabilitiesArray:Array<Capability>) => {
                    _.forEach(capabilitiesArray, (capability:Capability):void => {
                        capability.ownerName = componentInstance.name;
                    });
                });
                this.ModalServiceSdcUI.closeModal();
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_COMPONENT_INSTANCE_NAME_CHANGED, this.$scope.currentComponent.selectedInstance);
            };

            this.$scope.currentComponent.updateComponentInstance(componentInstanceModel).then(onSuccess, onFailed);
        }  else {
            this.ModalServiceSdcUI.closeModal();
        }

    };

    private removeSelectedComponentInstance = ():void => {
        this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE, this.$scope.currentComponent.selectedInstance)
        this.$scope.currentComponent.selectedInstance = null;
        this.$scope.selectedComponent = this.$scope.currentComponent;
    };

    private removeSelectedZoneInstance = ():void => {
        this.$scope.currentComponent.selectedInstance = null;
        this.$scope.selectedZoneInstance = null;
        this.$scope.selectedComponent = this.$scope.currentComponent;
    }

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
        this.$scope.isCanvasTagging = false;
        this.$scope.graphApi = {};
        this.$scope.version = this.cacheService.get('version');
        this.initComponent();

        this.cacheComponentsInstancesFullData = new Array<Component>();

        this.$scope.isComponentInstanceSelected = ():boolean => {
            return this.$scope.currentComponent && this.$scope.currentComponent.selectedInstance != undefined && this.$scope.currentComponent.selectedInstance != null;
        };

        this.$scope.$on('$destroy', () => {
            this.unRegisterGraphEvents(this.$scope);
        })

        this.$scope.restoreComponent = ():void => {
            this.ComponentServiceNg2.restoreComponent(this.$scope.selectedComponent.componentType, this.$scope.selectedComponent.uniqueId).subscribe(() => {
                    this.Notification.success({
                        message: '&lt;' + this.$scope.component.name + '&gt; ' + this.$filter('translate')("ARCHIVE_SUCCESS_MESSAGE_TEXT"),
                        title: this.$filter('translate')("ARCHIVE_SUCCESS_MESSAGE_TITLE")
                    });
                    this.$scope.selectedComponent.archived = false;
                }
            )
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
            this.$scope.selectedZoneInstance = null;
            this.$scope.updateSelectedComponent();

            if (this.$state.current.name === 'workspace.composition.api') {
                this.$state.go('workspace.composition.details');
            }
            if(!selectedComponent.isServiceProxy() && (this.$state.current.name === 'workspace.composition.consumption' || this.$state.current.name === 'workspace.composition.dependencies')) {
                this.$state.go('workspace.composition.details');
            }
        };

        this.$scope.setSelectedZoneInstance = (zoneInstance: ZoneInstance): void => {
            this.$scope.currentComponent.selectedInstance = null;
            this.$scope.selectedZoneInstance = zoneInstance;
        };

        this.$scope.onBackgroundClick = ():void => {
            this.$scope.currentComponent.selectedInstance = null;
            this.$scope.selectedZoneInstance = null;
            this.$scope.selectedComponent = this.$scope.currentComponent;

            if (this.$state.current.name === 'workspace.composition.api' || this.$state.current.name === 'workspace.composition.consumption' || this.$state.current.name === 'workspace.composition.dependencies') {
                this.$state.go('workspace.composition.details');
            }

            if(this.$scope.selectedComponent.isService() && this.$state.current.name === 'workspace.composition.relations'){
                this.$state.go('workspace.composition.api');
            }
        };

        this.$scope.openUpdateModal = ():void => {
            this.openUpdateComponentInstanceNameModal();
        };

        this.$scope.changeZoneInstanceName = (newName:string):void => {
            this.$scope.selectedZoneInstance.instanceData.name = newName;
        };

        this.$scope.deleteSelectedComponentInstance = ():void => {
            const {currentComponent} = this.$scope;
            const {title, message} = this.$scope.sdcMenu.alertMessages['deleteInstance'];
            let modalText = message.format([currentComponent.selectedInstance.name]);

            if (currentComponent.isService()) {
                const {forwardingPaths} = (<Service>currentComponent);
                const instanceId = currentComponent.selectedInstance.uniqueId;

                const relatedPaths = _.filter(forwardingPaths, forwardingPath => {
                    const pathElements = forwardingPath.pathElements.listToscaDataDefinition;
                    return pathElements.find(path => path.fromNode === instanceId || path.toNode === instanceId);
                });

                if (relatedPaths.length) {
                    const pathNames = _.map(relatedPaths, path => path.name).join(', ');
                    modalText += `<p>The following service paths will be erased: ${pathNames}</p>`;
                }
            }
            this.ModalServiceSdcUI.openAlertModal(title, modalText, "OK", this.removeSelectedComponentInstance, "deleteInstanceModal");
        };

        this.$scope.openVersionChangeModal = (pathsToDelete:string[]):ng.IPromise<any> => {
            const {currentComponent} = this.$scope;
            const {forwardingPaths} = <Service>currentComponent;

            const relatedPaths = _.filter(forwardingPaths, path =>
                _.find(pathsToDelete, id =>
                    path.uniqueId === id
                )
            ).map(path => path.name);
            const pathNames = _.join(relatedPaths, ', ') || 'none';

            const {title, message} = this.$scope.sdcMenu.alertMessages['upgradeInstance'];
            return this.ModalsHandler.openConfirmationModal(title, message.format([pathNames]), false);
        };

        this.$scope.onComponentInstanceVersionChange = (component:Component):void => {
            let onChange = () => {
                this.$scope.currentComponent = component;
                this.$scope.setComponent(this.$scope.currentComponent);
                this.$scope.updateSelectedComponent();
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_VERSION_CHANGED, this.$scope.currentComponent);
            };

            if (component.isService()) {
                const service = this.ComponentServiceFactoryNg2.getComponentService(component);
                service.getComponentCompositionData(component).subscribe((response:ServiceGenericResponse) => {
                    (<Service>component).forwardingPaths = response.forwardingPaths;
                    onChange();
                });
            } else {
                onChange();
            }
        };

        this.$scope.isPNF = (): boolean => {
            return this.$scope.selectedComponent.isResource() && (<Resource>this.$scope.selectedComponent).resourceType === ResourceType.PNF;
        };

        this.$scope.isConfiguration = (): boolean => {
            return this.$scope.selectedComponent.isResource() && (<Resource>this.$scope.selectedComponent).resourceType === ResourceType.CONFIGURATION;
        };

        this.$scope.preventMoveTab = (state: boolean): void => {
            this.$scope.disabledTabs = state;
        };

        this.eventListenerService.registerObserverCallback(EVENTS.ON_LIFECYCLE_CHANGE, this.$scope.reload);

        this.$scope.registerCreateInstanceEvent = (callback: Function): void => {
            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_CREATE_COMPONENT_INSTANCE, callback);
        };

        this.$scope.unregisterCreateInstanceEvent = (): void => {
            this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_CREATE_COMPONENT_INSTANCE);
        };

        this.$scope.registerChangeComponentInstanceNameEvent = (callback: Function): void => {
            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_COMPONENT_INSTANCE_NAME_CHANGED, callback);
        };

        this.$scope.unregisterChangeComponentInstanceNameEvent = (): void => {
            this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_COMPONENT_INSTANCE_NAME_CHANGED);
        };
    }
}
