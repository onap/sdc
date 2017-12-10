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
import {ComponentType, CHANGE_COMPONENT_CSAR_VERSION_FLAG, SEVERITY, FileUtils, ModalsHandler, ComponentFactory} from "app/utils";
import {OnboardingService, CacheService} from "app/services";
import {Component, IComponent, IUser, IAppConfigurtaion, Resource} from "app/models";
import {IServerMessageModalModel} from "../message-modal/message-server-modal/server-message-modal-view-model";
import {Dictionary} from "app/utils";
import * as _ from 'underscore';

interface IOnboardingModalViewModelScope {
    modalOnboarding:ng.ui.bootstrap.IModalServiceInstance;
    componentsList:Array<IComponent>;
    tableHeadersList:Array<any>;
    selectedComponent:Component;
    componentFromServer:Component;
    reverse:boolean;
    sortBy:string;
    searchBind:string;
    okButtonText:string;
    isCsarComponentExists:boolean;
    user:IUser;
    isLoading:boolean;

    //this is for UI paging
    numberOfItemsToDisplay:number;
    allItemsDisplayed:boolean;

    doSelectComponent(component:Component):void;
    doUpdateCsar():void;
    doImportCsar():void;
    sort(sortBy:string):void;
    downloadCsar(packageId:string):void;
    increaseNumItemsToDisplay():void;
}

export class OnboardingModalViewModel {

    static '$inject' = [
        '$scope',
        '$filter',
        '$state',
        'sdcConfig',
        '$uibModalInstance',
        'Sdc.Services.OnboardingService',
        'okButtonText',
        'currentCsarUUID',
        'Sdc.Services.CacheService',
        'FileUtils',
        'ComponentFactory',
        'ModalsHandler'
    ];

    constructor(private $scope:IOnboardingModalViewModelScope,
                private $filter:ng.IFilterService,
                private $state:any,
                private sdcConfig:IAppConfigurtaion,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private onBoardingService:OnboardingService,
                private okButtonText:string,
                private currentCsarUUID:string,
                private cacheService:CacheService,
                private fileUtils:FileUtils,
                private componentFactory:ComponentFactory,
                private modalsHandler:ModalsHandler) {

        this.init();
    }

    /**
     * Called from controller constructor, this will call onboarding service to get list
     * of "mini" components (empty components created from CSAR).
     * The list is inserted to componentsList on $scope.
     * And then call initScope method.
     */
    private init = ():void => {
        this.initOnboardingComponentsList();
    };

    private initScope = ():void => {

        this.initSortedTableScope();
        this.initModalScope();
        this.$scope.sortBy = "name"; // Default sort by
        this.$scope.user = this.cacheService.get('user');
        this.$scope.okButtonText = this.okButtonText;
        this.$scope.numberOfItemsToDisplay = 0;
        this.$scope.allItemsDisplayed = false;

        // Dismiss the modal and pass the "mini" component to workspace general page
        this.$scope.doImportCsar = ():void => {
            this.$uibModalInstance.dismiss();
            this.$state.go('workspace.general', {
                type: ComponentType.RESOURCE.toLowerCase(),
                componentCsar: this.$scope.selectedComponent
            });
        };

        this.$scope.doUpdateCsar = ():void => {
            // In case user select on update the checkin and submit for testing buttons (in general page) should be disabled.
            // to do that we need to pass to workspace.general state parameter to know to disable the buttons.
            this.$uibModalInstance.close();
            // Change the component version to the CSAR version we want to update.
            /*(<Resource>this.$scope.componentFromServer).csarVersion = (<Resource>this.$scope.selectedComponent).csarVersion;
             let component:Components.Component = this.componentFactory.createComponent(this.$scope.componentFromServer);
             this.$state.go('workspace.general', {vspComponent: component, disableButtons: true });*/
            this.cacheService.set(CHANGE_COMPONENT_CSAR_VERSION_FLAG, (<Resource>this.$scope.selectedComponent).csarVersion);
            this.$state.go('workspace.general', {
                id: this.$scope.componentFromServer.uniqueId,
                componentCsar: this.$scope.selectedComponent,
                type: this.$scope.componentFromServer.componentType.toLowerCase(),
                disableButtons: true
            });
        };

        this.$scope.downloadCsar = (packageId:string):void => {
            this.$scope.isLoading = true;
            this.onBoardingService.downloadOnboardingCsar(packageId).then(
                (file:any):void => {
                    this.$scope.isLoading = false;
                    if (file) {
                        this.fileUtils.downloadFile(file, packageId + '.csar');
                    }
                }, ():void => {
                    this.$scope.isLoading = false;
                    var data:IServerMessageModalModel = {
                        title: 'Download error',
                        message: "Error downloading file",
                        severity: SEVERITY.ERROR,
                        messageId: "",
                        status: ""
                    };
                    this.modalsHandler.openServerMessageModal(data);
                }
            );
        };

        this.$scope.increaseNumItemsToDisplay = ():void => {
            this.$scope.numberOfItemsToDisplay = this.$scope.numberOfItemsToDisplay + 40;
            if (this.$scope.componentsList) {
                this.$scope.allItemsDisplayed = this.$scope.numberOfItemsToDisplay >= this.$scope.componentsList.length;
            }
        };

        // When the user select a row, set the component as selectedComponent
        this.$scope.doSelectComponent = (component:Component):void => {

            if (this.$scope.selectedComponent === component) {
                // Collapse the item
                this.$scope.selectedComponent = undefined;
                return;
            }

            this.$scope.isLoading = true;
            this.$scope.componentFromServer = undefined;
            this.$scope.selectedComponent = component;

            let onSuccess = (componentFromServer:Component):void => {
                this.$scope.isLoading = false;
                if (componentFromServer) {
                    this.$scope.componentFromServer = componentFromServer;
                    this.$scope.isCsarComponentExists = true;
                } else {
                    this.$scope.componentFromServer = component;
                    this.$scope.isCsarComponentExists = false;
                }
            };

            let onError = ():void => {
                this.$scope.isLoading = false;
                this.$scope.componentFromServer = component;
                this.$scope.isCsarComponentExists = false;
            };

            this.onBoardingService.getComponentFromCsarUuid((<Resource>component).csarUUID).then(onSuccess, onError);
        };

    };

    private initSortedTableScope = ():void => {
        this.$scope.tableHeadersList = [
            {title: 'Name', property: 'name'},
            {title: 'Vendor', property: 'vendorName'},
            {title: 'Category', property: 'categories'},
            {title: 'Version', property: 'csarVersion'},
            {title: '#', property: 'importAndUpdate'}
            //{title: 'Date', property: 'componentDate'}
        ];

        this.$scope.sort = (sortBy:string):void => {
            this.$scope.reverse = (this.$scope.sortBy === sortBy) ? !this.$scope.reverse : false;
            this.$scope.sortBy = sortBy;
        };
    };

    private initModalScope = ():void => {
        // Enable the modal directive to close
        this.$scope.modalOnboarding = this.$uibModalInstance;
    };

    private initOnboardingComponentsList = ():void => {
        let onSuccess = (onboardingResponse:Array<IComponent>):void => {
            initMaxVersionOfItemsInList(onboardingResponse);

            if (this.currentCsarUUID) {
                //this.$scope.componentsList = this.$filter('filter')(this.$scope.componentsList, {csarUUID: this.currentCsarUUID});
                this.$scope.componentsList = this.$filter('filter')(this.$scope.componentsList,
                    (input):boolean => {
                        return input.csarUUID === this.currentCsarUUID;
                    }
                );
            }
            this.initScope();
        };

        let onError = ():void => {
            console.log("Error getting onboarding list");
            this.initScope();
        };

        let initMaxVersionOfItemsInList = (onboardingResponse:Array<IComponent>):void => {
            // Get only the latest version of each item
            this.$scope.componentsList = [];

            // Get all unique items from the list
            let uniqueItems:Array<any> = _.uniq(onboardingResponse, false, (item:any):void=>{
                return item.packageId;
            });
            
            // Loop on all the items with unique packageId
            _.each(uniqueItems, (item:any):void=> {
                // Find all the items that has same packageId
                let ItemsFound:Array<IComponent> = _.filter(onboardingResponse, (inListItem:any):any => {
                    return inListItem.packageId === item.packageId;
                });

                // Loop on all the items with same packageId and find the max version.
                let maxItem:any;
                _.each(ItemsFound, (ItemFound:any):void=> {
                    if (!maxItem) {
                        maxItem = ItemFound;
                    } else if (maxItem && parseInt(maxItem.csarVersion) < parseInt(ItemFound.csarVersion)) {
                        maxItem = ItemFound;
                    }
                });
                this.$scope.componentsList.push(maxItem);
            });
        };

        this.onBoardingService.getOnboardingComponents().then(onSuccess, onError);
    };
}
