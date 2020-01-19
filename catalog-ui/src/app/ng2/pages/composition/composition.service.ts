import {Injectable} from "@angular/core";
import 'rxjs/add/observable/forkJoin';
import {Component, PropertiesGroup, AttributesGroup, PolicyInstance} from "app/models";
import {GroupInstance} from "app/models/graph/zones/group-instance";
import {CommonGraphDataService} from "./common/common-graph-data.service";
import {ForwardingPath} from "../../../models/forwarding-path";
import {SelectedComponentType} from "./common/store/graph.actions";

@Injectable()
export class CompositionService extends CommonGraphDataService{

    public originComponents: Array<Component>; //This contains the full data set after specifically requesting it. The uniqueId matches the 'componentUid' in the componentInstances array
    public componentInstancesProperties:PropertiesGroup;
    public componentInstancesAttributes:AttributesGroup;
    public groupInstances: GroupInstance[];
    public policies: PolicyInstance[];
    public forwardingPaths:  { [key:string]:ForwardingPath };
    public selectedComponentType: SelectedComponentType;

    //---------------------------- COMPONENT INSTANCES ------------------------------------//

    public getOriginComponentById = (uniqueId:string):Component => {
        return this.originComponents && this.originComponents.find(instance => instance.uniqueId === uniqueId);
    }

    public addOriginComponent = (originComponent:Component) => {
        if(!this.originComponents) this.originComponents = [];
        if(!this.getOriginComponentById(originComponent.uniqueId)){
            this.originComponents.push(originComponent);
        }
    }


    public updateGroup = (instance: GroupInstance) => {
        this.groupInstances = this.groupInstances.map(group => instance.uniqueId === group.uniqueId? instance : group);
    }

    public updatePolicy = (instance: PolicyInstance) => {
        this.policies = this.policies.map(policy => instance.uniqueId === policy.uniqueId? instance : policy);
    }

    //---------------------------- POLICIES---------------------------------//
    public addPolicyInstance = (instance: PolicyInstance) => {
        return this.policies.push(instance);
    }


    //---------------------------- POLICIES---------------------------------//
    public addGroupInstance = (instance: GroupInstance) => {
        return this.groupInstances.push(instance);
    }


   //----------------------------SELECTED COMPONENT -----------------------//

    public setSelectedComponentType = (selectedType: SelectedComponentType) => {
        this.selectedComponentType = selectedType;
    }
}
