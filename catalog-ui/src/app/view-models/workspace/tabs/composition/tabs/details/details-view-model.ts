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
import {Component, ModalModel, ButtonModel} from "app/models";
import {GRAPH_EVENTS} from "app/utils";
import {LeftPaletteLoaderService, EventListenerService} from "app/services";
import {ICompositionViewModelScope} from "../../composition-view-model";
import {LeftPaletteComponent} from "../../../../../../models/components/displayComponent";
import {ComponentServiceFactoryNg2} from "app/ng2/services/component-services/component.service.factory";
import {ServiceServiceNg2} from 'app/ng2/services/component-services/service.service';
import {Service} from "app/models/components/service";
import {ModalService} from 'app/ng2/services/modal.service';

export interface IEditResourceVersion {
    allVersions:any;
    changeVersion:string;
}

interface IDetailsViewModelScope extends ICompositionViewModelScope {
    isLoading:boolean;
    $parent:ICompositionViewModelScope;
    expandedSection:Array<string>;
    editForm:ng.IFormController;
    editResourceVersion:IEditResourceVersion;

    onChangeResourceVersion():void;
    alertBeforeChangeResourceVersion():void;
    changeVersion():void;
    cancelChangeResourceVersion():void;
}

export class DetailsViewModel {

    static '$inject' = [
        '$scope',
        '$filter',
        'LeftPaletteLoaderService',
        'EventListenerService',
        'ComponentServiceFactoryNg2',
        'ServiceServiceNg2',
        'ModalServiceNg2'
    ];

    constructor(private $scope:IDetailsViewModelScope,
                private $filter:ng.IFilterService,
                private LeftPaletteLoaderService:LeftPaletteLoaderService,
                private eventListenerService:EventListenerService,
                private ComponentServiceFactoryNg2: ComponentServiceFactoryNg2,
                private serviceService: ServiceServiceNg2,
                private ModalServiceNg2: ModalService) {
        this.initScope();
    }

    private clearSelectedVersion = ():void => {
        this.$scope.editResourceVersion = {
            allVersions: {},
            changeVersion: null
        };
    };

    private versioning:Function = (versionNumber:string):string => {
        let version:Array<string> = versionNumber.split('.');
        return '00000000'.slice(version[0].length) + version[0] + '.' + '00000000'.slice(version[1].length) + version[1];
    };

    private initEditResourceVersion = ():void => {
        this.clearSelectedVersion();
        this.$scope.editResourceVersion.allVersions[this.$scope.currentComponent.selectedInstance.componentVersion] = this.$scope.currentComponent.selectedInstance.componentUid;
        _.merge(this.$scope.editResourceVersion.allVersions, angular.copy(this.$scope.selectedComponent.allVersions));
        let sorted:any = _.sortBy(_.toPairs(this.$scope.editResourceVersion.allVersions), (item)=> {
            return this.versioning(item[0]);
        });
        this.clearSelectedVersion();
        _.forEach(sorted, (item)=> {
            this.$scope.editResourceVersion.allVersions[item[0]] = item[1];
        });

        let highestVersion = _.last(Object.keys(this.$scope.selectedComponent.allVersions));

        if (parseFloat(highestVersion) % 1) { //if highest is minor, make sure it is the latest checked in -
            let latestVersionComponent:LeftPaletteComponent = _.maxBy(_.filter(this.LeftPaletteLoaderService.getLeftPanelComponentsForDisplay(this.$scope.currentComponent), (component:LeftPaletteComponent) => { //latest checked in
                return (component.systemName === this.$scope.selectedComponent.systemName
                || component.uuid === this.$scope.selectedComponent.uuid);
            }),(component)=>{return component.version});

            let latestVersion:string = latestVersionComponent ? latestVersionComponent.version : highestVersion;

            if (highestVersion != latestVersion) { //highest is checked out - remove from options
                this.$scope.editResourceVersion.allVersions = _.omit(this.$scope.editResourceVersion.allVersions, highestVersion);
            }
        }
        this.$scope.editResourceVersion.changeVersion = this.$scope.currentComponent.selectedInstance.componentVersion;
    };

    private initScope = ():void => {
        this.$scope.isLoading = false;
        this.$scope.$parent.isLoading = false;
        this.$scope.expandedSection = ['general', 'tags'];
        //this.clearSelectedVersion();

        this.$scope.$watch('selectedComponent', (component:Component) => {
            if (this.$scope.isComponentInstanceSelected()) {
                this.initEditResourceVersion();
            }
        });

        this.$scope.onChangeResourceVersion = ():void => {
            if(this.$scope.isComponentInstanceSelected() && this.$scope.currentComponent.selectedInstance.isServiceProxy()) {
                this.$scope.alertBeforeChangeResourceVersion();
            }
            else {
                this.$scope.changeVersion();
            }
        };

        this.$scope.alertBeforeChangeResourceVersion = ():void => {
            let modalApproveTxt:string = this.$filter('translate')("MODAL_APPROVE");
            let modalCancelTxt:string = this.$filter('translate')("MODAL_CANCEL");
            let changeVersionModalTitle:string = this.$filter('translate')("DETAILS_TAB_CHANGE_VERSION_MODAL_TITLE");
            let changeVersionModalMsg:string = this.$filter('translate')("DETAILS_TAB_CHANGE_VERSION_MODAL_MSG");

            let actionButton: ButtonModel = new ButtonModel(modalApproveTxt, 'blue', this.$scope.changeVersion);
            let cancelButton: ButtonModel = new ButtonModel(modalCancelTxt, 'grey', this.$scope.cancelChangeResourceVersion);
            let modalModel: ModalModel = new ModalModel('sm', changeVersionModalTitle, changeVersionModalMsg, [actionButton, cancelButton]);
            let customModal = this.ModalServiceNg2.createCustomModal(modalModel);
            customModal.instance.open();
        };

        this.$scope.cancelChangeResourceVersion = () => {
            this.ModalServiceNg2.closeCurrentModal();
            this.$scope.editResourceVersion.changeVersion = this.$scope.currentComponent.selectedInstance.componentVersion;
        };

        this.$scope.changeVersion = ():void => {
            this.ModalServiceNg2.closeCurrentModal();
            this.$scope.isLoading = true;
            this.$scope.$parent.isLoading = true;

            let service = <Service>this.$scope.currentComponent;
            let {changeVersion} = this.$scope.editResourceVersion;
            let componentUid:string = this.$scope.editResourceVersion.allVersions[changeVersion];

            let onCancel = (error:any) => {
                this.$scope.isLoading = false;
                this.$scope.$parent.isLoading = false;
                this.$scope.editResourceVersion.changeVersion = this.$scope.currentComponent.selectedInstance.componentVersion;

                if (error) {
                    console.log(error);
                }
            };

            let onUpdate = () => {
                let onSuccess = (component:Component) => {
                    this.$scope.isLoading = false;
                    this.$scope.$parent.isLoading = false;
                    this.$scope.onComponentInstanceVersionChange(component);
                };
 
                this.$scope.currentComponent.changeComponentInstanceVersion(componentUid).then(onSuccess, onCancel);
            };

            if (this.$scope.currentComponent.isService()) {
                this.serviceService.checkComponentInstanceVersionChange(service, componentUid).subscribe((pathsToDelete:string[]) => {
                    if (pathsToDelete && pathsToDelete.length) {
                        this.$scope.isLoading = false;
                        this.$scope.$parent.isLoading = false;
                        this.$scope.$parent.openVersionChangeModal(pathsToDelete).then(() => {
                            this.$scope.isLoading = true;
                            this.$scope.$parent.isLoading = true;
                            onUpdate();
                        }, onCancel);
                    } else {
                        onUpdate();
                    }
                }, onCancel);
            } else {
                onUpdate();
            }
        };
    }
}
