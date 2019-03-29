/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
import {ArtifactModel} from "../../../models/artifacts";
import {Component} from "../../../models/components/component";
import {PathsAndNamesDefinition} from "../../../models/paths-and-names";

'use strict';

interface IGABViewModelScope {
    gabModalInstance:ng.ui.bootstrap.IModalServiceInstance;
    pathsandnames: PathsAndNamesDefinition[];
    artifactid: string;
    resourceid: string;
    close(): void;
}

export class GenericArtifactBrowserModalViewModel {
    static '$inject' = [
        '$scope',
        '$uibModalInstance',
        'artifact',
        'component'];

    constructor(private $scope:IGABViewModelScope,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private artifact:ArtifactModel,
                private component:Component) {
        this.$scope.gabModalInstance = this.$uibModalInstance;

        this.$scope.pathsandnames = [
            {friendlyName: 'Action', path: 'event.action[2]'},
            {friendlyName: 'Comment', path: 'event.comment'},
            {friendlyName: 'Alarm Additional Information',
                path: 'event.structure.faultFields.structure.alarmAdditionalInformation.comment'}];

        this.$scope.artifactid = artifact.esId;
        this.$scope.resourceid = component.uniqueId;

        this.$scope.close = ():void => {
            this.$uibModalInstance.close();
        };
    }

}
