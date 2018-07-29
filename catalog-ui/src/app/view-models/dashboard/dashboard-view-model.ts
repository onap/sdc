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
import {IConfigRoles, IAppConfigurtaion, IAppMenu, IUserProperties, Component} from "app/models";
import {EntityService, SharingService, CacheService} from "app/services";
import {ComponentType, ResourceType, MenuHandler, ModalsHandler, ChangeLifecycleStateHandler, SEVERITY, ComponentFactory, CHANGE_COMPONENT_CSAR_VERSION_FLAG} from "app/utils";
import {IClientMessageModalModel} from "../modals/message-modal/message-client-modal/client-message-modal-view-model";
import {UserService} from "../../ng2/services/user.service";


export interface IDashboardViewModelScope extends ng.IScope {

    isLoading:boolean;
    numberOfItemToDisplay:number;
    components:Array<Component>;
    folders:FoldersMenu;
    roles:IConfigRoles;
    user:IUserProperties;
    sdcConfig:IAppConfigurtaion;
    sdcMenu:IAppMenu;
    sharingService:SharingService;
    showTutorial:boolean;
    isFirstTime:boolean;
    version:string;
    filterParams:DashboardFilter;
    vfcmtType:string;

    changeFilterParams():void;
    updateSearchTerm(newTerm:string):void;
    onImportVfc(file:any):void;
    onImportVf(file:any):void;
    openCreateModal(componentType:ComponentType, importedFile:any):void;
    openWhatsNewModal(version:string):void;
    openDesignerModal(isResource:boolean, uniqueId:string):void;
    setSelectedFolder(folderItem:FoldersItemsMenu):void;
    entitiesCount(folderItem:FoldersItemsMenu):number;
    getCurrentFolderDistributed():Array<Component>;
    changeLifecycleState(entity:any, data:any):void;
    goToComponent(component:Component):void;
    raiseNumberOfElementToDisplay():void;
    wizardDebugEdit:Function;
    notificationIconCallback:Function;
}

interface ICheckboxesFilter {
    // Statuses
    selectedStatuses:Array<string>;
    // distributed
    distributed:Array<string>;
}

export interface IItemMenu {

}

export interface IMenuItemProperties {
    text:string;
    group:string;
    state:string;
    dist:string;
    groupname:string;
    states:Array<any>;
}

export interface IQueryFilterParams {
    'filter.term': string;
    'filter.distributed': string;
    'filter.status': string
}


export class DashboardFilter {
    searchTerm: string;
    checkboxes: ICheckboxesFilter;

    constructor(params = {}) {
        this.searchTerm = params['filter.term'] || "";
        this.checkboxes = {
            selectedStatuses : params['filter.status']? params['filter.status'].split(',') : [],
            distributed : params['filter.distributed']? params['filter.distributed'].split(',') : []
        };
    }

    public toParam = ():IQueryFilterParams => {
        return {
            'filter.term': this.searchTerm,
            'filter.distributed': this.checkboxes && this.checkboxes.distributed.join(',') || null,
            'filter.status': this.checkboxes && this.checkboxes.selectedStatuses.join(',') || null
        };
    }

}

export class FoldersMenu {

    private _folders:Array<FoldersItemsMenu> = [];

    constructor(folders:Array<IMenuItemProperties>) {
        let self = this;
        folders.forEach(function (folder:IMenuItemProperties) {
            if (folder.groupname) {
                self._folders.push(new FoldersItemsMenuGroup(folder));
            } else {
                self._folders.push(new FoldersItemsMenu(folder));
            }
        });
        self._folders[0].setSelected(true);
    }

    public getFolders = ():Array<FoldersItemsMenu> => {
        return this._folders;
    };

    public getCurrentFolder = ():FoldersItemsMenu => {
        let menuItem:FoldersItemsMenu = undefined;
        this.getFolders().forEach(function (tmpFolder:FoldersItemsMenu) {
            if (tmpFolder.isSelected()) {
                menuItem = tmpFolder;
            }
        });
        return menuItem;
    };

    public setSelected = (folder:FoldersItemsMenu):void => {
        this.getFolders().forEach(function (tmpFolder:FoldersItemsMenu) {
            tmpFolder.setSelected(false);
        });
        folder.setSelected(true);
    }

}

export class FoldersItemsMenu implements IItemMenu {

    public text:string;
    public group:string;
    public state:string;
    public dist:string;
    public states:Array<any>;

    private selected:boolean = false;

    constructor(menuProperties:IMenuItemProperties) {
        this.text = menuProperties.text;
        this.group = menuProperties.group;
        this.state = menuProperties.state;
        this.states = menuProperties.states;
        this.dist = menuProperties.dist;
    }

    public isSelected = ():boolean => {
        return this.selected;
    };

    public setSelected = (value:boolean):void => {
        this.selected = value;
    };

    public isGroup = ():boolean => {
        return false;
    }

}

export class FoldersItemsMenuGroup extends FoldersItemsMenu {

    public groupname:string;

    constructor(menuProperties:IMenuItemProperties) {
        super(menuProperties);
        this.groupname = menuProperties.groupname;
    }

    public isGroup = ():boolean => {
        return true;
    }

}

export class DashboardViewModel {
    static '$inject' = [
        '$scope',
        '$filter',
        'Sdc.Services.EntityService',
        '$http',
        'sdcConfig',
        'sdcMenu',
        '$state',
        '$stateParams',
        'UserServiceNg2',
        'Sdc.Services.SharingService',
        'Sdc.Services.CacheService',
        '$q',
        'ComponentFactory',
        'ChangeLifecycleStateHandler',
        'ModalsHandler',
        'MenuHandler'
    ];

    private components:Array<Component>;

    constructor(private $scope:IDashboardViewModelScope,
                private $filter:ng.IFilterService,
                private entityService:EntityService,
                private $http:ng.IHttpService,
                private sdcConfig:IAppConfigurtaion,
                private sdcMenu:IAppMenu,
                private $state:ng.ui.IStateService,
                private $stateParams:any,
                private userService:UserService,
                private sharingService:SharingService,
                private cacheService:CacheService,
                private $q:ng.IQService,
                private ComponentFactory:ComponentFactory,
                private ChangeLifecycleStateHandler:ChangeLifecycleStateHandler,
                private ModalsHandler:ModalsHandler,
                private MenuHandler:MenuHandler) {
        this.initScope();
        this.initFolders();
        this.initEntities();

        if (this.$stateParams) {

            if (this.$state.params.folder) {
                let self = this;
                let folderName = this.$state.params.folder.replaceAll("_", " ");

                this.$scope.folders.getFolders().forEach(function (tmpFolder:FoldersItemsMenu) {
                    if (tmpFolder.text === folderName) {
                        self.$scope.setSelectedFolder(tmpFolder);
                    }
                });
            }

            // Show the tutorial if needed when the dashboard page is opened.<script src="bower_components/angular-filter/dist/angular-filter.min.js"></script>
            // This is called from the welcome page.
            else if (this.$stateParams.show === 'tutorial') {
                this.$scope.showTutorial = true;
                this.$scope.isFirstTime = true;
            }
        }
    }

    private initFolders = ():void => {
        if (this.$scope.user) {
            this.$scope.folders = new FoldersMenu(this.$scope.roles[this.$scope.user.role].folder);
        }
    };

    private initScope = ():void => {
        let self = this;

        this.$scope.version = this.cacheService.get('version');
        this.$scope.sharingService = this.sharingService;
        this.$scope.numberOfItemToDisplay = 0;
        this.$scope.isLoading = false;
        this.$scope.sdcConfig = this.sdcConfig;
        this.$scope.sdcMenu = this.sdcMenu;
        this.$scope.user = this.userService.getLoggedinUser();
        this.$scope.roles = this.sdcMenu.roles;
        this.$scope.showTutorial = false;
        this.$scope.isFirstTime = false;
        this.$scope.vfcmtType = ResourceType.VFCMT;
        this.$scope.filterParams = new DashboardFilter(this.$state.params);

        // Open onboarding modal
        this.$scope.notificationIconCallback = ():void => {
            this.ModalsHandler.openOnboadrdingModal('Import').then((result)=> {
                //OK
                if(!result.previousComponent || result.previousComponent.csarVersion != result.componentCsar.csarVersion) {
                    this.cacheService.set(CHANGE_COMPONENT_CSAR_VERSION_FLAG, result.componentCsar.csarVersion);
                }

                this.$state.go('workspace.general', {
                    id: result.previousComponent && result.previousComponent.uniqueId,
                    componentCsar: result.componentCsar,
                    type: result.type
                });
            }, ()=> {
                // ERROR
            });
        };

        this.$scope.onImportVf = (file:any):void => {
            if (file && file.filename) {
                // Check that the file has valid extension.
                let fileExtension:string = file.filename.split(".").pop();
                if (this.sdcConfig.csarFileExtension.indexOf(fileExtension.toLowerCase()) !== -1) {
                    this.$state.go('workspace.general', {
                        type: ComponentType.RESOURCE.toLowerCase(),
                        importedFile: file,
                        resourceType: ResourceType.VF
                    });
                } else {
                    let data:IClientMessageModalModel = {
                        title: self.$filter('translate')("NEW_SERVICE_RESOURCE_ERROR_VALID_CSAR_EXTENSIONS_TITLE"),
                        message: self.$filter('translate')("NEW_SERVICE_RESOURCE_ERROR_VALID_CSAR_EXTENSIONS", "{'extensions': '" + this.sdcConfig.csarFileExtension + "'}"),
                        severity: SEVERITY.ERROR
                    };
                    this.ModalsHandler.openClientMessageModal(data);
                }
            }
        };

        this.$scope.onImportVfc = (file:any):void => {
            if (file && file.filename) {
                // Check that the file has valid extension.
                let fileExtension:string = file.filename.split(".").pop();
                if (this.sdcConfig.toscaFileExtension.indexOf(fileExtension.toLowerCase()) !== -1) {
                    this.$state.go('workspace.general', {
                        type: ComponentType.RESOURCE.toLowerCase(),
                        importedFile: file,
                        resourceType: ResourceType.VFC
                    });
                } else {
                    let data:IClientMessageModalModel = {
                        title: self.$filter('translate')("NEW_SERVICE_RESOURCE_ERROR_VALID_TOSCA_EXTENSIONS_TITLE"),
                        message: self.$filter('translate')("NEW_SERVICE_RESOURCE_ERROR_VALID_TOSCA_EXTENSIONS", "{'extensions': '" + this.sdcConfig.toscaFileExtension + "'}"),
                        severity: SEVERITY.ERROR
                    };
                    this.ModalsHandler.openClientMessageModal(data);
                }
            }
        };

        this.$scope.openCreateModal = (componentType:string, importedFile:any):void => {
            if (importedFile) {
                this.initEntities(true); // Return from import
            } else {
                this.$state.go('workspace.general', {type: componentType.toLowerCase()});
            }

        };

        this.$scope.createPNF = ():void => {
            this.$state.go('workspace.general', {
                type: ComponentType.RESOURCE.toLowerCase(),
                resourceType: ResourceType.PNF
            });
        };

        this.$scope.createCR = ():void => {
            this.$state.go('workspace.general', {
                type: ComponentType.RESOURCE.toLowerCase(),
                resourceType: ResourceType.CR
            });
        };

        this.$scope.entitiesCount = (folderItem:FoldersItemsMenu):any => {
            let self = this;
            let total:number = 0;
            if (folderItem.isGroup()) {
                this.$scope.folders.getFolders().forEach(function (tmpFolder:FoldersItemsMenu) {
                    if (tmpFolder.group && tmpFolder.group === (<FoldersItemsMenuGroup>folderItem).groupname) {
                        total = total + self._getTotalCounts(tmpFolder, self);
                    }
                });
            } else {
                total = total + self._getTotalCounts(folderItem, self);
            }
            return total;
        };

        this.$scope.getCurrentFolderDistributed = ():Array<any> => {
            let self = this;
            let states = [];
            if (this.$scope.folders) {
                let folderItem:FoldersItemsMenu = this.$scope.folders.getCurrentFolder();
                if (folderItem.isGroup()) {
                    this.$scope.folders.getFolders().forEach(function (tmpFolder:FoldersItemsMenu) {
                        if (tmpFolder.group && tmpFolder.group === (<FoldersItemsMenuGroup>folderItem).groupname) {
                            self._setStates(tmpFolder, states);
                        }
                    });
                } else {
                    self._setStates(folderItem, states);
                }
            }
            return states;
        };

        this.$scope.setSelectedFolder = (folderItem:FoldersItemsMenu):void => {
            this.$scope.folders.setSelected(folderItem);
        };

        this.$scope.goToComponent = (component:Component):void => {
            this.$scope.isLoading = true;
            this.$state.go('workspace.general', {id: component.uniqueId, type: component.componentType.toLowerCase()});
        };

        this.$scope.raiseNumberOfElementToDisplay = ():void => {
            this.$scope.numberOfItemToDisplay = this.$scope.numberOfItemToDisplay + 35;
            if (this.$scope.components) {
                this.$scope.isAllItemDisplay = this.$scope.numberOfItemToDisplay >= this.$scope.components.length;
            }
        };

        this.$scope.updateSearchTerm = (newTerm: string):void => {
            this.$scope.filterParams.searchTerm = newTerm;
        };

        this.$scope.changeFilterParams = ():void => {
            this.$state.go('.', this.$scope.filterParams.toParam(), {location: 'replace', notify: false});
        };
    };

    private _getTotalCounts(tmpFolder, self):number {
        let total:number = 0;
        if (tmpFolder.dist !== undefined) {
            let distributions = tmpFolder.dist.split(',');
            distributions.forEach((item:any) => {
                total = total + self.getEntitiesByStateDist(tmpFolder.state, item).length;
            });
        }
        else {
            total = total + self.getEntitiesByStateDist(tmpFolder.state, tmpFolder.dist).length;
        }
        return total;
    }

    private _setStates(tmpFolder, states) {
        if (tmpFolder.states !== undefined) {
            tmpFolder.states.forEach(function (item:any) {
                states.push({"state": item.state, "dist": item.dist});
            });
        } else {
            states.push({"state": tmpFolder.state, "dist": tmpFolder.dist});
        }
    }

    private initEntities = (forceReload?:boolean):void => {

        if(forceReload || this.componentShouldReload()){
            this.$scope.isLoading = true;
            this.entityService.getAllComponents(true).then(
                (components:Array<Component>) => {
                    this.cacheService.set('breadcrumbsComponentsState', this.$state.current.name);  //dashboard
                    this.cacheService.set('breadcrumbsComponents', components);
                    this.components = components;
                    this.$scope.components = components;
                    this.$scope.isAllItemDisplay = this.$scope.numberOfItemToDisplay >= this.$scope.components.length;
                    this.$scope.isLoading = false;
                });
        } else {
            this.components = this.cacheService.get('breadcrumbsComponents');
            this.$scope.components = this.components;
            this.$scope.isAllItemDisplay = this.$scope.numberOfItemToDisplay >= this.$scope.components.length;
            
        }
    
    };

    private isDefaultFilter = (): boolean => {
        let defaultFilter = new DashboardFilter();
        return angular.equals(defaultFilter, this.$scope.filterParams);
    }

    private componentShouldReload = ():boolean => {
        let breadcrumbsValid: boolean = (this.$state.current.name === this.cacheService.get('breadcrumbsComponentsState') && this.cacheService.contains('breadcrumbsComponents'));
        return !breadcrumbsValid || this.isDefaultFilter();
    }

    private getEntitiesByStateDist = (state:string, dist:string):Array<Component> => {
        let gObj:Array<Component>;
        if (this.components && (state || dist)) {
            gObj = this.components.filter(function (obj:Component) {
                if (dist !== undefined && obj.distributionStatus === dist && obj.lifecycleState === state) {
                    return true;
                } else if (dist === undefined && obj.lifecycleState === state) {
                    return true;
                }
                return false;
            });
        } else {
            gObj = [];
        }
        return gObj;
    }
}
