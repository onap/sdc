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

module Sdc.ViewModels {
    'use strict';

      export interface IEnvParametersFormViewModelScope extends ng.IScope {
          isLoading: boolean;
          type:string;
          heatParameters:any;
          editForm:ng.IFormController;
          artifactResource:Models.ArtifactModel;
          saveButton: Array<any>;
          envParametersModal: ng.ui.bootstrap.IModalServiceInstance;

          getValidationPattern(type:string):RegExp;
          isInstance():boolean;
          validateJson(json:string):boolean;
          close(): void;
          save():void;
        }

        export class EnvParametersFormViewModel {


            static '$inject' = [
                '$scope',
                '$state',
                '$modalInstance',
                'artifact',
               // 'ArtifactsUtils',
                'ValidationUtils',
                'component'
            ];


            constructor(private $scope:IEnvParametersFormViewModelScope,
                        private $state:any,
                        private $modalInstance:ng.ui.bootstrap.IModalServiceInstance,
                        private artifact:Models.ArtifactModel,
                       // private artifactsUtils:Sdc.Utils.ArtifactsUtils,
                        private ValidationUtils: Sdc.Utils.ValidationUtils,
                        private component:Models.Components.Component) {


                this.initScope();
            }

            private updateInstanceHeat = ():void => {
                let success =(responseArtifact:Models.ArtifactModel): void => {
                    this.$scope.isLoading = false;
                    this.$modalInstance.close();
                };

                let error = ():void => {
                    this.$scope.isLoading = false;
                    console.info('Failed to load save artifact');
                };

                this.component.addOrUpdateInstanceArtifact(this.$scope.artifactResource).then(success, error);

            };

            private initScope = ():void => {
                this.$scope.envParametersModal = this.$modalInstance;
                this.$scope.artifactResource= this.artifact;
                this.$scope.heatParameters = angular.copy(this.artifact.heatParameters);

                this.$scope.getValidationPattern = (validationType:string , parameterType?:string):RegExp => {
                   return this.ValidationUtils.getValidationPattern(validationType, parameterType);
                };

                this.$scope.validateJson = (json:string):boolean => {
                    if(!json){
                        return true;
                    }
                    return this.ValidationUtils.validateJson(json);
                };

                this.$scope.isInstance =(): boolean =>{
                    return !!this.component.selectedInstance;
                };


                this.$scope.save = ():void => {
                    this.$scope.isLoading = true;
                    this.artifact.heatParameters = this.$scope.heatParameters;
                    this.artifact.heatParameters.forEach((parameter:any):void => {
                        /* if ("" === parameter.currentValue) {
                            parameter.currentValue = null;
                        }else */
                        if(!parameter.currentValue && parameter.defaultValue) {
                            parameter.currentValue = parameter.defaultValue;
                        }
                    });

                    if(this.$scope.isInstance()){
                        this.updateInstanceHeat();
                        return;
                    }

                    let success =(responseArtifact:Models.ArtifactModel): void => {
                        this.$scope.isLoading = false;
                        this.$modalInstance.close();

                    };

                    let error = ():void => {
                        this.$scope.isLoading = false;
                        console.info('Failed to load save artifact');
                    };

                    this.component.addOrUpdateArtifact(this.$scope.artifactResource).then(success, error);
                };

                this.$scope.saveButton = [
                    {'name': 'Save', 'css': 'blue', 'callback': this.$scope.save}
                ];

                this.$scope.close = ():void => {
                    //this.artifact.heatParameters.forEach((parameter:any):void => {
                    //    if (!parameter.currentValue && parameter.defaultValue) {
                    //        parameter.currentValue = parameter.defaultValue;
                    //    }
                    //});
                    this.$modalInstance.dismiss();
                };

            };
        }
}
