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

    export interface INetworkCallFlowViewModelScope extends IWorkspaceViewModelScope {
        vendorMessageModel:VendorModel;
    }

    export class participant {
        name:string;
        id:string;

        constructor(instance:Models.ComponentsInstances.ComponentInstance){
            this.name = instance.name;
            this.id = instance.uniqueId;
        }
    }


    export class NetworkCallFlowViewModel {

        static '$inject' = [
            '$scope',
             'uuid4'
        ];

        constructor(private $scope:INetworkCallFlowViewModelScope,
                    private uuid4:any) {

            this.initScope();
            this.$scope.updateSelectedMenuItem();
        }

        private getVFParticipantsFromInstances(instances:Array<Models.ComponentsInstances.ComponentInstance>):Array<participant> {
            let participants = [];
            _.forEach(instances,(instance)=> {
                if(Utils.Constants.ResourceType.VF == instance.originType){
                    participants.push(new participant(instance));
                }
            });
            return participants;
        }


        private initScope():void {
            this.$scope.vendorMessageModel = new VendorModel(
                this.$scope.component.artifacts.filteredByType(Utils.Constants.ArtifactType.THIRD_PARTY_RESERVED_TYPES.NETWORK_CALL_FLOW),
                this.$scope.component.uniqueId,
                this.$scope.isViewMode(),
                this.$scope.user.userId,
                this.uuid4.generate(),
                Utils.Constants.ArtifactType.THIRD_PARTY_RESERVED_TYPES.NETWORK_CALL_FLOW,
                this.getVFParticipantsFromInstances(this.$scope.component.componentInstances)
            );

            this.$scope.thirdParty = true;
            this.$scope.setValidState(true);
        }

    }
}
