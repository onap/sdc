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
    import {ArtifactType} from "app/utils";
    import {ArtifactGroupModel} from "app/models";
    import {participant} from "app/view-models/workspace/tabs/network-call-flow/network-call-flow-view-model";
    import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
    import {ComponentGenericResponse} from "../../../../ng2/services/responses/component-generic-response";
    import {ComponentServiceNg2} from "../../../../ng2/services/component-services/component.service";

    export interface IManagementWorkflowViewModelScope extends IWorkspaceViewModelScope {
        vendorModel:VendorModel;
    }

    export class VendorModel {
        artifacts: ArtifactGroupModel;
        serviceID: string;
        readonly: boolean;
        sessionID: string;
        requestID: string;
        diagramType: string;
        participants:Array<participant>;

        constructor(artifacts: ArtifactGroupModel, serviceID:string, readonly:boolean, sessionID:string,
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
            'uuid4',
            'ComponentServiceNg2'
        ];

        constructor(private $scope:IManagementWorkflowViewModelScope,
                    private uuid4:any,
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
                this.$scope.component.artifacts.filteredByType(ArtifactType.THIRD_PARTY_RESERVED_TYPES.WORKFLOW),
                this.$scope.component.uniqueId,
                this.$scope.isViewMode(),
                this.$scope.user.userId,
                this.uuid4.generate(),
                ArtifactType.THIRD_PARTY_RESERVED_TYPES.WORKFLOW,
                ManagementWorkflowViewModel.getParticipants()
            );

            this.$scope.thirdParty = true;
            this.$scope.setValidState(true);
        }
    }
