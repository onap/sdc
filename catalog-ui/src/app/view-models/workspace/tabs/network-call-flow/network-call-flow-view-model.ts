'use strict';
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {VendorModel} from "app/view-models/workspace/tabs/management-workflow/management-workflow-view-model";
import {ResourceType, ArtifactType} from "app/utils";
import {ComponentInstance} from "app/models";
import {ComponentGenericResponse} from "../../../../ng2/services/responses/component-generic-response";
import {ComponentServiceNg2} from "../../../../ng2/services/component-services/component.service";

export interface INetworkCallFlowViewModelScope extends IWorkspaceViewModelScope {
    vendorMessageModel:VendorModel;
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

        this.initComponentInstancesAndInformationalArtifacts();
        this.$scope.updateSelectedMenuItem();
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
