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
import {
    ArtifactModel,
    Service,
    IAppConfigurtaion,
    Resource,
    Component,
    ComponentInstance,
    ArtifactGroupModel,
    IFileDownload
} from "app/models";
import {ICompositionViewModelScope} from "../../composition-view-model";
import {ArtifactsUtils, ModalsHandler, ArtifactGroupType} from "app/utils";
import {GRAPH_EVENTS} from "app/utils/constants";
import {EventListenerService} from "app/services/event-listener-service";
import {Dictionary} from "../../../../../../utils/dictionary/dictionary";

export interface IArtifactsViewModelScope extends ICompositionViewModelScope {
    artifacts:Array<ArtifactModel>;
    artifactType:string;
    downloadFile:IFileDownload;
    isLoading:boolean;
    allowDeleteAndUpdateArtifactMap:Dictionary<string, boolean>;
    getTitle():string;
    addOrUpdate(artifact:ArtifactModel):void;
    delete(artifact:ArtifactModel):void;
    download(artifact:ArtifactModel):void;
    openEditEnvParametersModal(artifact:ArtifactModel):void;
    getEnvArtifact(heatArtifact:ArtifactModel):any;
    getEnvArtifactName(artifact:ArtifactModel):string;
    isLicenseArtifact(artifact:ArtifactModel):boolean;
    //isVFiArtifact(artifact:ArtifactModel):boolean;
}

export class ResourceArtifactsViewModel {

    static '$inject' = [
        '$scope',
        '$filter',
        '$state',
        'sdcConfig',
        'ArtifactsUtils',
        'ModalsHandler',
        '$q',
        'EventListenerService'
    ];

    constructor(private $scope:IArtifactsViewModelScope,
                private $filter:ng.IFilterService,
                private $state:any,
                private sdcConfig:IAppConfigurtaion,
                private artifactsUtils:ArtifactsUtils,
                private ModalsHandler:ModalsHandler,
                private $q:ng.IQService,
                private eventListenerService: EventListenerService) {

        this.initScope();
    }


    private initArtifactArr = (artifactType:string):void => {
        let artifacts:Array<ArtifactModel> = [];

        if (this.$scope.selectedComponent) {
            if ('interface' == artifactType) {
                let interfaces = this.$scope.currentComponent.interfaces;
                if (interfaces && interfaces.standard && interfaces.standard.operations) {

                    angular.forEach(interfaces.standard.operations, (operation:any, interfaceName:string):void => {
                        let item:ArtifactModel = <ArtifactModel>{};
                        if (operation.implementation) {
                            item = <ArtifactModel> operation.implementation;
                        }
                        item.artifactDisplayName = interfaceName;
                        item.artifactLabel = interfaceName;
                        item.mandatory = false;
                        artifacts.push(item);
                    });
                }
            } else {
                //init normal artifacts, deployment or api artifacts
                let artifactsObj:ArtifactGroupModel;
                switch (artifactType) {
                    case "api":
                        artifactsObj = (<Service>this.$scope.currentComponent).serviceApiArtifacts;
                        break;
                    case "deployment":
                        if (!this.$scope.isComponentInstanceSelected()) {
                            artifactsObj = this.$scope.currentComponent.deploymentArtifacts;
                        } else {
                            artifactsObj = this.$scope.currentComponent.selectedInstance.deploymentArtifacts;
                        }
                        break;
                    default:
                        //artifactsObj = this.$scope.selectedComponent.artifacts;
                        if (!this.$scope.isComponentInstanceSelected()) {
                            artifactsObj = this.$scope.currentComponent.artifacts;
                        } else {
                            artifactsObj = this.$scope.currentComponent.selectedInstance.artifacts;
                        }
                        break;
                }
                _.forEach(artifactsObj, (artifact:ArtifactModel, key) => {
                    artifacts.push(artifact);
                });
            }
        }
        this.$scope.artifacts = artifacts;
        this.$scope.allowDeleteAndUpdateArtifactMap = new Dictionary<string, boolean>();
        _.forEach(this.$scope.artifacts, (artifact:ArtifactModel)=>{
            this.$scope.allowDeleteAndUpdateArtifactMap[artifact.artifactLabel] = this.allowDeleteAndUpdateArtifact(artifact);
        });
        this.$scope.isLoading = false;
    };


    private convertToArtifactUrl = (artifactType:string):string => {

        switch (artifactType) {
            case 'deployment':
                return 'DEPLOYMENT';
            case 'api':
                return 'SERVICE_API';
            default:
                return 'INFORMATIONAL';
        }

    }

    private loadComponentArtifactIfNeeded = (forceLoad?: boolean) => {

        let onGetComponentArtifactsSuccess = (artifacts:ArtifactGroupModel)=> {
            switch (this.$scope.artifactType) {
                case 'deployment':
                    this.$scope.currentComponent.deploymentArtifacts = artifacts;
                    break;
                case 'api':
                    (<Service>this.$scope.currentComponent).serviceApiArtifacts = artifacts;
                    break;
                default:
                    this.$scope.currentComponent.artifacts = artifacts;
                    break;
            }
            this.$scope.isLoading = false;
            this.initArtifactArr(this.$scope.artifactType);
        }

        let onError = ()=> {
            this.$scope.isLoading = false;
        };

        switch (this.$scope.artifactType) {
            case 'deployment':
                if(forceLoad || !this.$scope.currentComponent.deploymentArtifacts) {
                    this.$scope.component.getArtifactByGroupType(this.convertToArtifactUrl(this.$scope.artifactType)).then(onGetComponentArtifactsSuccess, onError);
                } else {
                    this.initArtifactArr(this.$scope.artifactType);
                }

                break;
            case 'api':
                if(!(<Service>this.$scope.currentComponent).serviceApiArtifacts) {
                    this.$scope.component.getArtifactByGroupType(this.convertToArtifactUrl(this.$scope.artifactType)).then(onGetComponentArtifactsSuccess, onError);
                } else {
                    this.initArtifactArr(this.$scope.artifactType);
                }
                break;
            default:
                if(!this.$scope.currentComponent.artifacts) {
                    this.$scope.component.getArtifactByGroupType(this.convertToArtifactUrl(this.$scope.artifactType)).then(onGetComponentArtifactsSuccess, onError);
                } else {
                    this.initArtifactArr(this.$scope.artifactType);
                }
                break;
        }
    }
    private loadArtifacts = (forceLoad?: boolean):void => {

        let onGetInstanceArtifactsSuccess = (artifacts:ArtifactGroupModel)=> {
            switch (this.$scope.artifactType) {
                case 'deployment':
                    this.$scope.currentComponent.selectedInstance.deploymentArtifacts = artifacts;
                    break;
                default:
                    this.$scope.currentComponent.selectedInstance.artifacts = artifacts;
                    break;
            }
            this.initArtifactArr(this.$scope.artifactType);
        };

        let onError = ()=> {
            this.$scope.isLoading = false;
        };

        this.$scope.isLoading = true;
        if (this.$scope.isComponentInstanceSelected()) {
            this.$scope.component.getComponentInstanceArtifactsByGroupType(this.$scope.component.selectedInstance.uniqueId, this.convertToArtifactUrl(this.$scope.artifactType)).then(onGetInstanceArtifactsSuccess, onError);
        } else {
            this.loadComponentArtifactIfNeeded(forceLoad);
        }
    }

    private updateArtifactsIfNeeded = ():void => {
        if (this.$scope.artifactType === "deployment") {
            this.loadArtifacts(true);
        } else {
            this.initArtifactArr(this.$scope.artifactType);
        }
    };

    private openEditArtifactModal = (artifact:ArtifactModel):void => {
        this.ModalsHandler.openArtifactModal(artifact, this.$scope.currentComponent).then(():void => {
            this.updateArtifactsIfNeeded();
        });
    };

    private allowDeleteAndUpdateArtifact = (artifact:ArtifactModel):boolean => {
    if(!this.$scope.isViewMode()){
        if(this.$scope.isComponentInstanceSelected()){//is artifact of instance
            return !this.$scope.selectedComponent.deploymentArtifacts || !this.$scope.selectedComponent.deploymentArtifacts[artifact.artifactLabel];//if the artifact is not from instance parent
        }else{//is artifact of main component
            return (!artifact.isHEAT() && !artifact.isThirdParty() && !this.$scope.isLicenseArtifact(artifact));
        }
    }
    return false;
};

    private initScope = ():void => {

        this.$scope.isLoading = false;
        this.$scope.artifactType = this.artifactsUtils.getArtifactTypeByState(this.$state.current.name);
        this.$scope.getTitle = ():string => {
            return this.artifactsUtils.getTitle(this.$scope.artifactType, this.$scope.currentComponent);
        };

        // Bug 310499 - user should be unable to delete RI artifact. (also talked to David and agreed this function isn't necessary)
        // this.$scope.isVFiArtifact = (artifact:ArtifactModel):boolean=> {
        //     if (artifact.artifactGroupType === ArtifactGroupType.INFORMATION) {//fix DE256847
        //         return this.$scope.currentComponent.artifacts && (!this.$scope.currentComponent.artifacts[artifact.artifactLabel] || !this.$scope.currentComponent.artifacts[artifact.artifactLabel].artifactName);
        //     }
        //     return this.$scope.currentComponent.selectedInstance && this.$scope.currentComponent.selectedInstance.deploymentArtifacts && this.$scope.currentComponent.selectedInstance.deploymentArtifacts[artifact.artifactLabel];
        // };

        this.$scope.addOrUpdate = (artifact:ArtifactModel):void => {
            this.artifactsUtils.setArtifactType(artifact, this.$scope.artifactType);
            let artifactCopy = new ArtifactModel(artifact);
            this.openEditArtifactModal(artifactCopy);
        };


        this.$scope.delete = (artifact:ArtifactModel):void => {

            let onOk = ():void => {
                this.$scope.isLoading = true;
                this.artifactsUtils.removeArtifact(artifact, this.$scope.artifacts);

                let success = (responseArtifact:ArtifactModel):void => {
                    this.initArtifactArr(this.$scope.artifactType);
                    this.$scope.isLoading = false;
                };

                let error = (error:any):void => {
                    console.log('Delete artifact returned error:', error);
                    this.initArtifactArr(this.$scope.artifactType);
                    this.$scope.isLoading = false;
                };
                if (this.$scope.isComponentInstanceSelected()) {
                    this.$scope.currentComponent.deleteInstanceArtifact(artifact.uniqueId, artifact.artifactLabel).then(success, error);
                } else {
                    this.$scope.currentComponent.deleteArtifact(artifact.uniqueId, artifact.artifactLabel).then(success, error);//TODO simulate error (make sure error returns)
                }
            };
            let title:string = this.$filter('translate')("ARTIFACT_VIEW_DELETE_MODAL_TITLE");
            let message:string = this.$filter('translate')("ARTIFACT_VIEW_DELETE_MODAL_TEXT", "{'name': '" + artifact.artifactDisplayName + "'}");
            this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
        };


        this.$scope.getEnvArtifact = (heatArtifact:ArtifactModel):any=> {
            return _.find(this.$scope.artifacts, (item:ArtifactModel)=> {
                return item.generatedFromId === heatArtifact.uniqueId;
            });
        };

        this.$scope.getEnvArtifactName = (artifact:ArtifactModel):string => {
            let envArtifact = this.$scope.getEnvArtifact(artifact);
            if (envArtifact) {
                return envArtifact.artifactDisplayName;
            }
        };

        this.$scope.isLicenseArtifact = (artifact:ArtifactModel):boolean => {
            let isLicense:boolean = false;
            if (this.$scope.component.isResource() && (<Resource>this.$scope.component).isCsarComponent()) {
                isLicense = this.artifactsUtils.isLicenseType(artifact.artifactType);
            }

            return isLicense;
        };

        this.$scope.openEditEnvParametersModal = (artifact:ArtifactModel):void => {
            this.ModalsHandler.openEditEnvParametersModal(artifact, this.$scope.currentComponent).then(()=> {
                this.updateArtifactsIfNeeded();
            }, ()=> {
                // ERROR
            });
        };

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_NODE_SELECTED, this.loadArtifacts);
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED, this.loadArtifacts);

        this.$scope.$on('$destroy', () => {

            this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_NODE_SELECTED, this.loadArtifacts);
            this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED, this.loadArtifacts);
        });

        this.loadArtifacts();
    }
}
