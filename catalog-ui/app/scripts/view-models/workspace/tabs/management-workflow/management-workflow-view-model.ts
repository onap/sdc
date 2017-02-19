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

    export interface IManagementWorkflowViewModelScope extends IWorkspaceViewModelScope {
        vendorModel:VendorModel;
    }

    export class VendorModel {
        artifacts: Models.ArtifactGroupModel;
        serviceID: string;
        readonly: boolean;
        sessionID: string;
        requestID: string;
        diagramType: string;
        participants:Array<participant>;

        constructor(artifacts: Models.ArtifactGroupModel, serviceID:string, readonly:boolean, sessionID:string,
                    requestID:string, diagramType:string, participants:Array<participant>){
            this.artifacts = artifacts;
            this.serviceID = serviceID;
            this.readonly = readonly;
            this.sessionID = sessionID;
            this.requestID = requestID;
            this.diagramType = diagramType;
            this.participants = participants;
        }
    }

    export class ManagementWorkflowViewModel {

        static '$inject' = [
            '$scope',
            'uuid4'
        ];

        constructor(private $scope:IManagementWorkflowViewModelScope,
                    private uuid4:any) {

            this.initScope();
            this.$scope.updateSelectedMenuItem();
        }


        private static getParticipants():Array<participant> {
            return [
                {
                    "id": "1",
                    "name": "Customer"},
                {
                    "id": "2",
                    "name": "CCD"
                },
                {
                    "id": "3",
                    "name": "Infrastructure"
                },
                {
                    "id": "4",
                    "name": "MSO"
                },
                {
                    "id": "5",
                    "name": "SDN-C"
                },
                {
                    "id": "6",
                    "name": "A&AI"
                },
                {
                    "id": "7",
                    "name": "APP-C"
                },
                {
                    "id": "8",
                    "name": "Cloud"
                },
                {
                    "id": "9",
                    "name": "DCAE"
                },
                {
                    "id": "10",
                    "name": "ALTS"
                },
                {
                    "id": "11",
                    "name": "VF"
                }
            ]
        }


        private initScope():void {
            this.$scope.vendorModel = new VendorModel(
                this.$scope.component.artifacts.filteredByType(Utils.Constants.ArtifactType.THIRD_PARTY_RESERVED_TYPES.WORKFLOW),
                this.$scope.component.uniqueId,
                this.$scope.isViewMode(),
                this.$scope.user.userId,
                this.uuid4.generate(),
                Utils.Constants.ArtifactType.THIRD_PARTY_RESERVED_TYPES.WORKFLOW,
                ManagementWorkflowViewModel.getParticipants()
            );

            this.$scope.thirdParty = true;
            this.$scope.setValidState(true);
        }
    }
}
