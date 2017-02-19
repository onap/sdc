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
    import Resource = Sdc.Models.Components.Resource;

    export interface IEditArtifactStepModel {
        artifactResource: Models.ArtifactModel;
        artifactTypes: Array<string>;
        artifactsFormList: any;
        artifactFile:any;
    }

    export interface IArtifactResourceFormStepViewModelScope extends ng.IScope {
        editForm:ng.IFormController;
        forms:any;
        footerButtons: Array<any>;
        isNew: boolean;
        isPlaceHolderArtifact:boolean;
        isLoading: boolean;
        validationPattern: RegExp;
        urlValidationPattern: RegExp;
        labelValidationPattern: RegExp;
        integerValidationPattern: RegExp;
        commentValidationPattern: RegExp;
        artifactType: string;
        artifactGroupType:string;
        editArtifactResourceModel: IEditArtifactStepModel;
        defaultHeatTimeout: number;
        validExtensions: any;
        originalArtifactName: string;
        modalInstanceArtifact:ng.ui.bootstrap.IModalServiceInstance;
        selectedArtifact:string;

        fileExtensions():string;
        save(): void;
        close(): void;
        changeArtifact(selectedArtifact:string):void;
        getOptions(): Array<string>;
        removeInputLabel(): void;
        fileUploadRequired():string;
        isDeploymentHeat():boolean;
        setDefaultTimeout():void;
        getFormTitle():string;
    }

    export class ArtifactResourceFormStepViewModel {

        static '$inject' = [
            '$scope',
            '$modalInstance',
            'artifact',
            'component',
            'Sdc.Services.CacheService',
            'ValidationPattern',
            'UrlValidationPattern',
            'LabelValidationPattern',
            'IntegerValidationPattern',
            'CommentValidationPattern',
            'ValidationUtils',
            'ArtifactsUtils',
            '$state',
            '$modal',
            '$templateCache'
        ];

        private formState:Utils.Constants.FormState;
        private entityId:string;

        constructor(private $scope:IArtifactResourceFormStepViewModelScope,
                    private $modalInstance:ng.ui.bootstrap.IModalServiceInstance,
                    private artifact:Models.ArtifactModel,
                    private component:Models.Components.Component,
                    private cacheService:Services.CacheService,
                    private ValidationPattern:RegExp,
                    private UrlValidationPattern:RegExp,
                    private LabelValidationPattern:RegExp,
                    private IntegerValidationPattern:RegExp,
                    private CommentValidationPattern:RegExp,
                    private ValidationUtils:Sdc.Utils.ValidationUtils,
                    private ArtifactsUtils:Sdc.Utils.ArtifactsUtils,
                    private $state:any,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $templateCache:ng.ITemplateCacheService) {


            this.entityId = this.component.uniqueId;
            this.formState = angular.isDefined(artifact.artifactLabel) ? Utils.Constants.FormState.UPDATE : Utils.Constants.FormState.CREATE;

            this.initScope();
            this.initEditArtifactResourceModel();
            this.initComponent();
            this.initArtifactTypes();
        }

        private initEditArtifactResourceModel = ():void => {
            this.$scope.editArtifactResourceModel = {
                artifactResource: null,
                artifactTypes: null,
                artifactsFormList: {},
                artifactFile: {}
            }
        };

        private initComponent = ():void => {
            this.$scope.editArtifactResourceModel.artifactResource = this.artifact;
            this.$scope.originalArtifactName = this.artifact.artifactName;
            let artifacts:any = Utils.Constants.ArtifactGroupType.INFORMATION === this.artifact.artifactGroupType?
                this.component.artifacts : this.component.deploymentArtifacts;
            this.$scope.editArtifactResourceModel.artifactsFormList = _.pick(artifacts, (artifact:Models.ArtifactModel)=> {
                return artifact.artifactLabel && !artifact.esId;
            });
            this.$scope.editArtifactResourceModel.artifactFile.filename= this.artifact.artifactName?this.artifact.artifactName:'';

            if(this.artifact.artifactLabel){//this is edit mode
                this.$scope.editArtifactResourceModel.artifactsFormList[this.artifact.artifactLabel]= this.artifact;
                this.$scope.selectedArtifact = this.artifact.artifactDisplayName;
            }
        };

        private initArtifactTypes = ():void => {
            let artifactTypes:any = this.cacheService.get('UIConfiguration');

            if (Utils.Constants.ArtifactGroupType.INFORMATION === this.artifact.artifactGroupType) {
                this.$scope.editArtifactResourceModel.artifactTypes = artifactTypes.artifacts.other.map((element:any)=> {
                    return element.name;
                });
                _.remove(this.$scope.editArtifactResourceModel.artifactTypes, (item:string)=> {
                    return _.has(Utils.Constants.ArtifactType.THIRD_PARTY_RESERVED_TYPES, item) ||
                           _.has(Utils.Constants.ArtifactType.TOSCA, item);
                })
            }else if(Utils.Constants.ArtifactGroupType.DEPLOYMENT === this.artifact.artifactGroupType) {

                this.$scope.validExtensions = artifactTypes.artifacts.deployment.resourceDeploymentArtifacts;
                if(this.$scope.validExtensions) {
                    this.$scope.editArtifactResourceModel.artifactTypes = Object.keys(this.$scope.validExtensions);
                }
                this.$scope.defaultHeatTimeout = artifactTypes.defaultHeatTimeout;

                if(!this.$scope.isPlaceHolderArtifact) {
                    _.remove(this.$scope.editArtifactResourceModel.artifactTypes, (item:string)=> {
                        return Utils.Constants.ArtifactType.HEAT == item.substring(0,4) ||
                            _.has(Utils.Constants.ArtifactType.THIRD_PARTY_RESERVED_TYPES, item);
                    })
                }

                if(this.component.isResource() && (<Resource>this.component).isCsarComponent()) {
                    _.remove(this.$scope.editArtifactResourceModel.artifactTypes, (item:string) => {
                        return this.ArtifactsUtils.isLicenseType(item);
                    })
                }
            }
        };

        private initScope = ():void => {

            this.$scope.modalInstanceArtifact = this.$modalInstance;
            this.$scope.validationPattern = this.ValidationPattern;
            this.$scope.urlValidationPattern = this.UrlValidationPattern;
            this.$scope.labelValidationPattern = this.LabelValidationPattern;
            this.$scope.integerValidationPattern = this.IntegerValidationPattern;
            this.$scope.commentValidationPattern = this.CommentValidationPattern;
            this.$scope.isLoading = false;
            this.$scope.isPlaceHolderArtifact = true;
            this.$scope.isNew = (this.formState === Utils.Constants.FormState.CREATE);
            this.$scope.artifactGroupType = this.artifact.artifactGroupType;
            this.$scope.selectedArtifact = '?';

            this.$scope.fileExtensions = ():string => {
                let type:string = this.$scope.editArtifactResourceModel.artifactResource.artifactType;
                return  type && this.$scope.validExtensions && this.$scope.validExtensions[type].acceptedTypes ?
                    this.$scope.validExtensions[type].acceptedTypes.join(',') : "";
            };

            this.$scope.removeInputLabel = ():void => {
                this.$scope.isPlaceHolderArtifact = true;
            };

            this.$scope.fileUploadRequired = ():string => {
                if (this.$scope.isNew===false){
                    return 'false'; // This is edit mode
                } else {
                    return 'true';
                }
            };

            this.$scope.isDeploymentHeat = ():boolean =>{
                return Utils.Constants.ArtifactGroupType.DEPLOYMENT === this.artifact.artifactGroupType
                    && this.$scope.editArtifactResourceModel.artifactResource
                    && this.$scope.editArtifactResourceModel.artifactResource.artifactType
                    && Utils.Constants.ArtifactType.HEAT === this.$scope.editArtifactResourceModel.artifactResource.artifactType.substring(0,4);
            };

            this.$scope.getFormTitle =(): string =>{
                let title:string = this.artifact.esId? 'Update':'Add';
                if (Utils.Constants.ArtifactGroupType.DEPLOYMENT === this.artifact.artifactGroupType) {
                        title += ' Deployment';
                }
                title += ' Artifact';
                return title;
            };

            this.$scope.setDefaultTimeout = ():void => {
                if(!this.$scope.editArtifactResourceModel.artifactResource.timeout) {
                    this.$scope.editArtifactResourceModel.artifactResource.timeout = this.$scope.defaultHeatTimeout;
                }
            };

            this.$scope.changeArtifact = (selectedArtifact:string):void => {
                let tempArtifact:Models.ArtifactModel = this.$scope.editArtifactResourceModel.artifactResource;
                this.$scope.editArtifactResourceModel.artifactResource = null;

                if (selectedArtifact && selectedArtifact != '' && selectedArtifact != '?') {
                    let artifactResource = <Models.ArtifactModel>_.find(this.$scope.editArtifactResourceModel.artifactsFormList,{'artifactDisplayName':selectedArtifact});
                    this.$scope.editArtifactResourceModel.artifactResource = new Sdc.Models.ArtifactModel(artifactResource);
                    this.$scope.originalArtifactName = this.$scope.editArtifactResourceModel.artifactResource.artifactName;
                    this.$scope.isPlaceHolderArtifact = true;
                    if(this.$scope.isDeploymentHeat()){
                        this.$scope.setDefaultTimeout();
                    }
                } else if (selectedArtifact === "") {
                    //this.$scope.editArtifactResourceModel.artifactFile = {};
                    this.$scope.editArtifactResourceModel.artifactResource = <Models.ArtifactModel>{};
                    this.$scope.editArtifactResourceModel.artifactResource.artifactGroupType = this.$scope.artifactGroupType;
                    this.$scope.isPlaceHolderArtifact = false;
                }

                if (_.size(this.$scope.editArtifactResourceModel.artifactFile) && this.$scope.editArtifactResourceModel.artifactResource) {
                    this.$scope.editArtifactResourceModel.artifactResource.artifactName = this.$scope.editArtifactResourceModel.artifactFile.filename;
                }
                if(tempArtifact && tempArtifact.description != ''){
                    this.$scope.editArtifactResourceModel.artifactResource.description = tempArtifact.description;
                }

                this.initArtifactTypes();
                this.$scope.isNew = true;

            };


            this.$scope.save = ():void => {
                this.$scope.isLoading = true;
                this.$scope.editArtifactResourceModel.artifactResource.description =
                this.ValidationUtils.stripAndSanitize(this.$scope.editArtifactResourceModel.artifactResource.description);

                if (this.$scope.editArtifactResourceModel.artifactFile) {
                    this.$scope.editArtifactResourceModel.artifactResource.payloadData = this.$scope.editArtifactResourceModel.artifactFile.base64;
                    this.$scope.editArtifactResourceModel.artifactResource.artifactName = this.$scope.editArtifactResourceModel.artifactFile.filename;
                }

                let onFailed = (response) => {
                    this.$scope.isLoading = false;
                    console.info('onFaild', response);
                    this.$scope.editArtifactResourceModel.artifactResource.esId = undefined;
                };

                let onSuccess = () => {
                    this.$scope.isLoading = false;
                    this.$scope.originalArtifactName = "";
                    this.$modalInstance.close();

                };

                this.component.addOrUpdateArtifact(this.$scope.editArtifactResourceModel.artifactResource).then(onSuccess, onFailed);
            };


            this.$scope.close = ():void => {
                this.$modalInstance.dismiss();
            };

            //new form layout for import asset
            this.$scope.forms = {};
            this.$scope.footerButtons = [
                {'name': this.artifact.esId ? 'Update' : 'Add', 'css': 'blue', 'callback': this.$scope.save},
                {'name': 'Cancel', 'css': 'grey', 'callback': this.$scope.close}
            ];

            this.$scope.$watch("forms.editForm.$invalid", (newVal, oldVal) => {
                this.$scope.footerButtons[0].disabled = this.$scope.forms.editForm.$invalid;
            });

        }

    }
}
