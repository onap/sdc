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
import {IUserProperties, IAppMenu, Resource, Component} from "app/models";
import {
    WorkspaceMode, ComponentFactory, ChangeLifecycleStateHandler, Role, ComponentState, MenuItemGroup, MenuHandler,
    MenuItem, ModalsHandler, States, EVENTS, CHANGE_COMPONENT_CSAR_VERSION_FLAG, ResourceType
} from "app/utils";
import {
    EventListenerService,
    EntityService,
    ProgressService,
    CacheService,
    LeftPaletteLoaderService
} from "app/services";
import {FileUploadModel} from "../../directives/file-upload/file-upload";


export interface IWorkspaceViewModelScope extends ng.IScope {

    isLoading:boolean;
    isCreateProgress:boolean;
    component:Component;
    originComponent:Component;
    componentType:string;
    importFile:any;
    leftBarTabs:MenuItemGroup;
    isNew:boolean;
    isFromImport:boolean;
    isValidForm:boolean;
    mode:WorkspaceMode;
    breadcrumbsModel:Array<MenuItemGroup>;
    sdcMenu:IAppMenu;
    changeLifecycleStateButtons:any;
    version:string;
    versionsList:Array<any>;
    changeVersion:any;
    isComposition:boolean;
    isDeployment:boolean;
    $state:ng.ui.IStateService;
    user:IUserProperties;
    thirdParty:boolean;
    disabledButtons:boolean;
    menuComponentTitle:string;
    progressService:ProgressService;
    progressMessage:string;
    // leftPanelComponents:Array<Models.Components.Component>; //this is in order to load the left panel once, and not wait long time when moving to composition

    showChangeStateButton():boolean;
    getComponent():Component;
    setComponent(component:Component):void;
    onMenuItemPressed(state:string):ng.IPromise<boolean>;
    save():ng.IPromise<boolean>;
    setValidState(isValid:boolean):void;
    revert():void;
    changeLifecycleState(state:string):void;
    enabledTabs():void
    isDesigner():boolean;
    isViewMode():boolean;
    isEditMode():boolean;
    isCreateMode():boolean;
    isDisableMode():boolean;
    showFullIcons():boolean;
    goToBreadcrumbHome():void;
    onVersionChanged(selectedId:string):void;
    getLatestVersion():void;
    getStatus():string;
    showLifecycleIcon():boolean;
    updateSelectedMenuItem(state:string):void;
    uploadFileChangedInGeneralTab():void;
    updateMenuComponentName(ComponentName:string):void;
    getTabTitle():string;
    reload(component:Component):void;
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
        'ModalsHandler',
        'LeftPaletteLoaderService',
        '$filter',
        'EventListenerService',
        'Sdc.Services.EntityService',
        'Notification',
        '$stateParams',
        'Sdc.Services.ProgressService'
    ];

    constructor(private $scope:IWorkspaceViewModelScope,
                private injectComponent:Component,
                private ComponentFactory:ComponentFactory,
                private $state:ng.ui.IStateService,
                private sdcMenu:IAppMenu,
                private $q:ng.IQService,
                private MenuHandler:MenuHandler,
                private cacheService:CacheService,
                private ChangeLifecycleStateHandler:ChangeLifecycleStateHandler,
                private ModalsHandler:ModalsHandler,
                private LeftPaletteLoaderService:LeftPaletteLoaderService,
                private $filter:ng.IFilterService,
                private EventListenerService:EventListenerService,
                private EntityService:EntityService,
                private Notification:any,
                private $stateParams:any,
                private progressService:ProgressService) {

        this.initScope();
        this.initAfterScope();
        this.$scope.updateSelectedMenuItem(this.$state.current.name);
    }

    private role:string;
    private components:Array<Component>;

    private initViewMode = ():WorkspaceMode => {
        let mode = WorkspaceMode.VIEW;

        if (!this.$state.params['id']) {   //&& !this.$state.params['vspComponent']
            mode = WorkspaceMode.CREATE;
        } else {
            if (this.$scope.component.lifecycleState === ComponentState.NOT_CERTIFIED_CHECKOUT &&
                this.$scope.component.lastUpdaterUserId === this.cacheService.get("user").userId) {
                if ((this.$scope.component.isService() || this.$scope.component.isResource()) && this.role == Role.DESIGNER) {
                    mode = WorkspaceMode.EDIT;
                }
            }
        }
        return mode;
    };

    private initChangeLifecycleStateButtons = ():void => {
        let state = this.$scope.component.isService() && (Role.OPS == this.role || Role.GOVERNOR == this.role) ? this.$scope.component.distributionStatus : this.$scope.component.lifecycleState;
        this.$scope.changeLifecycleStateButtons = this.sdcMenu.roles[this.role].changeLifecycleStateButtons[state];
    };

    private isNeedSave = ():boolean => {
        return this.$scope.isEditMode() &&
            this.$state.current.data && this.$state.current.data.unsavedChanges;
    };

    private initLeftPalette = ():void => {
        this.LeftPaletteLoaderService.loadLeftPanel(this.$scope.component);
    };

    private initScope = ():void => {

        this.$scope.component = this.injectComponent;
        this.initLeftPalette();
        this.$scope.menuComponentTitle = this.$scope.component.name;
        this.$scope.disabledButtons = false;
        this.$scope.originComponent = this.ComponentFactory.createComponent(this.$scope.component);
        this.$scope.componentType = this.$scope.component.componentType;
        this.$scope.version = this.cacheService.get('version');
        this.$scope.user = this.cacheService.get("user");
        this.role = this.$scope.user.role;
        this.$scope.mode = this.initViewMode();
        this.$scope.isValidForm = true;
        this.initChangeLifecycleStateButtons();
        this.initVersionObject();
        this.$scope.$state = this.$state;
        this.$scope.isLoading = false;
        this.$scope.isComposition = (this.$state.current.name.indexOf(States.WORKSPACE_COMPOSITION) > -1);
        this.$scope.isDeployment = this.$state.current.name == States.WORKSPACE_DEPLOYMENT;
        this.$scope.progressService = this.progressService;

        this.$scope.getComponent = ():Component => {
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

        this.$scope.uploadFileChangedInGeneralTab = ():void => {
            // In case user select browse file, and in update mode, need to disable submit for testing and checkin buttons.
            if (this.$scope.isEditMode() && this.$scope.component.isResource() && (<Resource>this.$scope.component).resourceType == ResourceType.VF) {
                this.$scope.disabledButtons = true;
            }
        };

        this.$scope.onMenuItemPressed = (state:string):ng.IPromise<boolean> => {
            let deferred = this.$q.defer();
            let goToState = ():void => {
                this.$scope.updateSelectedMenuItem(state);
                this.$state.go(state, {
                    id: this.$scope.component.uniqueId,
                    type: this.$scope.component.componentType.toLowerCase(),
                    components: this.components
                });
                deferred.resolve(true);
            };
            if (this.isNeedSave()) {
                if (this.$scope.isValidForm) {
                    this.$scope.save().then(goToState);
                } else {
                    console.log('form is not valid');
                    deferred.reject(false);
                }
            } else if (this.$scope.isEditMode() && //this is a workaround for amdocs - we need to get the artifact in order to avoid saving the vf when moving from their tabs
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
            if (this.$state.current.data && this.$state.current.data.unsavedChanges) {
                this.$scope.changeVersion.selectedVersion = _.find(this.$scope.versionsList, (versionObj)=> {
                   return versionObj.versionId === this.$scope.component.uniqueId;
                });
            }
            this.$scope.isLoading = true;
            this.$state.go(this.$state.current.name, {
                id: selectedId,
                type: this.$scope.componentType.toLowerCase(),
                mode: WorkspaceMode.VIEW,
                components: this.$state.params['components']
            }, {reload: true});

        };

        this.$scope.getLatestVersion = ():void => {
            this.$scope.onVersionChanged(_.first(this.$scope.versionsList).versionId);
        };

        this.$scope.save = (state?:string):ng.IPromise<boolean> => {
            this.EventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_SAVE_BUTTON_CLICK);

            this.progressService.initCreateComponentProgress(this.$scope.component.uniqueId);

            let deferred = this.$q.defer();
            let modalInstance:ng.ui.bootstrap.IModalServiceInstance;

            let onFailed = () => {
                _.first(this.$scope.leftBarTabs.menuItems).isDisabled = false;//enabled click on general tab (DE246274)
                this.EventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_SAVE_BUTTON_ERROR);
                this.progressService.deleteProgressValue(this.$scope.component.uniqueId);
                modalInstance && modalInstance.close();  // Close the modal in case it is opened.
                this.$scope.component.tags = _.without(this.$scope.component.tags, this.$scope.component.name);// for fix DE246217
                this.$scope.isCreateProgress = false;
                this.$scope.isLoading = false; // stop the progress.

                this.$scope.setValidState(true);  // Set the form valid (if sent form is valid, the error from server).
                if (!this.$scope.isCreateMode()) {
                    this.$scope.component = this.ComponentFactory.createComponent(this.$scope.originComponent); // Set the component back to the original.
                    this.enableMenuItems();  // Enable the menu items (left tabs), so user can press on them.
                    this.$scope.disabledButtons = false;  // Enable "submit for testing" & checking buttons.
                }

                deferred.reject(false);
            };

            let onSuccessCreate = (component:Component) => {

                this.showSuccessNotificationMessage();
                this.progressService.deleteProgressValue(this.$scope.component.uniqueId);
                //update components for breadcrumbs
                this.components.unshift(component);
                this.$state.go(States.WORKSPACE_GENERAL, {
                    id: component.uniqueId,
                    type: component.componentType.toLowerCase(),
                    components: this.components
                });

                deferred.resolve(true);
            };

            let onSuccessUpdate = (component:Component) => {
                this.$scope.isCreateProgress = false;
                this.$scope.disabledButtons = false;
                this.showSuccessNotificationMessage();
                this.progressService.deleteProgressValue(this.$scope.component.uniqueId);

                // Stop the circle loader.
                this.$scope.isLoading = false;

                component.tags = _.reject(component.tags, (item)=> {
                    return item === component.name
                });

                // Update the components
                this.$scope.component = component;
                this.$scope.originComponent = this.ComponentFactory.createComponent(this.$scope.component);

                //update components for breadcrumbs
                this.components.unshift(component);

                // Enable left tags
                this.$scope.enabledTabs();


                if (this.$state.current.data) {
                    this.$state.current.data.unsavedChanges = false;
                }

                deferred.resolve(true);
            };

            if (this.$scope.isCreateMode()) {
                this.$scope.progressMessage = "Creating Asset...";
                // CREATE MODE
                this.$scope.isCreateProgress = true;

                _.first(this.$scope.leftBarTabs.menuItems).isDisabled = true;//disabled click on general tab (DE246274)

                // Start creating the component
                this.ComponentFactory.createComponentOnServer(this.$scope.component).then(onSuccessCreate, onFailed);

                // In case we import CSAR. Notify user that import VF will take long time (the create is performed in the background).
                if (this.$scope.component.isResource() && (<Resource>this.$scope.component).csarUUID) {
                    this.Notification.info({
                        message: this.$filter('translate')("IMPORT_VF_MESSAGE_CREATE_TAKES_LONG_TIME_DESCRIPTION"),
                        title: this.$filter('translate')("IMPORT_VF_MESSAGE_CREATE_TAKES_LONG_TIME_TITLE")
                    });
                }
            } else {
                // UPDATE MODE
                this.$scope.isCreateProgress = true;
                this.$scope.progressMessage = "Updating Asset...";
                this.disableMenuItems();


                // Work around to change the csar version
                if (this.cacheService.get(CHANGE_COMPONENT_CSAR_VERSION_FLAG)) {
                    (<Resource>this.$scope.component).csarVersion = this.cacheService.get(CHANGE_COMPONENT_CSAR_VERSION_FLAG);
                    this.cacheService.remove(CHANGE_COMPONENT_CSAR_VERSION_FLAG);
                }

                this.$scope.component.updateComponent().then(onSuccessUpdate, onFailed);
            }
            return deferred.promise;
        };

        this.$scope.revert = ():void => {
            //in state of import file leave the file in place
            if (this.$scope.component.isResource() && (<Resource>this.$scope.component).importedFile) {
                let tempFile:FileUploadModel = (<Resource>this.$scope.component).importedFile;
                this.$scope.component = this.ComponentFactory.createComponent(this.$scope.originComponent);
                (<Resource>this.$scope.component).importedFile = tempFile;
            } else {
                this.$scope.component = this.ComponentFactory.createComponent(this.$scope.originComponent);
            }
            this.EventListenerService.notifyObservers(EVENTS.ON_REVERT);
        };

        this.$scope.changeLifecycleState = (state:string):void => {
            if (this.isNeedSave() && state !== 'deleteVersion') {
                this.$scope.save().then(() => {
                    changeLifecycleState(state);
                })
            } else {
                changeLifecycleState(state);
            }
        };

        let defaultActionAfterChangeLifecycleState = ():void => {
            if (this.$state.current.data && this.$state.current.data.unsavedChanges) {
                this.$state.current.data.unsavedChanges = false;
            }
            this.$state.go('dashboard');
        };

        let changeLifecycleState = (state:string) => {
            if ('monitor' === state) {
                this.$state.go('workspace.distribution');
                return;
            }

            let data = this.$scope.changeLifecycleStateButtons[state];
            let onSuccess = (component:Component, url:string):void => {
                //Updating the component from server response

                //the server returns only metaData (small component) except checkout (Full component)  ,so we update only the statuses of distribution & lifecycle
                this.$scope.component.lifecycleState = component.lifecycleState;
                this.$scope.component.distributionStatus = component.distributionStatus;

                switch (url) {
                    case 'lifecycleState/CHECKOUT':
                        // only checkOut get the full component from server
                        //   this.$scope.component = component;
                        // Work around to change the csar version
                        if (this.cacheService.get(CHANGE_COMPONENT_CSAR_VERSION_FLAG)) {
                            (<Resource>this.$scope.component).csarVersion = this.cacheService.get(CHANGE_COMPONENT_CSAR_VERSION_FLAG);
                        }

                        //when checking out a minor version uuid remains
                        let bcComponent:Component = _.find(this.components, (item) => {
                            return item.uuid === component.uuid;
                        });
                        if (bcComponent) {
                            this.components[this.components.indexOf(bcComponent)] = component;
                        } else {
                            //when checking out a major(certified) version
                            this.components.unshift(component);
                        }
                        // this.$state.go(this.$state.current.name, {
                        //     id: component.uniqueId,
                        //     type: component.componentType.toLowerCase(),
                        //     components: this.components
                        // });
                        this.$scope.mode = this.initViewMode();
                        this.initChangeLifecycleStateButtons();
                        this.initVersionObject();
                        this.$scope.isLoading = false;
                        this.EventListenerService.notifyObservers(EVENTS.ON_CHECKOUT, component);
                        this.Notification.success({
                            message: this.$filter('translate')("CHECKOUT_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("CHECKOUT_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    case 'lifecycleState/CHECKIN':
                        defaultActionAfterChangeLifecycleState();
                        this.Notification.success({
                            message: this.$filter('translate')("CHECKIN_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("CHECKIN_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    case 'lifecycleState/UNDOCHECKOUT':
                        defaultActionAfterChangeLifecycleState();
                        this.Notification.success({
                            message: this.$filter('translate')("DELETE_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("DELETE_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    case 'lifecycleState/certificationRequest':
                        defaultActionAfterChangeLifecycleState();
                        this.Notification.success({
                            message: this.$filter('translate')("SUBMIT_FOR_TESTING_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("SUBMIT_FOR_TESTING_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    //Tester Role
                    case 'lifecycleState/failCertification':
                        defaultActionAfterChangeLifecycleState();
                        this.Notification.success({
                            message: this.$filter('translate')("REJECT_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("REJECT_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    case 'lifecycleState/certify':
                        defaultActionAfterChangeLifecycleState();
                        this.Notification.success({
                            message: this.$filter('translate')("ACCEPT_TESTING_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("ACCEPT_TESTING_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    //DE203504 Bug Fix Start
                    case 'lifecycleState/startCertification':
                        this.initChangeLifecycleStateButtons();
                        this.Notification.success({
                            message: this.$filter('translate')("START_TESTING_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("START_TESTING_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    case  'lifecycleState/cancelCertification':
                        this.initChangeLifecycleStateButtons();
                        this.Notification.success({
                            message: this.$filter('translate')("CANCEL_TESTING_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("CANCEL_TESTING_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    //Ops Role
                    case  'distribution/PROD/activate':
                        this.initChangeLifecycleStateButtons();
                        this.Notification.success({
                            message: this.$filter('translate')("DISTRIBUTE_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("DISTRIBUTE_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    //Governor Role
                    case  'distribution-state/reject':
                        this.initChangeLifecycleStateButtons();
                        this.Notification.success({
                            message: this.$filter('translate')("REJECT_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("REJECT_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    case  'distribution-state/approve':
                        this.initChangeLifecycleStateButtons();
                        this.$state.go('catalog');
                        this.Notification.success({
                            message: this.$filter('translate')("APPROVE_SUCCESS_MESSAGE_TEXT"),
                            title: this.$filter('translate')("APPROVE_SUCCESS_MESSAGE_TITLE")
                        });
                        break;
                    //DE203504 Bug Fix End

                    default :
                        defaultActionAfterChangeLifecycleState();

                }
                if (data.url != 'lifecycleState/CHECKOUT') {
                    this.$scope.isLoading = false;
                }
            };
            //this.$scope.isLoading = true;
            this.ChangeLifecycleStateHandler.changeLifecycleState(this.$scope.component, data, this.$scope, onSuccess);
        };

        this.$scope.enabledTabs = ():void => {
            this.$scope.leftBarTabs.menuItems.forEach((item:MenuItem) => {
                item.isDisabled = false;
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

        this.$scope.showFullIcons = ():boolean => {
            //we show revert and save icons only in general view
            return this.$state.current.name === States.WORKSPACE_GENERAL;
        };

        this.$scope.isCreateMode = ():boolean => {
            return this.$scope.mode === WorkspaceMode.CREATE;
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

        this.$scope.showChangeStateButton = ():boolean => {
            let result:boolean = true;
            if (!this.$scope.component.isLatestVersion() && Role.OPS != this.role && Role.GOVERNOR != this.role) {
                result = false;
            }
            if (ComponentState.NOT_CERTIFIED_CHECKOUT === this.$scope.component.lifecycleState && this.$scope.isViewMode()) {
                result = false;
            }
            if (ComponentState.CERTIFIED != this.$scope.component.lifecycleState &&
                (Role.OPS == this.role || Role.GOVERNOR == this.role)) {
                result = false;
            }
            return result;
        };

        this.$scope.updateSelectedMenuItem = (state:string):void => {
            let stateArray:Array<string> = state.split('.');
            let stateWithoutInternalNavigate:string = stateArray[0] + '.' + stateArray[1];
            let selectedItem:MenuItem = _.find(this.$scope.leftBarTabs.menuItems, (item:MenuItem) => {
                return _.startsWith(item.state, stateWithoutInternalNavigate);
            });
            this.$scope.leftBarTabs.selectedIndex = selectedItem ? this.$scope.leftBarTabs.menuItems.indexOf(selectedItem) : 0;
        };

        this.$scope.$watch('$state.current.name', (newVal:string):void => {
            if (newVal) {
                this.$scope.isComposition = (newVal.indexOf(States.WORKSPACE_COMPOSITION) > -1);
                this.$scope.isDeployment = newVal == States.WORKSPACE_DEPLOYMENT;
            }
        });

        this.$scope.getTabTitle = ():string => {
            return this.$scope.leftBarTabs.menuItems.find((menuItem:MenuItem)=>{
                return menuItem.state == this.$scope.$state.current.name;
            }).text;
        };

        this.$scope.reload = (component:Component):void => {
            this.$state.go(this.$state.current.name,{id:component.uniqueId},{reload:true});
        };

    };

    private initAfterScope = ():void => {
        // In case user select csar from the onboarding modal, need to disable checkout and submit for testing.
        if (this.$state.params['disableButtons'] === true) {
            this.$scope.uploadFileChangedInGeneralTab();
        }
    };

    private initVersionObject = ():void => {
        this.$scope.versionsList = (this.$scope.component.getAllVersionsAsSortedArray()).reverse();
        this.$scope.changeVersion = {
            selectedVersion: _.find(this.$scope.versionsList, (versionObj)=> {
                return versionObj.versionId === this.$scope.component.uniqueId;
            })
        };
    };

    private getNewComponentBreadcrumbItem = ():MenuItem => {
        let text = "";
        if (this.$scope.component.isResource() && (<Resource>this.$scope.component).isCsarComponent()) {
            text = this.$scope.component.getComponentSubType() + ': ' + this.$scope.component.name;
        } else {
            text = 'Create new ' + this.$state.params['type'];
        }
        return new MenuItem(text, null, States.WORKSPACE_GENERAL, 'goToState', [this.$state.params]);
    };

    private updateMenuItemByRole = (menuItems:Array<MenuItem>, role:string) => {
        let tempMenuItems:Array<MenuItem> = new Array<MenuItem>();
        menuItems.forEach((item:MenuItem) => {
            //remove item if role is disabled
            if (!(item.disabledRoles && item.disabledRoles.indexOf(role) > -1)) {
                tempMenuItems.push(item);
            }
        });
        return tempMenuItems;
    };

    private initBreadcrumbs = () => {
        this.components = this.cacheService.get('breadcrumbsComponents');
        let breadcrumbsComponentsLvl = this.MenuHandler.generateBreadcrumbsModelFromComponents(this.components, this.$scope.component);

        if (this.$scope.isCreateMode()) {
            let createItem = this.getNewComponentBreadcrumbItem();
            if (!breadcrumbsComponentsLvl.menuItems) {
                breadcrumbsComponentsLvl.menuItems = [];
            }
            breadcrumbsComponentsLvl.menuItems.unshift(createItem);
            breadcrumbsComponentsLvl.selectedIndex = 0;
        }

        this.$scope.breadcrumbsModel = [breadcrumbsComponentsLvl, this.$scope.leftBarTabs];
    };

    private initMenuItems() {

        let inCreateMode = this.$scope.isCreateMode();
        this.$scope.leftBarTabs = new MenuItemGroup();
        this.$scope.leftBarTabs.menuItems = this.updateMenuItemByRole(this.sdcMenu.component_workspace_menu_option[this.$scope.component.getComponentSubType()], this.role);

        this.$scope.leftBarTabs.menuItems.forEach((item:MenuItem) => {
            item.params = [item.state];
            item.callback = this.$scope.onMenuItemPressed;
            item.isDisabled = (inCreateMode && States.WORKSPACE_GENERAL != item.state) ||
                (States.WORKSPACE_DEPLOYMENT === item.state && this.$scope.component.groups && this.$scope.component.groups.length === 0 && this.$scope.component.isResource());
        });

        if (this.cacheService.get('breadcrumbsComponents')) {
            this.initBreadcrumbs();
        } else {
            let onSuccess = (components:Array<Component>) => {
                this.cacheService.set('breadcrumbsComponents', components);
                this.initBreadcrumbs();
            };
            this.EntityService.getCatalog().then(onSuccess); //getAllComponents() doesnt return components from catalog
        }
    }

    private disableMenuItems() {
        this.$scope.leftBarTabs.menuItems.forEach((item:MenuItem) => {
            item.params = [item.state];
            item.callback = this.$scope.onMenuItemPressed;
            item.isDisabled = (States.WORKSPACE_GENERAL != item.state);
        });
    }

    private enableMenuItems() {
        this.$scope.leftBarTabs.menuItems.forEach((item:MenuItem) => {
            item.params = [item.state];
            item.callback = this.$scope.onMenuItemPressed;
            item.isDisabled = false;
        });
    }

    private showSuccessNotificationMessage = ():void => {
        this.Notification.success({
            message: this.$filter('translate')("IMPORT_VF_MESSAGE_CREATE_FINISHED_DESCRIPTION"),
            title: this.$filter('translate')("IMPORT_VF_MESSAGE_CREATE_FINISHED_TITLE")
        });
    };

}


