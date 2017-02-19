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
/// <reference path="../../../references"/>
module Sdc.ViewModels.Wizard {
    'use strict';
    import ArtifactModel = Sdc.Models.ArtifactModel;

    export interface IArtifactInformationStepScope extends IWizardCreationScope {
        artifacts: Array<Models.ArtifactModel>;
        tableHeadersList: Array<any>;
        artifactType: string;
        isResourceInstance:boolean;
        downloadFile:Models.IFileDownload;
        isLoading:boolean;
        sortBy:string;
        reverse:boolean;
        component:Models.Components.Component;

        getTitle(): string;
        addOrUpdate(artifact:Models.ArtifactModel): void;
        delete(artifact:Models.ArtifactModel): void;
        download(artifact:Models.ArtifactModel): void;
        clickArtifactName(artifact:any):void;
        openEditEnvParametersModal(artifactResource: Models.ArtifactModel):void;
        sort(sortBy:string): void;
        showNoArtifactMessage():boolean;
    }

    export class ArtifactInformationStepViewModel implements IWizardCreationStep {

        static '$inject' = [
            '$scope',
            '$filter',
            '$modal',
            '$templateCache',
            'Sdc.Services.SharingService',
            '$state',
            'sdcConfig',
            'ModalsHandler'
        ];

        constructor(private $scope:IArtifactInformationStepScope,
                    private $filter:ng.IFilterService,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $templateCache:ng.ITemplateCacheService,
                    private sharingService:Sdc.Services.SharingService,
                    private $state:any,
                    private sdcConfig:Models.IAppConfigurtaion,
                    private ModalsHandler: Utils.ModalsHandler) {
            this.$scope.registerChild(this);
            this.$scope.setValidState(true);
            this.initScope();
        }


        private getMappedObjects():any {
            return {
                normal: this.$scope.component.artifacts
            };
        }

        private initScope = ():void => {
            let self = this;
            this.$scope.isLoading = false;
            this.$scope.sortBy = 'artifactDisplayName';
            this.$scope.reverse = false;

            this.$scope.artifactType = 'normal';
            this.$scope.getTitle = ():string => {
                return this.$filter("resourceName")(this.$scope.component.name) + ' Artifacts';

            };

            this.$scope.tableHeadersList = [
                {title: 'Name', property: 'artifactDisplayName'},
                {title: 'Type', property: 'artifactType'}
            ];


            this.$scope.component = this.$scope.getComponent();
            this.$scope.artifacts = <ArtifactModel[]>_.values(this.$scope.component.artifacts);


            this.$scope.sort = (sortBy:string):void => {
                this.$scope.reverse = (this.$scope.sortBy === sortBy) ? !this.$scope.reverse : false;
                this.$scope.sortBy = sortBy;
            };


            this.$scope.addOrUpdate = (artifact:Models.ArtifactModel):void => {
                artifact.artifactGroupType = 'INFORMATIONAL';
                this.ModalsHandler.openWizardArtifactModal(artifact, this.$scope.getComponent()).then(() => {
                    this.$scope.artifacts = <ArtifactModel[]>_.values(this.$scope.component.artifacts);
                });
            };

            this.$scope.showNoArtifactMessage = ():boolean => {
                let artifacts:any = [];
                artifacts = _.filter(this.$scope.artifacts, (artifact:Models.ArtifactModel)=> {
                    return artifact.esId;
                });

                if (artifacts.length === 0) {
                    return true;
                }
                return false;
            }

            this.$scope.delete = (artifact:Models.ArtifactModel):void => {

                let onOk = ():void => {
                    this.$scope.isLoading = true;
                    let onSuccess = ():void => {
                        this.$scope.isLoading = false;
                        this.$scope.artifacts = <ArtifactModel[]>_.values(this.$scope.component.artifacts);
                    };

                    let onFailed = (error:any):void => {
                        console.log('Delete artifact returned error:', error);
                        this.$scope.isLoading = false;
                    };

                    this.$scope.component.deleteArtifact(artifact.uniqueId, artifact.artifactLabel).then(onSuccess, onFailed);
                };

                let title:string =  this.$filter('translate')("ARTIFACT_VIEW_DELETE_MODAL_TITLE");
                let message:string = this.$filter('translate')("ARTIFACT_VIEW_DELETE_MODAL_TEXT", "{'name': '" + artifact.artifactDisplayName + "'}");
                this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
            };

            this.$scope.clickArtifactName = (artifact:any) => {
                if ('deployment' !== this.$scope.artifactType || 'HEAT' !== artifact.artifactType || !artifact.esId) {
                    this.$scope.addOrUpdate(artifact);
                }

            };

        }

        public save = (callback:Function):void => {
            this.$scope.setComponent(this.$scope.component);
            callback(true);
        }

        public back = (callback:Function):void => {
            callback(true);
        }

    }

}
