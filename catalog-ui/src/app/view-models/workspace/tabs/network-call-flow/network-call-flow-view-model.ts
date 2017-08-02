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
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {VendorModel} from "app/view-models/workspace/tabs/management-workflow/management-workflow-view-model";
import {ResourceType, ArtifactType} from "app/utils";
import {ComponentInstance} from "app/models";
import {ComponentGenericResponse} from "../../../../ng2/services/responses/component-generic-response";
import {ComponentServiceNg2} from "../../../../ng2/services/component-services/component.service";
declare var PunchOutRegistry;

export interface INetworkCallFlowViewModelScope extends IWorkspaceViewModelScope {
    vendorMessageModel:VendorModel;
    isLoading: boolean;
}

export class participant {
    name:string;
    id:string;

    constructor(instance:ComponentInstance) {
        this.name = instance.name;
        this.id = instance.uniqueId;
    }
}


export class NetworkCallFlowViewModel {

    static '$inject' = [
        '$scope',
        'uuid4',
        'ComponentServiceNg2'
    ];

    constructor(private $scope:INetworkCallFlowViewModelScope,
                private uuid4:any,
                private ComponentServiceNg2: ComponentServiceNg2) {

        this.$scope.isLoading = true;

        PunchOutRegistry.loadOnBoarding(()=> {
            this.$scope.isLoading = false;
            this.initComponentInstancesAndInformationalArtifacts();
        });
    }

    private getVFParticipantsFromInstances(instances:Array<ComponentInstance>):Array<participant> {
        let participants = [];
        _.forEach(instances, (instance)=> {
            if (ResourceType.VF == instance.originType) {
                participants.push(new participant(instance));
            }
        });
        return participants;
    }

    private initComponentInstancesAndInformationalArtifacts = ():void => {
        if(!this.$scope.component.artifacts || !this.$scope.component.componentInstances) {
            this.$scope.isLoading = true;
            this.ComponentServiceNg2.getComponentInformationalArtifactsAndInstances(this.$scope.component).subscribe((response:ComponentGenericResponse) => {
                this.$scope.component.artifacts = response.artifacts;
                this.$scope.component.componentInstances = response.componentInstances;
                this.initScope();
                this.$scope.isLoading = false;
            });
        } else {
            this.initScope();
        }
    }

    private initScope():void {
        this.$scope.vendorMessageModel = new VendorModel(
            this.$scope.component.artifacts.filteredByType(ArtifactType.THIRD_PARTY_RESERVED_TYPES.NETWORK_CALL_FLOW),
            this.$scope.component.uniqueId,
            this.$scope.isViewMode(),
            this.$scope.user.userId,
            this.uuid4.generate(),
            ArtifactType.THIRD_PARTY_RESERVED_TYPES.NETWORK_CALL_FLOW,
            this.getVFParticipantsFromInstances(this.$scope.component.componentInstances)
        );

        this.$scope.thirdParty = true;
        this.$scope.setValidState(true);
    }

}
