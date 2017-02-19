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
/// <reference path="../../../../references"/>
module Sdc.ViewModels {
    'use strict';
    import ArtifactModel = Sdc.Models.ArtifactModel;

    export interface IToscaArtifactsScope extends IWorkspaceViewModelScope {
        artifacts: Array<Models.ArtifactModel>;
        tableHeadersList: Array<any>;
        artifactType: string;
        downloadFile:Models.IFileDownload;
        isLoading:boolean;
        sortBy:string;
        reverse:boolean;

        getTitle(): string;
        download(artifact:Models.ArtifactModel): void;
        sort(sortBy:string): void;
        showNoArtifactMessage():boolean;
    }

    export class ToscaArtifactsViewModel {

        static '$inject' = [
            '$scope',
            '$filter'
        ];

        constructor(private $scope:IToscaArtifactsScope,
                    private $filter:ng.IFilterService) {
            this.initScope();
            this.$scope.updateSelectedMenuItem();
        }

        private initScope = ():void => {
            let self = this;
            this.$scope.isLoading = false;
            this.$scope.sortBy = 'artifactDisplayName';
            this.$scope.reverse = false;
            this.$scope.setValidState(true);
            this.$scope.artifactType = 'normal';
            this.$scope.getTitle = ():string => {
                return this.$filter("resourceName")(this.$scope.component.name) + ' Artifacts';

            };

            this.$scope.tableHeadersList = [
                {title: 'Name', property: 'artifactDisplayName'},
                {title: 'Type', property: 'artifactType'}
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
}
