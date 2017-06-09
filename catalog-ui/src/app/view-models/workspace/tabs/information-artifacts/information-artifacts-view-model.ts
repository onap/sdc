'use strict';
import {ModalsHandler} from "app/utils";
import {SharingService} from "app/services";
import {IAppConfigurtaion, ArtifactModel, IFileDownload} from "app/models";
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {ComponentServiceNg2} from "../../../../ng2/services/component-services/component.service";
import {ArtifactGroupModel} from "../../../../models/artifacts";
import {ComponentGenericResponse} from "../../../../ng2/services/responses/component-generic-response";

export interface IInformationArtifactsScope extends IWorkspaceViewModelScope {
    artifacts:Array<ArtifactModel>;
    tableHeadersList:Array<any>;
    artifactType:string;
    isResourceInstance:boolean;
    downloadFile:IFileDownload;
    isLoading:boolean;
    sortBy:string;
    reverse:boolean;

    getTitle():string;
    addOrUpdate(artifact:ArtifactModel):void;
    delete(artifact:ArtifactModel):void;
    download(artifact:ArtifactModel):void;
    clickArtifactName(artifact:any):void;
    openEditEnvParametersModal(artifactResource:ArtifactModel):void;
    sort(sortBy:string):void;
    showNoArtifactMessage():boolean;
}

export class InformationArtifactsViewModel {

    static '$inject' = [
        '$scope',
        '$filter',
        '$state',
        'sdcConfig',
        'ModalsHandler',
        'ComponentServiceNg2'
    ];

    constructor(private $scope:IInformationArtifactsScope,
                private $filter:ng.IFilterService,
                private $state:any,
                private sdcConfig:IAppConfigurtaion,
                private ModalsHandler:ModalsHandler,
                private ComponentServiceNg2: ComponentServiceNg2) {
        this.initInformationalArtifacts();
        this.$scope.updateSelectedMenuItem();
    }

    private initInformationalArtifacts = ():void => {
        if(!this.$scope.component.artifacts) {
            this.$scope.isLoading = true;
            this.ComponentServiceNg2.getComponentInformationalArtifacts(this.$scope.component).subscribe((response:ComponentGenericResponse) => {
                this.$scope.component.artifacts = response.artifacts;
                this.initScope();
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
            {title: 'Version', property: 'artifactVersion'},
            {title: 'UUID', property: 'artifactUUID'}
        ];

        this.$scope.artifacts = <ArtifactModel[]>_.values(this.$scope.component.artifacts);
        this.$scope.sort = (sortBy:string):void => {
            this.$scope.reverse = (this.$scope.sortBy === sortBy) ? !this.$scope.reverse : false;
            this.$scope.sortBy = sortBy;
        };


        this.$scope.addOrUpdate = (artifact:ArtifactModel):void => {
            artifact.artifactGroupType = 'INFORMATIONAL';
            this.ModalsHandler.openArtifactModal(artifact, this.$scope.component).then(() => {
                this.$scope.artifacts = <ArtifactModel[]>_.values(this.$scope.component.artifacts);
            });
        };

        this.$scope.showNoArtifactMessage = ():boolean => {
            let artifacts:any = [];
            artifacts = _.filter(this.$scope.artifacts, (artifact:ArtifactModel)=> {
                return artifact.esId;
            });

            if (artifacts.length === 0) {
                return true;
            }
            return false;
        };

        this.$scope.delete = (artifact:ArtifactModel):void => {

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

            let title:string = this.$filter('translate')("ARTIFACT_VIEW_DELETE_MODAL_TITLE");
            let message:string = this.$filter('translate')("ARTIFACT_VIEW_DELETE_MODAL_TEXT", "{'name': '" + artifact.artifactDisplayName + "'}");
            this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
        };

        this.$scope.clickArtifactName = (artifact:any) => {
            if (!artifact.esId) {
                this.$scope.addOrUpdate(artifact);
            }

        };
    }
}
