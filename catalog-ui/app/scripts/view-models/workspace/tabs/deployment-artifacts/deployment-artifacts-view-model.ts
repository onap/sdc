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
    import Resource = Sdc.Models.Components.Resource;
    import ArtifactModel = Sdc.Models.ArtifactModel;

    interface IDeploymentArtifactsViewModelScope extends IWorkspaceViewModelScope {
        tableHeadersList: Array<any>;
        reverse: boolean;
        sortBy:string;
        artifacts: Array<Models.ArtifactModel>;
        editForm:ng.IFormController;
        isLoading:boolean;
        artifactDescriptions:any;
        updateInProgress:boolean;

        addOrUpdate(artifact:Models.ArtifactModel): void;
        update(artifact:Models.ArtifactModel): void;
        delete(artifact:Models.ArtifactModel): void;
        sort(sortBy:string): void;
        noArtifactsToShow():boolean;
        getValidationPattern(validationType:string, parameterType?:string):RegExp;
        validateJson(json:string):boolean;
        resetValue(parameter:any):void;
        viewModeOrCsarComponent():boolean;
        isLicenseArtifact(artifact:Models.ArtifactModel): boolean;

    }

    export class DeploymentArtifactsViewModel {

        static '$inject' = [
            '$scope',
            '$filter',
            '$modal',
            '$templateCache',
            'ValidationUtils',
            'ArtifactsUtils',
            'ModalsHandler'
        ];

        constructor(private $scope:IDeploymentArtifactsViewModelScope,
                    private $filter:ng.IFilterService,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $templateCache:ng.ITemplateCacheService,
                    private validationUtils:Sdc.Utils.ValidationUtils,
                    private artifactsUtils:Sdc.Utils.ArtifactsUtils,
                    private ModalsHandler:Utils.ModalsHandler) {
            this.initScope();
            this.$scope.updateSelectedMenuItem();
        }

        private initDescriptions = ():void => {
            this.$scope.artifactDescriptions = {};
            _.forEach(this.$scope.component.deploymentArtifacts, (artifact:Models.ArtifactModel):void => {
                this.$scope.artifactDescriptions[artifact.artifactLabel] = artifact.description;
            });
        };


        private setArtifact = (artifact:Models.ArtifactModel):void => {
            if (artifact.heatParameters) {
                artifact.heatParameters.forEach((parameter:any):void => {
                    if (!parameter.currentValue && parameter.defaultValue) {
                        parameter.currentValue = parameter.defaultValue;
                    } else if ("" === parameter.currentValue) {
                        parameter.currentValue = null;
                    }
                });
            }
            if (!artifact.description || !this.$scope.getValidationPattern('string').test(artifact.description)) {
                artifact.description = this.$scope.artifactDescriptions[artifact.artifactLabel];
            }
        };

        private updateAll = ():void => {
            let artifacts:Array<Models.ArtifactModel> = [];
            _.forEach(this.$scope.component.deploymentArtifacts, (artifact:Models.ArtifactModel):void => {
                if (artifact.selected) {
                    this.setArtifact(artifact);
                    artifacts.push(artifact);
                }
            });
            this.$scope.component.updateMultipleArtifacts(artifacts);
        };


        private initScope = ():void => {
            let self = this;
            this.$scope.isLoading = false;
            this.$scope.updateInProgress = false;
            this.initDescriptions();
            this.$scope.artifacts = <ArtifactModel[]>_.values(this.$scope.component.deploymentArtifacts);
            this.$scope.setValidState(true);

            this.$scope.tableHeadersList = [
                {title: 'Name', property: 'artifactDisplayName'},
                {title: 'Type', property: 'artifactType'},
                {title: 'Deployment timeout', property: 'timeout'}
            ];

            this.$scope.isLicenseArtifact = (artifact:Models.ArtifactModel) :boolean => {
                let isLicense:boolean = false;
                if(this.$scope.component.isResource() && (<Resource>this.$scope.component).isCsarComponent()) {

                   isLicense = this.artifactsUtils.isLicenseType(artifact.artifactType);
                }

                return isLicense;
            };

            this.$scope.sort = (sortBy:string):void => {
                this.$scope.reverse = (this.$scope.sortBy === sortBy) ? !this.$scope.reverse : false;
                this.$scope.sortBy = sortBy;
            };

            this.$scope.getValidationPattern = (validationType:string, parameterType?:string):RegExp => {
                return this.validationUtils.getValidationPattern(validationType, parameterType);
            };

            this.$scope.validateJson = (json:string):boolean => {
                if(!json){
                    return true;
                }
                return this.validationUtils.validateJson(json);
            };

            this.$scope.viewModeOrCsarComponent = ():boolean => {
              return this.$scope.isViewMode() || (this.$scope.component.isResource() && (<Resource>this.$scope.component).isCsarComponent());
            };


            this.$scope.addOrUpdate = (artifact:Models.ArtifactModel):void => {
                artifact.artifactGroupType = 'DEPLOYMENT';
                let artifactCopy = new Models.ArtifactModel(artifact);

                let success = (response:any):void => {
                    self.$scope.artifactDescriptions[artifactCopy.artifactLabel] = artifactCopy.description;
                    self.$scope.artifacts = <ArtifactModel[]>_.values(self.$scope.component.deploymentArtifacts);
                };

                let error = (err:any):void =>{
                    console.log(err);
                    self.$scope.artifacts = <ArtifactModel[]>_.values(self.$scope.component.deploymentArtifacts);
                };


                this.ModalsHandler.openWizardArtifactModal(artifactCopy, self.$scope.component).then(success, error);
            };

            this.$scope.noArtifactsToShow = ():boolean => {
                return !_.some(this.$scope.artifacts, 'esId');
            };


            this.$scope.resetValue = (parameter:any):void => {
                if (!parameter.currentValue && parameter.defaultValue) {
                    parameter.currentValue = parameter.defaultValue;
                }
                else if ('boolean' == parameter.type) {
                    parameter.currentValue = parameter.currentValue.toUpperCase();
                }
            };


            this.$scope.$watch('editForm.$valid', ():void => {
                if (this.$scope.editForm) {
                    //    this.$scope.setValidState(this.$scope.editForm.$valid);
                }
            });

            this.$scope.update = (artifact:Models.ArtifactModel):void => {
                if (false == this.$scope.isLoading) {
                    if (artifact.selected && !this.$scope.isViewMode()) {
                        this.$scope.isLoading = true;
                        this.$scope.updateInProgress = true;
                        let onSuccess = (responseArtifact:Models.ArtifactModel):void => {
                            this.$scope.artifactDescriptions[responseArtifact.artifactLabel] = responseArtifact.description;
                            this.$scope.artifacts = <ArtifactModel[]>_.values(this.$scope.component.deploymentArtifacts);
                            this.$scope.isLoading = false;
                            artifact.selected = !artifact.selected;
                            this.$scope.updateInProgress = false;
                        };

                        let onFailed = (error:any):void => {
                            console.log('Delete artifact returned error:', error);
                            this.$scope.isLoading = false;
                            artifact.selected = !artifact.selected;
                            this.$scope.updateInProgress = false;
                        };

                        this.setArtifact(artifact);
                        this.$scope.component.addOrUpdateArtifact(artifact).then(onSuccess, onFailed);
                    } else {
                        artifact.selected = !artifact.selected;

                    }
                }
            };

            this.$scope.delete = (artifact:Models.ArtifactModel):void => {
                let onOk = ():void => {
                    this.$scope.isLoading = true;
                    let onSuccess = ():void => {
                        this.$scope.isLoading = false;
                        this.$scope.artifacts = <ArtifactModel[]>_.values(this.$scope.component.deploymentArtifacts);
                    };

                    let onFailed = (error:any):void => {
                        this.$scope.isLoading = false;
                        console.log('Delete artifact returned error:', error);
                    };

                    this.$scope.component.deleteArtifact(artifact.uniqueId, artifact.artifactLabel).then(onSuccess, onFailed);
                };

                let title:string = self.$filter('translate')("ARTIFACT_VIEW_DELETE_MODAL_TITLE");
                let message:string = self.$filter('translate')("ARTIFACT_VIEW_DELETE_MODAL_TEXT", "{'name': '" + artifact.artifactDisplayName + "'}");
                this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
            };
        };

        public save = (callback:Function):void => {
            this.updateAll();
            this.$scope.setComponent(this.$scope.component);
            callback(true);
        };

        public back = (callback:Function):void => {
            this.$scope.setComponent(this.$scope.component);
            callback(true);
        }
    }
}
