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
/// <reference path="../references"/>
module Sdc.Utils {
    export class ArtifactsUtils {

        static '$inject' = [
            '$filter',
            '$templateCache',
            '$modal'
        ];

        constructor(private $filter:ng.IFilterService,
                    private $templateCache:ng.ITemplateCacheService,
                    private $modal:ng.ui.bootstrap.IModalService) {

        }

        public getArtifactTypeByState(currentState:string):string {
            switch (currentState) {
                case "workspace.composition.lifecycle":
                    return "interface";
                case "workspace.composition.api":
                    return "api";
                case "workspace.composition.deployment":
                    return "deployment";
                default:
                    return "normal";
            }
        }

        public getTitle(artifactType:string, selectedComponent:Models.Components.Component):string {
            switch (artifactType) {
                case "interface":
                    return "Lifecycle Management";
                case "api":
                    return "API Artifacts";
                case "deployment":
                    return "Deployment Artifacts";
                default:
                    if (!selectedComponent) {
                        return "";
                    } else {
                        return this.$filter("resourceName")(selectedComponent.name) + ' Artifacts';
                    }
            }
        }

        public setArtifactType = (artifact:Models.ArtifactModel, artifactType:string):void => {
            switch (artifactType) {
                case "api":
                    artifact.artifactGroupType = 'SERVICE_API';
                    break;
                case "deployment":
                    artifact.artifactGroupType = 'DEPLOYMENT';
                    break;
                default:
                    artifact.artifactGroupType = 'INFORMATIONAL';
                    break;
            }
        };

        public isLicenseType = (artifactType:string) :boolean => {
            let isLicense:boolean = false;

            if(Utils.Constants.ArtifactType.VENDOR_LICENSE === artifactType || Utils.Constants.ArtifactType.VF_LICENSE === artifactType) {
                isLicense = true;
            }

            return isLicense;
        };

        public removeArtifact = (artifact:Models.ArtifactModel, artifactsArr:Array<Models.ArtifactModel>):void => {

            if (!artifact.mandatory && (Utils.Constants.ArtifactGroupType.INFORMATION == artifact.artifactGroupType ||
                Utils.Constants.ArtifactGroupType.DEPLOYMENT == artifact.artifactGroupType)) {
                _.remove(artifactsArr, {uniqueId: artifact.uniqueId});
            }
            else {
                let artifactToDelete = _.find(artifactsArr, {uniqueId: artifact.uniqueId});

                delete artifactToDelete.esId;
                delete artifactToDelete.description;
                delete artifactToDelete.artifactName;
                delete artifactToDelete.apiUrl;
            }
        };

        public addAnotherAfterSave(scope:Sdc.ViewModels.IArtifactResourceFormViewModelScope) {
            let newArtifact = new Models.ArtifactModel();
            this.setArtifactType(newArtifact, scope.artifactType);
            scope.editArtifactResourceModel.artifactResource = newArtifact;

            scope.forms.editForm['description'].$setPristine();
            if(scope.forms.editForm['artifactLabel']){
                scope.forms.editForm['artifactLabel'].$setPristine();
            }
            if(scope.forms.editForm['type']){
                scope.forms.editForm['type'].$setPristine();
            }

        }
    }
}
