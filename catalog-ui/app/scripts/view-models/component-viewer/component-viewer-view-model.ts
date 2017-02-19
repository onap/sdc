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
/// <reference path="../../references"/>

module Sdc.ViewModels {
    'use strict';

    interface IComponentViewerViewModelScope extends ng.IScope {
        component: Models.Components.Component;
        additionalInformations: Array<Models.AdditionalInformationModel>;
        activityLog: any;
        activityDateArray: Array<any>; //this is in order to sort the dates
        inputs: Array<any>;
        isLoading: boolean;
        templateUrl: string;
        currentTab:string;
        preVersion:string;
        sdcMenu:Models.IAppMenu;
        versionsList:Array<any>;
        close(): void;
        hasItems(obj:any): boolean;
        onVersionChanged(version:any) : void;
        moveToTab(tab:string):void;
        isSelected(tab:string):boolean;
        getActivityLog(uniqueId:string):void;
        parseAction(action:string):string;
    }

    export class ComponentViewerViewModel {

        static '$inject' = [
            '$scope',
            '$modalInstance',
            'component',
            'Sdc.Services.ActivityLogService',
            'sdcMenu',
            'ComponentFactory'
        ];

        constructor(private $scope:IComponentViewerViewModelScope,
                    private $modalInstance:ng.ui.bootstrap.IModalServiceInstance,
                    private component:Models.Components.Component,
                    private activityLogService:Services.ActivityLogService,
                    private sdcMenu:Models.IAppMenu,
                    private ComponentFactory: Utils.ComponentFactory) {
            this.initScope(component);
        }

        //creating objects for versions
        private initVersionObject:Function = ():void => {
            this.$scope.versionsList = [];
            for (let version in this.$scope.component.allVersions) {
                this.$scope.versionsList.push({
                    versionNumber: version,
                    versioning: this.versioning(version),
                    versionId: this.$scope.component.allVersions[version]
                });
            }

        };

        private versioning:Function = (versionNumber:string):string => {
            let version:Array<string> = versionNumber.split('.');
            return '00000000'.slice(version[0].length) + version[0] + '.' + '00000000'.slice(version[1].length) + version[1];
        };

        private showComponentInformationView:Function = ():void => {
            if (this.$scope.component.isResource()) {
                this.$scope.templateUrl = '/app/scripts/view-models/component-viewer/properties/resource-properties-view.html';
            } else if(this.$scope.component.isService()) {
                this.$scope.templateUrl = '/app/scripts/view-models/component-viewer/properties/service-properties-view.html';
            } else {
                this.$scope.templateUrl = '/app/scripts/view-models/component-viewer/properties/product-properties-view.html';
            }
        };

        private showActivityLogView:Function = ():void => {
            this.$scope.templateUrl = '/app/scripts/view-models/component-viewer/activity-log/activity-log-view.html';
        };

        private initComponent = (component:Models.Components.Component):void => {
            this.$scope.component = component;
            this.$scope.additionalInformations = component.getAdditionalInformation();
            this.initVersionObject();
            this.$scope.isLoading = false;
        };

        private initScope = (component:Models.Components.Component):void => {
            this.$scope.isLoading = false;
            this.initComponent(component);
            this.$scope.currentTab = 'PROPERTIES';
            this.$scope.preVersion = component.version;
            this.$scope.sdcMenu = this.sdcMenu;
            this.showComponentInformationView();
            //service inputs
            if (component.isService()) {
                let inputs:Array<any> = [];

                for (let group in component.componentInstancesProperties) {
                    if (component.componentInstancesProperties[group]) {
                        component.componentInstancesProperties[group].forEach((property:Models.PropertyModel):void => {
                            if (!property.value) {
                                property.value = property.defaultValue;
                            }
                            inputs.push({
                                name: property.name,
                                value: property.value,
                                type: property.type
                            });
                        });
                    }
                }
                this.$scope.inputs = inputs;
            }

            this.$scope.hasItems = (obj:any):boolean => {
                return Object.keys(obj).length > 0;
            };

            this.$scope.close = ():void => {
                this.$modalInstance.dismiss();
            };

            this.$scope.onVersionChanged = (version:any):void => {
                if (version.versionNumber != this.$scope.component.version) {
                    this.$scope.isLoading = true;
                    this.ComponentFactory.getComponentFromServer(this.component.componentType, version.versionId).then((component: Models.Components.Component):void => {
                        this.initComponent(component);
                    });
                    if (this.$scope.currentTab === 'ACTIVITY_LOG') {
                        this.$scope.getActivityLog(version.versionId);
                    }

                }
            };

            this.$scope.getActivityLog = (uniqueId:any):void => {

                let onError = (response) => {
                    this.$scope.isLoading = false;
                    console.info('onFaild', response);

                };
                let onSuccess = (response:Array<Models.Activity>) => {
                    this.$scope.activityLog = _.groupBy(response, function (activity:Models.Activity) {  //group by date only
                        let dateTime:Date = new Date(activity.TIMESTAMP.replace(" UTC", '').replace(" ", 'T'));
                      //  let date:Date = new Date(dateTime.getFullYear(), dateTime.getMonth(), dateTime.getDate());
                        return dateTime.getTime();
                    });
                    /*this is in order to sort the jsonObject by date*/
                    this.$scope.activityDateArray = Object.keys(this.$scope.activityLog);
                    this.$scope.activityDateArray.sort().reverse();
                    this.$scope.isLoading = false;
                };

                this.$scope.isLoading = true;
                if (this.$scope.component.isResource()) {
                    this.activityLogService.getActivityLogService('resources', uniqueId).then(onSuccess, onError);
                }
                if (this.$scope.component.isService()) {
                    this.activityLogService.getActivityLogService('services', uniqueId).then(onSuccess, onError);
                }

            };

            this.$scope.moveToTab = (tab:string):void => {
                if (tab === this.$scope.currentTab) {
                    return;
                } else if (tab === 'PROPERTIES') {
                    this.showComponentInformationView();
                    this.$scope.preVersion = this.$scope.component.version;
                } else if (tab === 'ACTIVITY_LOG') {
                    if (!this.$scope.activityLog || this.$scope.preVersion != this.$scope.component.version) {
                        this.$scope.activityLog = this.$scope.getActivityLog(this.$scope.component.uniqueId);
                    }
                    this.showActivityLogView();
                } else {
                    console.error("Tab " + tab + " not found!");
                    return;
                }
                this.$scope.currentTab = tab;
            };

            this.$scope.isSelected = (tab:string):boolean => {
                return tab === this.$scope.currentTab;
            };

            this.$scope.parseAction = (action:string) => {
                return action ? action.split(/(?=[A-Z])/).join(' ') : '';
            };

        }
    }
}
