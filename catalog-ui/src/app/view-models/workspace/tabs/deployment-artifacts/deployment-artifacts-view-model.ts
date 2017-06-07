//@require "./*.html"
'use strict';
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {ArtifactModel, ArtifactGroupModel, Resource} from "app/models";
import {ArtifactsUtils, ModalsHandler, ValidationUtils} from "app/utils";
import {ComponentServiceNg2} from "app/ng2/services/component-services/component.service";
import {ComponentGenericResponse} from "../../../../ng2/services/responses/component-generic-response";

interface IDeploymentArtifactsViewModelScope extends IWorkspaceViewModelScope {
    tableHeadersList:Array<any>;
    reverse:boolean;
    sortBy:string;
    artifacts:Array<ArtifactModel>;
    editForm:ng.IFormController;
    isLoading:boolean;
    artifactDescriptions:any;
    selectedArtifactId:string;
    popoverTemplate:string;

    addOrUpdate(artifact:ArtifactModel):void;
    updateSelectedArtifact():void;
    delete(artifact:ArtifactModel):void;
    sort(sortBy:string):void;
    noArtifactsToShow():boolean;
    getValidationPattern(validationType:string, parameterType?:string):RegExp;
    validateJson(json:string):boolean;
    resetValue(parameter:any):void;
    viewModeOrCsarComponent():boolean;
    isLicenseArtifact(artifact:ArtifactModel):void;
    getEnvArtifact(heatArtifact:ArtifactModel):ArtifactModel;
    getEnvArtifactName(artifact:ArtifactModel):string;
    openEditEnvParametersModal(artifact:ArtifactModel):void;
    openDescriptionPopover(artifactId:string):void;
    closeDescriptionPopover():void;
}

export class DeploymentArtifactsViewModel {

    static '$inject' = [
        '$scope',
        '$templateCache',
        '$filter',
        'ValidationUtils',
        'ArtifactsUtils',
        'ModalsHandler',
        'ComponentServiceNg2'
    ];

    constructor(private $scope:IDeploymentArtifactsViewModelScope,
                private $templateCache:ng.ITemplateCacheService,
                private $filter:ng.IFilterService,
                private validationUtils:ValidationUtils,
                private artifactsUtils:ArtifactsUtils,
                private ModalsHandler:ModalsHandler,
                private ComponentServiceNg2: ComponentServiceNg2) {
        this.initScope();
        this.$scope.updateSelectedMenuItem();
    }

    private initDescriptions = ():void => {
        this.$scope.artifactDescriptions = {};
        _.forEach(this.$scope.component.deploymentArtifacts, (artifact:ArtifactModel):void => {
            this.$scope.artifactDescriptions[artifact.artifactLabel] = artifact.description;
        });
    };

    private setArtifact = (artifact:ArtifactModel):void => {
        if (!artifact.description || !this.$scope.getValidationPattern('string').test(artifact.description)) {
            artifact.description = this.$scope.artifactDescriptions[artifact.artifactLabel];
        }
    };

    private initScopeArtifacts = ()=> {
        this.$scope.artifacts = <ArtifactModel[]>_.values(this.$scope.component.deploymentArtifacts);
        _.forEach(this.$scope.artifacts, (artifact:ArtifactModel):void => {
            artifact.envArtifact = this.getEnvArtifact(artifact);
        });
    };

    private initArtifacts = (loadFromServer:boolean):void => {
        if (loadFromServer) {
            this.$scope.isLoading = true;
            this.ComponentServiceNg2.getComponentDeploymentArtifacts(this.$scope.component).subscribe((response:ComponentGenericResponse) => {
                this.$scope.component.deploymentArtifacts = response.deploymentArtifacts;
                this.initScopeArtifacts();
                this.$scope.isLoading = false;
            });
        } else {
            this.initScopeArtifacts();
        }

    };

    private getEnvArtifact = (heatArtifact:ArtifactModel):ArtifactModel=> {
        return _.find(this.$scope.artifacts, (item:ArtifactModel)=> {
            return item.generatedFromId === heatArtifact.uniqueId;
        });
    };

    private getCurrentArtifact = ():ArtifactModel => {
        if (!this.$scope.selectedArtifactId) {
            return null;
        }
        let artifact:ArtifactModel = this.$scope.artifacts.filter((art) => {
            return art.uniqueId == this.$scope.selectedArtifactId;
        })[0];
        return artifact;
    }

    private initScope = ():void => {
        let self = this;
        this.$scope.isLoading = false;
        this.$scope.selectedArtifactId = null;
        this.initDescriptions();
        if(this.$scope.component.deploymentArtifacts) {
            this.initArtifacts(false);
        } else {
            this.initArtifacts(true);
        }
        this.$scope.setValidState(true);

        this.$scope.tableHeadersList = [
            {title: 'Name', property: 'artifactDisplayName'},
            {title: 'Type', property: 'artifactType'},
            {title: 'Deployment timeout', property: 'timeout'},
            {title: 'Version', property: 'artifactVersion'},
            {title: 'UUID', property: 'artifactUUID'}
        ];

        this.$templateCache.put("deployment-artifacts-description-popover.html", require('app/view-models/workspace/tabs/deployment-artifacts/deployment-artifacts-description-popover.html'));
        this.$scope.popoverTemplate = "deployment-artifacts-description-popover.html";

        this.$scope.isLicenseArtifact = (artifact:ArtifactModel):boolean => {
            let isLicense:boolean = false;
            if (this.$scope.component.isResource() && (<Resource>this.$scope.component).isCsarComponent()) {

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
            if (!json) {
                return true;
            }
            return this.validationUtils.validateJson(json);
        };

        this.$scope.viewModeOrCsarComponent = ():boolean => {
            return this.$scope.isViewMode() || (this.$scope.component.isResource() && (<Resource>this.$scope.component).isCsarComponent());
        };

        this.$scope.addOrUpdate = (artifact:ArtifactModel):void => {
            artifact.artifactGroupType = 'DEPLOYMENT';
            let artifactCopy = new ArtifactModel(artifact);

            let success = (response:any):void => {
                this.$scope.artifactDescriptions[artifactCopy.artifactLabel] = artifactCopy.description;
                this.initArtifacts(true);
                //  this.$scope.artifacts = _.values(this.$scope.component.deploymentArtifacts);
            };

            let error = (err:any):void => {
                console.log(err);
                this.initArtifacts(true);
                //  self.$scope.artifacts = _.values(self.$scope.component.deploymentArtifacts);
            };

            this.ModalsHandler.openArtifactModal(artifactCopy, this.$scope.component).then(success, error);
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

        this.$scope.updateSelectedArtifact = ():void => {
            if (!this.$scope.isViewMode() && !this.$scope.isLoading) {
                let artifact:ArtifactModel = this.getCurrentArtifact();
                this.setArtifact(artifact); //resets artifact description to original value if invalid.
                if (artifact && artifact.originalDescription != artifact.description) {
                    this.$scope.isLoading = true;
                    let onSuccess = (responseArtifact:ArtifactModel):void => {
                        this.$scope.artifactDescriptions[responseArtifact.artifactLabel] = responseArtifact.description;
                        // this.$scope.artifacts = _.values(this.$scope.component.deploymentArtifacts);
                        this.initArtifacts(true);
                        this.$scope.isLoading = false;
                    };

                    let onFailed = (error:any):void => {
                        console.log('Delete artifact returned error:', error);
                        this.$scope.isLoading = false;
                    };

                    this.$scope.component.addOrUpdateArtifact(artifact).then(onSuccess, onFailed);
                }
            }
        };

        this.$scope.delete = (artifact:ArtifactModel):void => {
            let onOk = ():void => {
                this.$scope.isLoading = true;
                let onSuccess = ():void => {
                    this.$scope.isLoading = false;
                    this.initArtifacts(true);
                    //this.$scope.artifacts = _.values(this.$scope.component.deploymentArtifacts);
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

        this.$scope.getEnvArtifactName = (artifact:ArtifactModel):string => {
            let envArtifact = this.$scope.getEnvArtifact(artifact);
            if (envArtifact) {
                return envArtifact.artifactDisplayName;
            }
        };

        this.$scope.openEditEnvParametersModal = (artifact:ArtifactModel):void => {
            this.ModalsHandler.openEditEnvParametersModal(artifact, this.$scope.component).then(()=> {
                this.initArtifacts(true);
            }, ()=> {
                this.initArtifacts(true);
            });
        };

        this.$scope.openDescriptionPopover = (artifactId:string):void => {
            if (this.$scope.selectedArtifactId && this.$scope.selectedArtifactId != artifactId) {
                this.$scope.updateSelectedArtifact();
            }
            this.$scope.selectedArtifactId = artifactId;

        };

        this.$scope.closeDescriptionPopover = ():void => {
            if (this.$scope.selectedArtifactId) {
                this.$scope.updateSelectedArtifact();
                this.$scope.selectedArtifactId = null;
            }
        };
    };
}
