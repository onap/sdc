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
    import Resource = Sdc.Models.Components.Resource;

    export interface IEditArtifactModel {
        artifactResource: Models.ArtifactModel;
        artifactTypes: Array<string>;
        artifactFile: any;
    }

    export interface IArtifactResourceFormViewModelScope extends ng.IScope {
        forms:any;
        $$childTail: any;
        isNew: boolean;
        isLoading: boolean;
        validationPattern: RegExp;
        urlValidationPattern: RegExp;
        labelValidationPattern: RegExp;
        integerValidationPattern: RegExp;
        commentValidationPattern: RegExp;
        artifactType: string;
        editArtifactResourceModel: IEditArtifactModel;
        defaultHeatTimeout: number;
        validExtensions: any;
        originalArtifactName: string;
        editForm: ng.IFormController;
        footerButtons: Array<any>;
        modalInstanceArtifact:ng.ui.bootstrap.IModalServiceInstance;

        fileExtensions():string;
        save(doNotCloseModal?:boolean): void;
        saveAndAnother(): void;
        close(): void;
        getOptions(): Array<string>;
        isDeploymentHeat(): boolean;
        onFileChange(): void;
        setDefaultTimeout(): void;
        openEditEnvParametersModal(artifact:Models.ArtifactModel):void;
        getFormTitle():string;
        fileUploadRequired():string;
        isArtifactOwner():boolean;
    }

    export class ArtifactResourceFormViewModel {

        private artifactArr:Array<Models.ArtifactModel>;

        static '$inject' = [
            '$scope',
            '$modalInstance',
            'artifact',
            'Sdc.Services.CacheService',
            'ValidationPattern',
            'UrlValidationPattern',
            'LabelValidationPattern',
            'IntegerValidationPattern',
            'CommentValidationPattern',
            'ValidationUtils',
            '$base64',
            '$state',
            'ArtifactsUtils',
            '$modal',
            '$templateCache',
            'component'
        ];

        private formState:Utils.Constants.FormState;
        private entityId:string;

        constructor(private $scope:IArtifactResourceFormViewModelScope,
                    private $modalInstance:ng.ui.bootstrap.IModalServiceInstance,
                    private artifact:Models.ArtifactModel,
                    private cacheService:Services.CacheService,
                    private ValidationPattern:RegExp,
                    private UrlValidationPattern:RegExp,
                    private LabelValidationPattern:RegExp,
                    private IntegerValidationPattern : RegExp,
                    private CommentValidationPattern: RegExp,
                    private ValidationUtils: Sdc.Utils.ValidationUtils,
                    private $base64:any,
                    private $state:any,
                    private artifactsUtils:Sdc.Utils.ArtifactsUtils,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $templateCache:ng.ITemplateCacheService,
                    private component:Models.Components.Component) {


            this.entityId = this.component.uniqueId;
            this.artifactArr = [];
            this.formState = angular.isDefined(artifact.artifactLabel) ? Utils.Constants.FormState.UPDATE : Utils.Constants.FormState.CREATE;
            this.initScope();
        }

        private initEntity = ():void => {
            this.$scope.editArtifactResourceModel.artifactResource = this.artifact;
            this.$scope.originalArtifactName = this.artifact.artifactName;
        };


        private initFooterButtons = ():void =>{

            this.$scope.footerButtons = [
                {'name': 'Done', 'css': 'blue', 'callback': this.$scope.save}
            ];
            if (this.$scope.isNew){
                this.$scope.footerButtons.push( {'name': 'Add Another', 'css': 'grey', 'disabled': !this.$scope.isNew && 'deployment' === this.$scope.artifactType, 'callback': this.$scope.saveAndAnother});
            }

        };


        private initArtifactTypes = ():void => {

            let artifactTypes:any = this.cacheService.get('UIConfiguration');

            if('deployment' === this.$scope.artifactType) {

                this.$scope.validExtensions = ('HEAT_ENV' == this.artifact.artifactType||this.component.selectedInstance)?//to remove the first condition?
                    artifactTypes.artifacts.deployment.resourceInstanceDeploymentArtifacts
                    : this.component.isResource() ? artifactTypes.artifacts.deployment.resourceDeploymentArtifacts
                    : artifactTypes.artifacts.deployment.serviceDeploymentArtifacts;

                if(this.$scope.validExtensions) {
                    this.$scope.editArtifactResourceModel.artifactTypes = Object.keys(this.$scope.validExtensions);
                }
                this.$scope.defaultHeatTimeout = artifactTypes.defaultHeatTimeout;
                if(this.$scope.isNew) {
                    let isHeat='HEAT_ENV' == this.artifact.artifactType;
                    _.remove(this.$scope.editArtifactResourceModel.artifactTypes, (item:string)=> {
                        return 'HEAT' == item.substring(0,4)||(!isHeat && item == "VF_MODULES_METADATA") ||
                            _.has(Utils.Constants.ArtifactType.THIRD_PARTY_RESERVED_TYPES, item);
                    });
                }

            }if (this.$scope.artifactType === 'normal') {
                this.$scope.editArtifactResourceModel.artifactTypes = artifactTypes.artifacts.other.map((element:any)=> {
                    return element.name;
                });
                _.remove(this.$scope.editArtifactResourceModel.artifactTypes, (item:string)=> {
                    return _.has(Utils.Constants.ArtifactType.THIRD_PARTY_RESERVED_TYPES, item) ||
                           _.has(Utils.Constants.ArtifactType.TOSCA, item);
                })
            }

            if(this.component.isResource() &&   (<Resource>this.component).isCsarComponent()) {
                _.remove(this.$scope.editArtifactResourceModel.artifactTypes, (item:string) => {
                    return this.artifactsUtils.isLicenseType(item);
                })
            }

        };

        private initEditArtifactResourceModel = ():void => {
            this.$scope.editArtifactResourceModel = {
                artifactResource: null,
                artifactTypes: null,
                artifactFile:{}
            };

            this.initEntity();
        };

        private initScope = ():void => {

            this.$scope.validationPattern = this.ValidationPattern;
            this.$scope.urlValidationPattern = this.UrlValidationPattern;
            this.$scope.labelValidationPattern = this.LabelValidationPattern;
            this.$scope.integerValidationPattern = this.IntegerValidationPattern;
            this.$scope.commentValidationPattern = this.CommentValidationPattern;
            this.$scope.isLoading = false;
            this.$scope.isNew = (this.formState === Utils.Constants.FormState.CREATE);
            this.$scope.artifactType = this.artifactsUtils.getArtifactTypeByState(this.$state.current.name);
            this.$scope.modalInstanceArtifact = this.$modalInstance;

            this.initEditArtifactResourceModel();
            this.initArtifactTypes();

            // In case of edit, show the file name in browse.
            if (this.artifact.artifactName!=="" && 'HEAT_ENV'!==this.artifact.artifactType){
                this.$scope.editArtifactResourceModel.artifactFile = {};
                this.$scope.editArtifactResourceModel.artifactFile.filename = this.artifact.artifactName;
            }

            //scope methods
            this.$scope.isDeploymentHeat = ():boolean => {
                return !this.$scope.isNew && this.$scope.artifactType === 'deployment' &&
                    'HEAT' === this.$scope.editArtifactResourceModel.artifactResource.artifactType.substring(0,4);
            };
            this.$scope.onFileChange = ():void => {
                if(this.$scope.editArtifactResourceModel.artifactFile && this.$scope.editArtifactResourceModel.artifactFile.filename) {
                    this.$scope.editArtifactResourceModel.artifactResource.artifactName = this.$scope.editArtifactResourceModel.artifactFile.filename;
                } else {
                    this.$scope.editArtifactResourceModel.artifactResource.artifactName = this.$scope.originalArtifactName;
                }
            };
            this.$scope.setDefaultTimeout = ():void => {
                if(this.$scope.isDeploymentHeat() && !this.$scope.editArtifactResourceModel.artifactResource.timeout) {
                    this.$scope.editArtifactResourceModel.artifactResource.timeout = this.$scope.defaultHeatTimeout;
                }
            };

            this.$scope.fileExtensions = ():string => {
                let type:string = this.$scope.editArtifactResourceModel.artifactResource.artifactType;
                return  type && this.$scope.validExtensions && this.$scope.validExtensions[type].acceptedTypes ?
                    this.$scope.validExtensions[type].acceptedTypes.join(',') : "";
            };

            this.$scope.save = (doNotCloseModal?:boolean):void => {
                this.$scope.isLoading = true;
                this.$scope.editArtifactResourceModel.artifactResource.description = this.ValidationUtils.stripAndSanitize(this.$scope.editArtifactResourceModel.artifactResource.description);

                if (!this.$scope.isDeploymentHeat()) {
                    this.$scope.editArtifactResourceModel.artifactResource.timeout = null;
                }

                if (this.$scope.editArtifactResourceModel.artifactFile) {
                    this.$scope.editArtifactResourceModel.artifactResource.payloadData = this.$scope.editArtifactResourceModel.artifactFile.base64;
                    this.$scope.editArtifactResourceModel.artifactResource.artifactName = this.$scope.editArtifactResourceModel.artifactFile.filename;
                }

                let onFaild = (response):void => {
                    this.$scope.isLoading = false;
                    console.info('onFaild', response);
                };

                let onSuccess = (artifactResource:Models.ArtifactModel):void => {
                    this.$scope.isLoading = false;
                    this.$scope.originalArtifactName = "";

                    if(this.$scope.isDeploymentHeat()){
                        if(artifactResource.heatParameters) {
                            this.$scope.openEditEnvParametersModal(artifactResource);
                        }
                    }

                    if (!doNotCloseModal) {
                        this.$modalInstance.close();
                        //this.artifactArr = [];
                    } else {
                        this.$scope.editArtifactResourceModel.artifactFile =  null;
                        angular.element("input[type='file']").val(null); //  for support chrome when upload the same file
                        this.artifactsUtils.addAnotherAfterSave(this.$scope);
                    }

                };

                if('HEAT_ENV' == this.artifact.artifactType){
                    this.component.uploadInstanceEnvFile(this.$scope.editArtifactResourceModel.artifactResource).then(onSuccess, onFaild);
                }else if(this.$scope.isArtifactOwner()){
                    this.component.addOrUpdateInstanceArtifact(this.$scope.editArtifactResourceModel.artifactResource).then(onSuccess, onFaild);
                }else {
                    this.component.addOrUpdateArtifact(this.$scope.editArtifactResourceModel.artifactResource).then(onSuccess, onFaild);
                }
            };

            this.$scope.isArtifactOwner = ():boolean=> {
                return this.component.isService() && !!this.component.selectedInstance;
            };

            this.$scope.saveAndAnother = ():void => {
                this.$scope.save(true);
            };

            this.$scope.close = ():void => {
              this.$modalInstance.close();
              this.artifactArr = [];
            };

            this.$scope.fileUploadRequired = ():string => {
                if (this.$scope.editArtifactResourceModel.artifactFile.filename){
                    // This is edit mode
                    return 'false';
                } else {
                    return 'true';
                }
            };

            this.$scope.getFormTitle =(): string =>{
                if('HEAT_ENV' == this.artifact.artifactType){
                    return 'Update HEAT ENV';
                }
                if(this.$scope.isDeploymentHeat()){
                    if(!this.$scope.editArtifactResourceModel.artifactResource.artifactChecksum){
                        return 'Add HEAT Template';
                    }
                    return 'Update HEAT Template';
                }
                if(this.$scope.isNew){
                    return 'Add Artifact';
                }
                return 'Update Artifact';
            };

            this.$scope.openEditEnvParametersModal = (artifactResource:Models.ArtifactModel):void => {

                let modalOptions:ng.ui.bootstrap.IModalSettings = {
                    template: this.$templateCache.get('/app/scripts/view-models/forms/env-parameters-form/env-parameters-form.html'),
                    controller: 'Sdc.ViewModels.EnvParametersFormViewModel',
                    size: 'sdc-md',
                    backdrop: 'static',
                    resolve: {
                        artifact: ():Models.ArtifactModel => {
                            return artifactResource;
                        },
                        component: ():Models.Components.Component => {
                            return  this.component;
                        }
                    }
                };

                let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
                modalInstance
                    .result
                    .then(():void => {
                    });
            };

            this.$scope.forms = {};

            this.initFooterButtons();


            this.$scope.$watch("forms.editForm.$invalid", (newVal, oldVal) => {
                this.$scope.footerButtons[0].disabled = this.$scope.forms.editForm.$invalid;
                if(this.$scope.isNew){
                    this.$scope.footerButtons[1].disabled = this.$scope.forms.editForm.$invalid;
                }
            });

        }

    }
}
