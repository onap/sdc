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
 * Created by obarda on 3/30/2016.
 */
'use strict';
import * as _ from 'lodash';
import {
    IUserProperties,
    IAppMenu,
    Resource,
    Service, 
    Component,
    Plugin,
    PluginsConfiguration,
    PluginDisplayOptions
} from 'app/models';
import {
    MenuItem, ModalsHandler, States, EVENTS, CHANGE_COMPONENT_CSAR_VERSION_FLAG, ResourceType, PREVIOUS_CSAR_COMPONENT,
    WorkspaceMode, ComponentFactory, ChangeLifecycleStateHandler, Role, ComponentState, MenuItemGroup, MenuHandler
} from 'app/utils';
import {
    EventListenerService,
    LeftPaletteLoaderService,
    ProgressService
} from 'app/services';
import {
    CacheService
} from 'app/services-ng2';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import { AutomatedUpgradeService } from '../../ng2/pages/automated-upgrade/automated-upgrade.service';
import { CatalogService } from '../../ng2/services/catalog.service';
import { ComponentServiceNg2 } from '../../ng2/services/component-services/component.service';
import { EventBusService } from '../../ng2/services/event-bus.service';
import { HomeService } from '../../ng2/services/home.service';
import { PluginsService } from '../../ng2/services/plugins.service';
import { IDependenciesServerResponse } from '../../ng2/services/responses/dependencies-server-response';
import { WorkspaceNg1BridgeService } from '../../ng2/pages/workspace/workspace-ng1-bridge-service';
import { WorkspaceService } from '../../ng2/pages/workspace/workspace.service';

export interface IWorkspaceViewModelScope extends ng.IScope {

    isLoading: boolean;
    isCreateProgress: boolean;
    component: Component;
    originComponent: Component;
    componentType: string;
    importFile: any;
    leftBarTabs: MenuItemGroup;
    isNew: boolean;
    isFromImport: boolean;
    isValidForm: boolean;
    isActiveTopBar: boolean;
    mode: WorkspaceMode;
    breadcrumbsModel: Array<MenuItemGroup>;
    sdcMenu: IAppMenu;
    changeLifecycleStateButtons: any;
    version: string;
    versionsList: Array<any>;
    changeVersion: any;
    isComposition: boolean;
    isDeployment: boolean;
    isPlugins: boolean;
    $state: ng.ui.IStateService;
    user: IUserProperties;
    thirdParty: boolean;
    disabledButtons: boolean;
    menuComponentTitle: string;
    progressService: ProgressService;
    progressMessage: string;
    ComponentServiceNg2: ComponentServiceNg2;
    // leftPanelComponents:Array<Models.Components.Component>; //this is in order to load the left panel once, and not wait long time when moving to composition
    unsavedChanges: boolean;
    unsavedChangesCallback: Function;
    unsavedFile: boolean;
    hasNoDependencies: boolean;
    models: Array<string>;

    startProgress(message: string): void;
    stopProgress(): void;
    updateBreadcrumbs(component: Component): void;
    updateUnsavedFileFlag(isUnsaved: boolean): void;
    showChangeStateButton(): boolean;
    getComponent(): Component;
    setComponent(component: Component): void;
    setOriginComponent(component: Component): void;
    onMenuItemPressed(state: string, params: any): ng.IPromise<boolean>;
    create(): void;
    save(): Promise<void>;
    setValidState(isValid: boolean): void;
    changeLifecycleState(state: string): void;
    handleChangeLifecycleState(state: string, newCsarVersion?: string, errorFunction?: Function): void;
    disableMenuItems(): void;
    enableMenuItems(): void;
    isDesigner(): boolean;
    isViewMode(): boolean;
    isEditMode(): boolean;
    isCreateMode(): boolean;
    isDisableMode(): boolean;
    isGeneralView(): boolean;
    goToBreadcrumbHome(): void;
    onVersionChanged(selectedId: string): void;
    getLatestVersion(): void;
    getStatus(): string;
    showLifecycleIcon(): boolean;
    updateSelectedMenuItem(state: string): void;
    isSelected(menuItem: MenuItem): boolean;
    uploadFileChangedInGeneralTab(): void;
    updateMenuComponentName(ComponentName: string): void;
    getTabTitle(): string;
    reload(component: Component): void;
}

export class WorkspaceViewModel {

    static '$inject' = [
        '$scope',
        'injectComponent',
        'ComponentFactory',
        '$state',
        'sdcMenu',
        '$q',
        'MenuHandler',
        'Sdc.Services.CacheService',
        'ChangeLifecycleStateHandler',
        'LeftPaletteLoaderService',
        '$filter',
        'EventListenerService',
        'Notification',
        '$stateParams',
        'HomeService',
        'CatalogService',
        'Sdc.Services.ProgressService',
        'ComponentServiceNg2',
        'AutomatedUpgradeService',
        'EventBusService',
        'ModalServiceSdcUI',
        'PluginsService',
        'WorkspaceNg1BridgeService',
        'workspaceService'
    ];

    constructor(private $scope: IWorkspaceViewModelScope,
                private injectComponent: Component,
                private ComponentFactory: ComponentFactory,
                private $state: ng.ui.IStateService,
                private sdcMenu: IAppMenu,
                private $q: ng.IQService,
                private MenuHandler: MenuHandler,
                private cacheService: CacheService,
                private ChangeLifecycleStateHandler: ChangeLifecycleStateHandler,
                private LeftPaletteLoaderService: LeftPaletteLoaderService,
                private $filter: ng.IFilterService,
                private EventListenerService: EventListenerService,
                private Notification: any,
                private $stateParams: any,
                private homeService: HomeService,
                private catalogService: CatalogService,
                private progressService: ProgressService,
                private ComponentServiceNg2: ComponentServiceNg2,
                private AutomatedUpgradeService: AutomatedUpgradeService,
                private eventBusService: EventBusService,
                private modalServiceSdcUI: SdcUiServices.ModalService,
                private pluginsService: PluginsService,
                private workspaceNg1BridgeService: WorkspaceNg1BridgeService,
                private workspaceService: WorkspaceService) {

                this.initScope();
                // this.initAfterScope();
                this.$scope.updateSelectedMenuItem(this.$state.current.name);
    }

    private role: string;
    private category: string;
    private components: Component[];
    
    private initViewMode = ():WorkspaceMode => {
        let mode = WorkspaceMode.VIEW;

        if (!this.$state.params['id']) {   //&& !this.$state.params['vspComponent']
            mode = WorkspaceMode.CREATE;
        } else {
            if (this.$scope.component.lifecycleState === ComponentState.NOT_CERTIFIED_CHECKOUT &&
                this.$scope.component.lastUpdaterUserId === this.cacheService.get('user').userId) {
                if ((this.$scope.component.isService() || this.$scope.component.isResource()) && this.role === Role.DESIGNER) {
                    mode = WorkspaceMode.EDIT;
                }
            }
        }
        this.workspaceNg1BridgeService.updateIsViewOnly(mode === WorkspaceMode.VIEW);
        return mode;
    }

    private initChangeLifecycleStateButtons = (): void => {
        let state: string;
        if (this.$scope.component.isService() && this.$scope.component.lifecycleState === 'CERTIFIED') {
            state = this.$scope.component.distributionStatus;
        } else {
            state = this.$scope.component.lifecycleState;
        }
        this.$scope.changeLifecycleStateButtons = (this.sdcMenu.roles[this.role].changeLifecycleStateButtons[state] || [])[this.$scope.component.componentType.toUpperCase()];
    }

    private initScope = (): void => {
        this.$scope.component = this.injectComponent;
        this.$scope.menuComponentTitle = this.$scope.component.name;
        this.$scope.disabledButtons = false;
        this.$scope.originComponent = this.ComponentFactory.createComponent(this.$scope.component);
        this.$scope.componentType = this.$scope.component.componentType;
        this.$scope.version = this.cacheService.get('version');
        this.$scope.user = this.cacheService.get('user');
        this.role = this.$scope.user.role;
        this.category = this.$scope.component.selectedCategory;
        this.$scope.mode = this.initViewMode();
        this.$scope.isValidForm = true;
        this.initChangeLifecycleStateButtons();
        this.initVersionObject();
        this.$scope.$state = this.$state;
        this.$scope.isLoading = false;
        this.$scope.isComposition = (this.$state.current.name.indexOf(States.WORKSPACE_COMPOSITION) > -1);
        this.$scope.isDeployment = this.$state.current.name == States.WORKSPACE_DEPLOYMENT;
        this.$scope.progressService = this.progressService;
        this.$scope.unsavedChanges = false;

        this.$scope.hasNoDependencies = true;
        this.verifyIfDependenciesExist();

        this.EventListenerService.registerObserverCallback(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, this.setWorkspaceButtonState);
        this.$scope.getComponent = (): Component => {
            return this.$scope.component;
        };

        this.$scope.updateMenuComponentName = (ComponentName:string):void => {
            this.$scope.menuComponentTitle = ComponentName;
        };

        this.$scope.sdcMenu = this.sdcMenu;
        // Will be called from each step after save to update the resource.
        this.$scope.setComponent = (component:Component):void => {
            this.$scope.component = component;
        };

        this.$scope.setOriginComponent = (component:Component):void => {
            this.$scope.originComponent = component;
        }

        this.$scope.uploadFileChangedInGeneralTab = ():void => {
            // In case user select browse file, and in update mode, need to disable submit for testing and checkin buttons.
            if (this.$scope.isEditMode() && this.$scope.component.isResource() && (<Resource>this.$scope.component).resourceType == ResourceType.VF) {
                // NOTE: Commented out the disabling of the workspace buttons on CSAR updating due fix of a bug [417534]
                // this.$scope.disabledButtons = true;
            }
        };

        this.$scope.archiveComponent = ():void => {
            this.$scope.isLoading = true;
            const typeComponent = this.$scope.component.componentType;
            this.ComponentServiceNg2.archiveComponent(typeComponent, this.$scope.component.uniqueId).subscribe(()=> {
                this.$scope.isLoading = false;
                if (this.$state.params.previousState) {
                    switch (this.$state.params.previousState) {
                        case 'catalog':
                        case 'dashboard':
                            this.$state.go(this.$state.params.previousState);
                            break;
                        default:
                            break;
                    }
                }
                this.$scope.component.isArchived = true;
                this.deleteArchiveCache();

                this.Notification.success({
                    message: this.$scope.component.name + ' ' + this.$filter('translate')("ARCHIVE_SUCCESS_MESSAGE_TEXT"),
                    title: this.$filter('translate')("ARCHIVE_SUCCESS_MESSAGE_TITLE")
                });
            }, (error) => { this.$scope.isLoading = false; });
        }

        this.$scope.restoreComponent = ():void => {
            this.$scope.isLoading = true;
            const typeComponent = this.$scope.component.componentType;
            this.ComponentServiceNg2.restoreComponent(typeComponent, this.$scope.component.uniqueId).subscribe(()=> {
                this.$scope.isLoading = false;
                this.Notification.success({
                    message: this.$scope.component.name + ' ' + this.$filter('translate')("RESTORE_SUCCESS_MESSAGE_TEXT"),
                    title: this.$filter('translate')("RESTORE_SUCCESS_MESSAGE_TITLE")
                });
                this.$scope.reload(this.$scope.component);
            });
            this.$scope.component.isArchived = false;
            this.deleteArchiveCache();
        }

        this.$scope.$on('$stateChangeStart', (event, toState, toParams, fromState, fromParams) => {
            if(this.$scope.isEditMode()){
                if (fromParams.id == toParams.id && this.$state.current.data && this.$state.current.data.unsavedChanges) {
                    event.preventDefault();
                    if(this.$scope.isValidForm){
                        this.$scope.save().then(() => {
                            this.$scope.onMenuItemPressed(toState.name, toParams);
                        }, ()=> {
                            console.error("Save failed, unable to navigate to " + toState.name);
                        })
                    } else {
                        console.error("Form is invalid, unable to navigate to " + toState.name);
                    }
                }
            }

        });

        this.$scope.$on('$stateChangeSuccess', (event, toState) => {
            this.$scope.updateSelectedMenuItem(this.$state.current.name);
        });

        this.$scope.onMenuItemPressed = (state:string, params:any):ng.IPromise<boolean> => {

            let deferred:ng.IDeferred<boolean> = this.$q.defer();
            let goToState = ():void => {
                this.$state.go(state, Object.assign({
                    id: this.$scope.component.uniqueId,
                    type: this.$scope.component.componentType.toLowerCase(),
                    components: this.components
                }, params));
                deferred.resolve(true);
            };

            if (this.$scope.isEditMode() && //this is a workaround for amdocs - we need to get the artifact in order to avoid saving the vf when moving from their tabs
                (this.$state.current.name === States.WORKSPACE_MANAGEMENT_WORKFLOW || this.$state.current.name === States.WORKSPACE_NETWORK_CALL_FLOW)) {
                let onGetSuccess = (component:Component) => {
                    this.$scope.isLoading = false;
                    // Update the components
                    this.$scope.component = component;
                    goToState();
                };
                let onFailed = () => {
                    this.EventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_SAVE_BUTTON_ERROR);
                    this.$scope.isLoading = false; // stop the progress.
                    deferred.reject(false);
                };
                this.$scope.component.getComponent().then(onGetSuccess, onFailed);
            } else {
                goToState();
            }
            return deferred.promise;
        };

        this.$scope.setValidState = (isValid:boolean):void => {
            this.$scope.isValidForm = isValid;
        };

        this.$scope.onVersionChanged = (selectedId:string):void => {
            if (this.$scope.isGeneralView() && this.$state.current.data.unsavedChanges) {
                this.$scope.changeVersion.selectedVersion = _.find(this.$scope.versionsList, (versionObj)=> {
                    return versionObj.versionId === this.$scope.component.uniqueId;
                });
            }

            let eventData = {
                uuid: this.$scope.component.uuid,
                version: this.$scope.changeVersion.selectedVersion.versionNumber
            };

            this.eventBusService.notify("VERSION_CHANGED", eventData).subscribe(() => {
                this.$scope.isLoading = true;

                this.$state.go(this.$state.current.name, {
                    id: selectedId,
                    type: this.$scope.componentType.toLowerCase(),
                    mode: WorkspaceMode.VIEW,
                    components: this.$state.params['components']
                }, {reload: true});
            });
        };

        this.$scope.getLatestVersion = ():void => {
            this.$scope.onVersionChanged(_.first(this.$scope.versionsList).versionId);
        };

        this.$scope.create = () => {

            this.$scope.startProgress("Creating Asset...");
            _.first(this.$scope.leftBarTabs.menuItems).isDisabled = true;//disabled click on general tab (DE246274)

             // In case we import CSAR. Notify user that import VF will take long time (the create is performed in the background).
             if (this.$scope.component.isResource() && (<Resource>this.$scope.component).csarUUID) {
                this.Notification.info({
                    message: this.$filter('translate')("IMPORT_VF_MESSAGE_CREATE_TAKES_LONG_TIME_DESCRIPTION"),
                    title: this.$filter('translate')("IMPORT_VF_MESSAGE_CREATE_TAKES_LONG_TIME_TITLE")
                });
            }

            let onFailed = () => {
                this.$scope.stopProgress();
                this.$scope.isLoading = false; // stop the progress.
                _.first(this.$scope.leftBarTabs.menuItems).isDisabled = false;//enabled click on general tab (DE246274)
                this.EventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_SAVE_BUTTON_ERROR);
                let modalInstance:ng.ui.bootstrap.IModalServiceInstance;
                modalInstance && modalInstance.close();  // Close the modal in case it is opened.
                this.$scope.component.tags = _.without(this.$scope.component.tags, this.$scope.component.name);// for fix DE246217

                this.$scope.setValidState(true);  // Set the form valid (if sent form is valid, the error from server).
            };

            let onSuccessCreate = (component:Component) => {

                this.$scope.stopProgress();
                this.showSuccessNotificationMessage();

                // Update the components list for breadcrumbs
                this.components.unshift(component);

                this.$state.go(States.WORKSPACE_GENERAL, {
                    id: component.uniqueId,
                    type: component.componentType.toLowerCase(),
                    components: this.components
                }, {inherit: false});
            };
	    
            console.log(this.$scope.component, "this.$scope.component")
            if ((<Service>this.$scope.component).serviceType == "Service") {
                this.ComponentFactory.importComponentOnServer(this.$scope.component).then(onSuccessCreate, onFailed);
            } else {
                this.ComponentFactory.createComponentOnServer(this.$scope.component).then(onSuccessCreate, onFailed);
            }



        };

        this.$scope.save = ():Promise<void> => {

            this.EventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_SAVE_BUTTON_CLICK);

            this.$scope.startProgress("Updating Asset...");
            this.$scope.disableMenuItems();

            return new Promise<void>((resolve, reject) => {
                let stopProgressAndEnableUI = () => {
                    this.$scope.disabledButtons = false;
                    this.$scope.isLoading = false;
                    this.$scope.enableMenuItems();
                    this.$scope.stopProgress();
                }

                let onFailed = () => {
                    stopProgressAndEnableUI();
                    this.$scope.updateUnsavedFileFlag(true);
                    this.EventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_SAVE_BUTTON_ERROR);

                    reject();
                };

                let onSuccessUpdate = (component:Component) => {
                    stopProgressAndEnableUI();
                    this.showSuccessNotificationMessage();

                    component.tags = _.reject(component.tags, (item)=> {
                        return item === component.name
                    });

                    this.$scope.updateBreadcrumbs(component);

                    //update the component
                    this.$scope.setComponent(component);
                    this.$scope.originComponent = this.ComponentFactory.createComponent(this.$scope.component);

                    if (this.cacheService.contains(CHANGE_COMPONENT_CSAR_VERSION_FLAG)) {
                        this.cacheService.remove(CHANGE_COMPONENT_CSAR_VERSION_FLAG);
                    }
                    if (this.cacheService.contains(PREVIOUS_CSAR_COMPONENT)) {
                        this.cacheService.remove(PREVIOUS_CSAR_COMPONENT);
                    }

                    //clear edit flags
                    this.$state.current.data.unsavedChanges = false;
                    this.$scope.unsavedFile = false;
                    this.$scope.reload(component);
                    resolve();
                };

                this.$scope.component.updateComponent().then(onSuccessUpdate, onFailed);
            });

        };

        this.$scope.changeLifecycleState = (state:string):void => {
            if (this.$scope.isGeneralView() && state !== 'deleteVersion') {
                this.EventListenerService.notifyObservers(EVENTS.ON_LIFECYCLE_CHANGE_WITH_SAVE, state);
            } else {
                this.$scope.handleChangeLifecycleState(state);
            }
        };

        let defaultActionAfterChangeLifecycleState = ():void => {
            if (this.$state.current.data && this.$state.current.data.unsavedChanges) {
                this.$state.current.data.unsavedChanges = false;
            }
            this.$state.go('dashboard');
        };

        this.$scope.handleChangeLifecycleState = (state:string, newCsarVersion?:string, onError?: Function) => {
            if ('monitor' === state) {
                this.$state.go('workspace.distribution');
                return;
            }

            let data = this.$scope.changeLifecycleStateButtons[state];
            if (!data && this.$stateParams.componentCsar && !this.$scope.isCreateMode()) {
                data = {text: 'Check Out', url: 'lifecycleState/CHECKOUT'};
            }
            const onSuccess = (component, url:string):void => {
                // Updating the component from server response
 
                // Creating the data object to notify the plugins with
                const eventData: any = {
                    uuid: this.$scope.component.uuid,
                    version: this.$scope.component.version
                };

                // the server returns only metaData (small component) except checkout (Full component)  ,so we update only the statuses of distribution & lifecycle
                this.$scope.component.lifecycleState = component.lifecycleState;
                this.$scope.component.distributionStatus = component.distributionStatus;

                switch (url) {
                    case 'lifecycleState/CHECKOUT':
                        this.workspaceNg1BridgeService.updateIsViewOnly(false);
                        this.eventBusService.notify("CHECK_OUT", eventData, false).subscribe(() => {
                            // only checkOut get the full component from server
                            //   this.$scope.component = component;
                            // Work around to change the csar version
                            if(newCsarVersion) {
                                this.cacheService.set(CHANGE_COMPONENT_CSAR_VERSION_FLAG, newCsarVersion);
                                (this.$scope.component as Resource).csarVersion = newCsarVersion;
                            }

                            //when checking out a minor version uuid remains
                            const bcIdx = _.findIndex(this.components, (item) => {
                                return item.uuid === component.uuid;
                            });
                            if (bcIdx !== -1) {
                                this.components[bcIdx] = component;
                            } else {
                                //when checking out a major(certified) version
                                this.components.unshift(component);
                            }
                            this.$scope.mode = this.initViewMode();
                            this.initChangeLifecycleStateButtons();
                            this.initVersionObject();
                            this.$scope.isLoading = false;
                            this.EventListenerService.notifyObservers(EVENTS.ON_CHECKOUT, component);
                            this.workspaceService.setComponentMetadata(component.componentMetadata);

                            this.Notification.success({
                                message: this.$filter('translate')("CHECKOUT_SUCCESS_MESSAGE_TEXT"),
                                title: this.$filter('translate')("CHECKOUT_SUCCESS_MESSAGE_TITLE")
                            });

                        });
                        break;
                    case 'lifecycleState/CHECKIN':
                        this.workspaceNg1BridgeService.updateIsViewOnly(true);
                        defaultActionAfterChangeLifecycleState();
                        this.Notification.success({
                            message: this.$filter('translate')("CHECKIN_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("CHECKIN_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    case 'lifecycleState/UNDOCHECKOUT':
                        this.eventBusService.notify("UNDO_CHECK_OUT", eventData, false).subscribe(() => {
                            defaultActionAfterChangeLifecycleState();
                            this.Notification.success({
                                message: this.$filter('translate')("DELETE_SUCCESS_MESSAGE_TEXT"),
                                title: this.$filter('translate')("DELETE_SUCCESS_MESSAGE_TITLE")
                            });
                        });
                        break;
                    case 'lifecycleState/certify':
                        this.$scope.handleCertification(component);
                        this.verifyIfDependenciesExist();
                        this.$scope.reload(component);
                        break;
                    case 'distribution/PROD/activate':
                        this.Notification.success({
                            message: this.$filter('translate')("DISTRIBUTE_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("DISTRIBUTE_SUCCESS_MESSAGE_TITLE")
                        });
                        this.initChangeLifecycleStateButtons();
                        break;
                    default :
                        defaultActionAfterChangeLifecycleState();
                }
                if (data.url !== 'lifecycleState/CHECKOUT') {
                    this.$scope.isLoading = false;
                }
            };
            this.ChangeLifecycleStateHandler.changeLifecycleState(this.$scope.component, data, this.$scope, onSuccess);
        };

        this.$scope.deleteArchivedComponent = (): void => {
            const modalTitle: string = this.$filter('translate')("COMPONENT_VIEW_DELETE_MODAL_TITLE");
            const modalMessage: string = this.$filter('translate')("COMPONENT_VIEW_DELETE_MODAL_TEXT");
            const modalButton = {
                testId: 'ok-button',
                text: this.sdcMenu.alertMessages.okButton,
                type: SdcUiCommon.ButtonType.warning,
                callback: this.$scope.handleDeleteArchivedComponent,
                closeModal: true
            } as SdcUiComponents.ModalButtonComponent;
            this.modalServiceSdcUI.openWarningModal(modalTitle, modalMessage, 'alert-modal', [modalButton]);
        };

        this.$scope.handleDeleteArchivedComponent = (): void => {
            this.$scope.isLoading = true;
            const typeComponent = this.$scope.component.componentType;
            this.ComponentServiceNg2.deleteComponent(typeComponent, this.$scope.component.uniqueId).subscribe(()=> {
                this.deleteArchiveCache();
                this.Notification.success({
                    message: this.$scope.component.name + ' ' + this.$filter('translate')("DELETE_SUCCESS_MESSAGE_TEXT"),
                    title: this.$filter('translate')("DELETE_SUCCESS_MESSAGE_TITLE")
                });
                if (this.$state.params.previousState) {
                    switch (this.$state.params.previousState) {
                        case 'catalog':
                        case 'dashboard':
                            this.$state.go(this.$state.params.previousState);
                            break;
                        default:
                            this.$state.go('dashboard');
                            break;
                    }
                }
                this.$scope.isLoading = false;
            }, () => {
                this.Notification.error({
                    message: this.$scope.component.name + ' ' + this.$filter('translate')('DELETE_FAILURE_MESSAGE_TEXT'),
                    title: this.$filter('translate')('DELETE_FAILURE_MESSAGE_TITLE')
                });
                this.$scope.isLoading = false;
            });
        };

        this.$scope.isViewMode = ():boolean => {
            return this.$scope.mode === WorkspaceMode.VIEW;
        };

        this.$scope.isDesigner = ():boolean => {
            return this.role == Role.DESIGNER;
        };

        this.$scope.isDisableMode = ():boolean => {
            return this.$scope.mode === WorkspaceMode.VIEW && this.$scope.component.lifecycleState === ComponentState.NOT_CERTIFIED_CHECKIN;
        };

        this.$scope.isGeneralView = ():boolean => {
            //we show revert and save icons only in general view
            return this.$state.current.name === States.WORKSPACE_GENERAL;
        };

        this.$scope.isCreateMode = ():boolean => {
            return this.$scope.mode === WorkspaceMode.CREATE;
        };

        this.$scope.checkDisableButton = (button: any):boolean => {
            // Logic moved from html to component
            if (this.$scope.isCreateMode() || button.disabled || this.$scope.disabledButtons || !this.$scope.isValidForm || this.$scope.unsavedChanges || this.$scope.component.isArchived){
                return true;
            }

            // Specific verification for Checkout - enabled only in case the component is the latest version.
            let result: boolean = false;

            if (button.url === 'lifecycleState/CHECKOUT') {
                result = !this.$scope.component.isLatestVersion();
            }
            return result;
        };

        this.$scope.isEditMode = ():boolean => {
            return this.$scope.mode === WorkspaceMode.EDIT;
        };

        this.$scope.goToBreadcrumbHome = ():void => {
            let bcHome:MenuItemGroup = this.$scope.breadcrumbsModel[0];
            this.$state.go(bcHome.menuItems[bcHome.selectedIndex].state);
        };

        this.$scope.showLifecycleIcon = ():boolean => {
            return this.role == Role.DESIGNER;
        };

        this.$scope.getStatus = ():string => {
            if (this.$scope.isCreateMode()) {
                return 'IN DESIGN';
            }

            return this.$scope.component.getStatus(this.sdcMenu);
        };

        this.initMenuItems();

        this.$scope.showLatestVersion = (): boolean => {
            let result: boolean = true;
            if (!this.$scope.component.isLatestVersion()) {
                result = false;
            }
            if (ComponentState.NOT_CERTIFIED_CHECKOUT === this.$scope.component.lifecycleState && this.$scope.isViewMode()) {
                result = false;
            }
            return result;
        };

        this.$scope.updateSelectedMenuItem = (state:string):void => {
            let stateArray:Array<string> = state.split('.', 2);
            let stateWithoutInternalNavigate:string = stateArray[0] + '.' + stateArray[1];
            let selectedItem:MenuItem = _.find(this.$scope.leftBarTabs.menuItems, (item:MenuItem) => {
                let itemStateArray:Array<string> = item.state.split('.', 2);
                let itemStateWithoutNavigation:string = itemStateArray[0] + '.' + itemStateArray[1];
                return (itemStateWithoutNavigation === stateWithoutInternalNavigate);
            });

            let selectedIndex = selectedItem ? this.$scope.leftBarTabs.menuItems.indexOf(selectedItem) : 0;

            if (stateArray[1] === 'plugins') {
                _.forEach(PluginsConfiguration.plugins, (plugin) => {
                    if (plugin.pluginStateUrl == this.$state.params.path) {
                        return false;
                    }
                    else if (this.pluginsService.isPluginDisplayedInContext(plugin, this.role, this.$scope.component.getComponentSubType())) {
                        selectedIndex++;
                    }
                });
            }

            this.$scope.leftBarTabs.selectedIndex = selectedIndex;
        };

        this.$scope.isSelected = (menuItem: MenuItem): boolean => {
            return this.$scope.leftBarTabs.selectedIndex === _.indexOf(this.$scope.leftBarTabs.menuItems, menuItem);
        };

        this.$scope.$watch('$state.current.name', (newVal: string): void => {
            if (newVal) {
                this.$scope.isComposition = (newVal.indexOf(States.WORKSPACE_COMPOSITION) > -1);
                this.$scope.isDeployment = newVal == States.WORKSPACE_DEPLOYMENT;
                this.$scope.isPlugins = newVal == States.WORKSPACE_PLUGINS;
            }
        });

        this.$scope.getTabTitle = (): string => {
            return this.$scope.leftBarTabs.menuItems.find((menuItem: MenuItem) => {
                return menuItem.state == this.$scope.$state.current.name;
            }).text;
        };

        this.$scope.reload = (component: Component): void => {
            const isGeneralTab = this.$state.current.name === 'workspace.general';
            // nullify the componentCsar in case we are in general tab so we know we didnt came from updateVsp Modal
            if (isGeneralTab) {
                this.$state.go(this.$state.current.name, {id: component.uniqueId, componentCsar: null}, {reload: true});
            } else {
                this.$state.go(this.$state.current.name, {id: component.uniqueId}, {reload: true});
            }
        };

        this.$scope.$on('$destroy', () => {
            this.EventListenerService.unRegisterObserver(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES);
        });

        this.$scope.openAutomatedUpgradeModal = ():void => {
            this.$scope.isLoading = true;
            this.ComponentServiceNg2.getDependencies(this.$scope.component.componentType, this.$scope.component.uniqueId).subscribe((response:Array<IDependenciesServerResponse>)=> {
                this.$scope.isLoading = false;
                this.AutomatedUpgradeService.openAutomatedUpgradeModal(response, this.$scope.component, false);
            });
        }

        this.$scope.handleCertification = (certifyComponent): void => {
            if (this.$scope.component.getComponentSubType() === ResourceType.VF || this.$scope.component.isService()) {
                this.ComponentServiceNg2.getDependencies(this.$scope.component.componentType, this.$scope.component.uniqueId).subscribe((response:Array<IDependenciesServerResponse>) => {
                    this.$scope.isLoading = false;

                    const isUpgradeNeeded = _.filter(response, (componentToUpgrade:IDependenciesServerResponse) => {
                        return componentToUpgrade.dependencies && componentToUpgrade.dependencies.length > 0;
                    });
                    if (isUpgradeNeeded.length === 0) {
                        this.onSuccessWithoutUpgradeNeeded();
                        return;
                    }
                    this.refreshDataAfterChangeLifecycleState(certifyComponent);
                    this.AutomatedUpgradeService.openAutomatedUpgradeModal(response, this.$scope.component, true);
                });
            } else {
                this.onSuccessWithoutUpgradeNeeded();
            }
        }

        this.$scope.disableMenuItems = () => {
            this.$scope.leftBarTabs.menuItems.forEach((item: MenuItem) => {
                item.isDisabled = (States.WORKSPACE_GENERAL !== item.state);
            });
        }

        this.$scope.enableMenuItems = () => {
            this.$scope.leftBarTabs.menuItems.forEach((item: MenuItem) => {
                item.isDisabled = false;
            });
        };


        this.$scope.startProgress = (message: string): void => {
            this.progressService.initCreateComponentProgress(this.$scope.component.uniqueId);
            this.$scope.isCreateProgress = true;
            this.$scope.progressMessage = message;
        };

        this.$scope.stopProgress = (): void => {
            this.$scope.isCreateProgress = false;
            this.progressService.deleteProgressValue(this.$scope.component.uniqueId);
        }

        this.$scope.updateBreadcrumbs = (component: Component): void => {
            // Update the components list for breadcrumbs
            const bcIdx = this.MenuHandler.findBreadcrumbComponentIndex(this.components, component);
            if (bcIdx !== -1) {
                this.components[bcIdx] = component;
                this.initBreadcrumbs();  // re-calculate breadcrumbs
            }
        };

        this.$scope.updateUnsavedFileFlag = (isUnsaved:boolean) => {
            this.$scope.unsavedFile = isUnsaved;
        };

    }

    private onSuccessWithoutUpgradeNeeded = (): void => {
        this.$scope.isLoading = false;
        this.Notification.success({
            message: this.$filter('translate')('SERVICE_CERTIFICATION_STATUS_TEXT'),
            title: this.$filter('translate')('SERVICE_CERTIFICATION_STATUS_TITLE')
        });
        this.initVersionObject();
        this.initChangeLifecycleStateButtons();
    }

    private refreshDataAfterChangeLifecycleState = (component:Component):void => {
        this.$scope.isLoading = false;
        this.$scope.mode = this.initViewMode();
        this.initChangeLifecycleStateButtons();
        this.initVersionObject();
        this.EventListenerService.notifyObservers(EVENTS.ON_LIFECYCLE_CHANGE, component);
    }

    private initAfterScope = (): void => {
        // In case user select csar from the onboarding modal, need to disable checkout and submit for testing.
        if (this.$state.params['disableButtons'] === true) {
            this.$scope.uploadFileChangedInGeneralTab();
        }
    };

    private initVersionObject = (): void => {
        this.$scope.versionsList = (this.$scope.component.getAllVersionsAsSortedArray()).reverse();
        this.$scope.changeVersion = {
            selectedVersion: _.find(this.$scope.versionsList, (versionObj) => {
                return versionObj.versionId === this.$scope.component.uniqueId;
            })
        };
    }

    private getNewComponentBreadcrumbItem = (): MenuItem => {
        let text = '';
        if (this.$scope.component.isResource() && (<Resource>this.$scope.component).isCsarComponent()) {
            text = this.$scope.component.getComponentSubType() + ': ' + this.$scope.component.name;
        } else {
            text = 'Create new ' + this.$state.params['type'];
        }
        return new MenuItem(text, null, States.WORKSPACE_GENERAL, 'goToState', [this.$state.params]);
    }

    private updateMenuItemByRole = (menuItems: any[], role: string) => {
        const tempMenuItems: any[] = new Array<any>();
        menuItems.forEach((item: any) => {
            //remove item if role is disabled
            if (!(item.disabledRoles && item.disabledRoles.indexOf(role) > -1)) {
                tempMenuItems.push(item);
            }
        });
        return tempMenuItems;
    }

	private updateMenuItemByCategory = (menuItems:Array<any>, category:string) => {
        let tempMenuItems:Array<any> = new Array<any>();
        menuItems.forEach((item:any) => {
            //update flag disabledCategory to true if category is disabled
            item.disabledCategory = false;
            if ((item.disabledCategories && item.disabledCategories.indexOf(category) > -1))
            {
                item.disabledCategory = true;
            }
            tempMenuItems.push(item);
        });
        return tempMenuItems;
    };


    private deleteArchiveCache = () => {
        this.cacheService.remove('archiveComponents'); // delete the cache to ensure the archive is reloaded from server
    }

    private initBreadcrumbs = () => {
        this.components = this.cacheService.get('breadcrumbsComponents');
        const breadcrumbsComponentsLvl = this.MenuHandler.generateBreadcrumbsModelFromComponents(this.components, this.$scope.component);

        if (this.$scope.isCreateMode()) {
            const createItem = this.getNewComponentBreadcrumbItem();
            if (!breadcrumbsComponentsLvl.menuItems) {
                breadcrumbsComponentsLvl.menuItems = [];
            }
            breadcrumbsComponentsLvl.menuItems.unshift(createItem);
            breadcrumbsComponentsLvl.selectedIndex = 0;
        }

        this.$scope.breadcrumbsModel = [breadcrumbsComponentsLvl, this.$scope.leftBarTabs];
    }

    private initMenuItems() {

        const inCreateMode = this.$scope.isCreateMode();
        this.$scope.leftBarTabs = new MenuItemGroup();
        let menuItemsObjects: any[] = this.updateMenuItemByRole(this.sdcMenu.component_workspace_menu_option[this.$scope.component.getComponentSubType()], this.role);
        if (this.$scope.component.getComponentSubType() === 'SERVICE') {
            const menuItemsObjectsCategory: any[] = this.updateMenuItemByCategory(menuItemsObjects, this.category);
            menuItemsObjects = menuItemsObjectsCategory;
        }

        // Only adding plugins to the workspace if they can be displayed for the current user role
        _.each(PluginsConfiguration.plugins, (plugin: Plugin) => {
            if (this.pluginsService.isPluginDisplayedInContext(plugin, this.role, this.$scope.component.getComponentSubType())) {
                menuItemsObjects.push({
                    text: plugin.pluginDisplayOptions['context'].displayName,
                    action: 'onMenuItemPressed',
                    state: 'workspace.plugins',
                    params: {path: plugin.pluginStateUrl}
                });
            }
        });

        this.$scope.leftBarTabs.menuItems = menuItemsObjects.map((item: MenuItem) => {
            const menuItem = new MenuItem(item.text, item.callback, item.state, item.action, item.params, item.blockedForTypes, item.disabledCategory);
            if (menuItem.params) {
                menuItem.params.state = menuItem.state;
            }
            else {
                menuItem.params = {state: menuItem.state};
            }
            menuItem.callback = () => this.$scope[menuItem.action](menuItem.state, menuItem.params);
            menuItem.isDisabled = (inCreateMode && States.WORKSPACE_GENERAL !== menuItem.state) ||
                (States.WORKSPACE_DEPLOYMENT === menuItem.state && this.$scope.component.modules
                && this.$scope.component.modules.length === 0 && this.$scope.component.isResource()) ||
                (menuItem.disabledCategory === true);
            return menuItem;
        });

        if (this.cacheService.get('breadcrumbsComponents')) {
            this.initBreadcrumbs();
        }
        else {
            this.initBreadcrumbsComponents();
        }
    }

    private showSuccessNotificationMessage = ():void => {
        this.Notification.success({
            message: this.$filter('translate')('IMPORT_VF_MESSAGE_CREATE_FINISHED_DESCRIPTION'),
            title: this.$filter('translate')('IMPORT_VF_MESSAGE_CREATE_FINISHED_TITLE')
        });
    }

    private setWorkspaceButtonState = (newState: boolean, callback?: Function) => {
        this.$scope.unsavedChanges = newState;
        this.$scope.unsavedChangesCallback = callback;
    }

    private initBreadcrumbsComponents = (): void => {
        let breadcrumbsComponentsObservable;
        if (this.$stateParams.previousState === 'dashboard') {
            breadcrumbsComponentsObservable = this.homeService.getAllComponents(true);
        } else if (this.$stateParams.previousState === 'catalog') {
            breadcrumbsComponentsObservable = this.catalogService.getCatalog();
        } else {
            this.cacheService.remove('breadcrumbsComponentsState');
            this.cacheService.remove('breadcrumbsComponents');
            return;
        }
        breadcrumbsComponentsObservable.subscribe((components) => {
            this.cacheService.set('breadcrumbsComponentsState', this.$stateParams.previousState);
            this.cacheService.set('breadcrumbsComponents', components);
            this.initBreadcrumbs();
        });

    }

    private verifyIfDependenciesExist(): void {
        let containsDependencies = [];
        if (this.$scope.component.componentType && this.$scope.component.uniqueId &&
            this.$scope.component.lifecycleState === 'CERTIFIED' && (this.$scope.component.isService() || this.$scope.component.getComponentSubType() === 'VF')) {
            this.ComponentServiceNg2.getDependencies(this.$scope.component.componentType, this.$scope.component.uniqueId).subscribe((response: IDependenciesServerResponse[]) => {
                containsDependencies = response.filter((version) => version.dependencies);
                if (containsDependencies.length > 0) {
                    this.$scope.hasNoDependencies = false;
                } else {
                    this.$scope.hasNoDependencies = true;
                }
            });
        }
    }
}
