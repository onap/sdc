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
import {Component} from "app/models";
import {GRAPH_EVENTS} from "app/utils";
import {LeftPaletteLoaderService, EventListenerService} from "app/services";
import {ICompositionViewModelScope} from "../../composition-view-model";
import {LeftPaletteComponent} from "../../../../../../models/components/displayComponent";

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

    changeResourceVersion():void;
}

export class DetailsViewModel {

    static '$inject' = [
        '$scope',
        'LeftPaletteLoaderService',
        'EventListenerService'

    ];

    constructor(private $scope:IDetailsViewModelScope,
                private LeftPaletteLoaderService:LeftPaletteLoaderService,
                private eventListenerService:EventListenerService) {
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
            let latestVersionComponent:LeftPaletteComponent = _.maxBy(_.filter(this.LeftPaletteLoaderService.getLeftPanelComponentsForDisplay(this.$scope.currentComponent.componentType), (component:LeftPaletteComponent) => { //latest checked in
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

        this.$scope.changeResourceVersion = ():void => {
            this.$scope.isLoading = true;
            this.$scope.$parent.isLoading = true;

            let onSuccess = (component:Component)=> {
                this.$scope.isLoading = false;
                this.$scope.$parent.isLoading = false;
                this.$scope.onComponentInstanceVersionChange(component);

                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_VERSION_CHANGED, this.$scope.currentComponent);
            };

            let onFailed = (error:any)=> {
                this.$scope.isLoading = false;
                this.$scope.$parent.isLoading = false;
                console.log(error);
            };

            let componentUid:string = this.$scope.editResourceVersion.allVersions[this.$scope.editResourceVersion.changeVersion];
            this.$scope.currentComponent.changeComponentInstanceVersion(componentUid).then(onSuccess, onFailed);
        };
    }
}
