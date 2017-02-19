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
/// <reference path="../../../../../../references"/>
module Sdc.ViewModels {
    'use strict';
    import Resource = Sdc.Models.Components.Resource;

   export interface IArtifactsViewModelScope extends ICompositionViewModelScope {
        artifacts: Array<Models.ArtifactModel>;
        artifactType: string;
        downloadFile:Models.IFileDownload;
        isLoading:boolean;

        getTitle(): string;
        addOrUpdate(artifact:Models.ArtifactModel): void;
        delete(artifact:Models.ArtifactModel): void;
        download(artifact:Models.ArtifactModel): void;
        openEditEnvParametersModal(artifact:Models.ArtifactModel):void;
        getEnvArtifact(heatArtifact:Models.ArtifactModel):any;
        getEnvArtifactName(artifact:Models.ArtifactModel):string;
        isLicenseArtifact(artifact:Models.ArtifactModel):boolean;
        isVFiArtifact(artifact:Models.ArtifactModel):boolean;
    }

    export class ResourceArtifactsViewModel {

        static '$inject' = [
            '$scope',
            '$filter',
            '$modal',
            '$templateCache',
            '$state',
            'sdcConfig',
            'ArtifactsUtils',
            'ModalsHandler',
            'Sdc.Services.CacheService'
        ];

        constructor(private $scope:IArtifactsViewModelScope,
                    private $filter:ng.IFilterService,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $templateCache:ng.ITemplateCacheService,
                    private $state:any,
                    private sdcConfig:Models.IAppConfigurtaion,
                    private artifactsUtils:Sdc.Utils.ArtifactsUtils,
                    private ModalsHandler: Utils.ModalsHandler,
                    private cacheService:Services.CacheService) {

            this.initScope();
        }


        private initArtifactArr = (artifactType:string):void => {
            let artifacts:Array<Models.ArtifactModel> = [];

            if (this.$scope.selectedComponent) {
                if ('interface' == artifactType) {
                    let interfaces = this.$scope.selectedComponent.interfaces;
                    if (interfaces && interfaces.standard && interfaces.standard.operations) {

                        angular.forEach(interfaces.standard.operations, (operation:any, interfaceName:string):void => {
                            let item:Sdc.Models.ArtifactModel = <Sdc.Models.ArtifactModel>{};
                            if (operation.implementation) {
                                item = <Sdc.Models.ArtifactModel> operation.implementation;
                            }
                            item.artifactDisplayName = interfaceName;
                            item.artifactLabel = interfaceName;
                            item.mandatory = false;
                            artifacts.push(item);
                        });
                    }
                }else {
                    //init normal artifacts, deployment or api artifacts
                    let artifactsObj:Models.ArtifactGroupModel;
                    switch (artifactType) {
                        case "api":
                            artifactsObj = (<Models.Components.Service>this.$scope.selectedComponent).serviceApiArtifacts;
                            break;
                        case "deployment":
                            if (!this.$scope.isComponentInstanceSelected()) {
                                artifactsObj = this.$scope.selectedComponent.deploymentArtifacts;
                            } else {
                                artifactsObj = this.$scope.currentComponent.selectedInstance.deploymentArtifacts;
                            }
                            break;
                        default:
                            artifactsObj = this.$scope.selectedComponent.artifacts;
                            break;
                    }
                    _.forEach(artifactsObj, (artifact:Models.ArtifactModel, key) => {
                        artifacts.push(artifact);
                    });
                }
            }
            this.$scope.artifacts = artifacts;
        };

        private openEditArtifactModal = (artifact:Models.ArtifactModel):void => {
            let viewModelsHtmlBasePath:string = '/app/scripts/view-models/';

            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get(viewModelsHtmlBasePath + 'forms/artifact-form/artifact-form-view.html'),
                controller: 'Sdc.ViewModels.ArtifactResourceFormViewModel',
                size: 'sdc-md',
                backdrop: 'static',
                keyboard: false,
                resolve: {
                    artifact: ():Models.ArtifactModel => {
                        return artifact;
                    },
                    component: (): Models.Components.Component => {
                        return  this.$scope.currentComponent;
                    }
                }
            };

            let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
            modalInstance
                .result
                .then(():void => {
                    this.initArtifactArr(this.$scope.artifactType);
                });
        };

        private initScope = ():void => {
            let self = this;
            this.$scope.isLoading= false;
            this.$scope.artifactType = this.artifactsUtils.getArtifactTypeByState(this.$state.current.name);
            this.initArtifactArr(this.$scope.artifactType);

            this.$scope.getTitle = ():string => {
                return this.artifactsUtils.getTitle(this.$scope.artifactType, this.$scope.selectedComponent);
            };

            let vfiArtifactTypes:any = this.cacheService.get('UIConfiguration').artifacts.deployment.resourceInstanceDeploymentArtifacts;

            this.$scope.isVFiArtifact=(artifact:Models.ArtifactModel):boolean=>{
                return vfiArtifactTypes[artifact.artifactType];
            }

            this.$scope.$watch('selectedComponent', (newResource:Models.Components.Component):void => {
                if (newResource) {
                    this.initArtifactArr(this.$scope.artifactType);
                }
            });


            this.$scope.$watch('currentComponent.selectedInstance', (newInstance:Models.ComponentsInstances.ComponentInstance):void => {
                if (newInstance) {
                    this.initArtifactArr(this.$scope.artifactType);
                }
            });

            this.$scope.addOrUpdate = (artifact:Models.ArtifactModel):void => {
                this.artifactsUtils.setArtifactType(artifact, this.$scope.artifactType);
                let artifactCopy = new Models.ArtifactModel(artifact);
                this.openEditArtifactModal(artifactCopy);
            };


            this.$scope.delete = (artifact:Models.ArtifactModel):void => {

                let onOk = ():void => {
                    this.$scope.isLoading= true;
                    this.artifactsUtils.removeArtifact(artifact, this.$scope.artifacts);

                    let success = (responseArtifact:Models.ArtifactModel):void => {
                        this.initArtifactArr(this.$scope.artifactType);
                        this.$scope.isLoading= false;
                    };

                    let error =(error:any):void =>{
                        console.log('Delete artifact returned error:', error);
                        this.initArtifactArr(this.$scope.artifactType);
                        this.$scope.isLoading= false;
                    };
                    if(this.$scope.isComponentInstanceSelected()){
                        this.$scope.currentComponent.deleteInstanceArtifact(artifact.uniqueId, artifact.artifactLabel).then(success, error);
                    }else{
                        this.$scope.currentComponent.deleteArtifact(artifact.uniqueId, artifact.artifactLabel).then(success, error);//TODO simulate error (make sure error returns)
                    }
                };
                let title: string = this.$filter('translate')("ARTIFACT_VIEW_DELETE_MODAL_TITLE");
                let message: string = this.$filter('translate')("ARTIFACT_VIEW_DELETE_MODAL_TEXT", "{'name': '" + artifact.artifactDisplayName + "'}");
                this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
            };


            this.$scope.getEnvArtifact = (heatArtifact:Models.ArtifactModel):any=>{
                return _.find(this.$scope.artifacts, (item:Models.ArtifactModel)=>{
                    return item.generatedFromId === heatArtifact.uniqueId;
                });
            };

            this.$scope.getEnvArtifactName = (artifact:Models.ArtifactModel):string =>{
                let envArtifact = this.$scope.getEnvArtifact(artifact);
                if(envArtifact){
                    return envArtifact.artifactDisplayName;
                }
            };

            this.$scope.isLicenseArtifact = (artifact:Models.ArtifactModel) :boolean => {
                let isLicense:boolean = false;
                if(this.$scope.component.isResource() && (<Resource>this.$scope.component).isCsarComponent()) {
                    isLicense = this.artifactsUtils.isLicenseType(artifact.artifactType);
                }

                return isLicense;
            };

            this.$scope.openEditEnvParametersModal = (artifact:Models.ArtifactModel):void => {

                let modalOptions:ng.ui.bootstrap.IModalSettings = {
                    template: this.$templateCache.get('/app/scripts/view-models/forms/env-parameters-form/env-parameters-form.html'),
                    controller: 'Sdc.ViewModels.EnvParametersFormViewModel',
                    size: 'sdc-md',
                    backdrop: 'static',
                    resolve: {
                        artifact: ():Models.ArtifactModel => {
                            return artifact;
                        },
                        component: (): Models.Components.Component => {
                            return  this.$scope.currentComponent;
                        }
                    }
                };

                let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
                modalInstance
                    .result
                    .then(():void => {
                        this.initArtifactArr(this.$scope.artifactType);
                    });
            };

        }
    }
}
