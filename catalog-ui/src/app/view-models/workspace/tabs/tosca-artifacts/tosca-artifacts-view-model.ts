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
import {ArtifactModel, IFileDownload} from "app/models";
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {ComponentGenericResponse} from "../../../../ng2/services/responses/component-generic-response";
import {ComponentServiceNg2} from "../../../../ng2/services/component-services/component.service";
export interface IToscaArtifactsScope extends IWorkspaceViewModelScope {
    artifacts:Array<ArtifactModel>;
    tableHeadersList:Array<any>;
    artifactType:string;
    downloadFile:IFileDownload;
    isLoading:boolean;
    sortBy:string;
    reverse:boolean;

    getTitle():string;
    download(artifact:ArtifactModel):void;
    sort(sortBy:string):void;
    showNoArtifactMessage():boolean;
}

export class ToscaArtifactsViewModel {

    static '$inject' = [
        '$scope',
        '$filter',
        'ComponentServiceNg2'
    ];

    constructor(private $scope:IToscaArtifactsScope,
                private $filter:ng.IFilterService,
                private ComponentServiceNg2:ComponentServiceNg2) {
        this.initToscaArtifacts();
    }

    private initToscaArtifacts = (): void => {

        if(!this.$scope.component.toscaArtifacts) {
            this.$scope.isLoading = true;
            this.ComponentServiceNg2.getComponentToscaArtifacts(this.$scope.component).subscribe((response:ComponentGenericResponse) => {
                this.$scope.component.toscaArtifacts = response.toscaArtifacts;
                this.initScope();
                this.$scope.isLoading = false;
            }, () => {
                this.$scope.isLoading = false;
            });
        } else {
            this.initScope();
        }
    }

    private initScope = ():void => {
        this.$scope.isLoading = false;
        this.$scope.sortBy = 'artifactDisplayName';
        this.$scope.reverse = false;
        this.$scope.setValidState(true);
        this.$scope.artifactType = 'informational';
        this.$scope.getTitle = ():string => {
            return this.$filter("resourceName")(this.$scope.component.name) + ' Artifacts';

        };

        this.$scope.tableHeadersList = [
            {title: 'Name', property: 'artifactDisplayName'},
            {title: 'Type', property: 'artifactType'},
            {title: 'Version', property: 'artifactVersion'}
        ];

        this.$scope.artifacts = <ArtifactModel[]>_.values(this.$scope.component.toscaArtifacts);
        this.$scope.sort = (sortBy:string):void => {
            this.$scope.reverse = (this.$scope.sortBy === sortBy) ? !this.$scope.reverse : false;
            this.$scope.sortBy = sortBy;
        };


        this.$scope.showNoArtifactMessage = ():boolean => {
            if (this.$scope.artifacts.length === 0) {
                return true;
            }
            return false;
        };

    }
}
