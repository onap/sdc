import { PropertyModel } from "app/models";
import { CommonUtils } from "app/utils";
import {IZoneInstanceMethod, IZoneInstanceAssignment} from "./zone-instance";
import {GroupInstance} from "./group-instance";
import {ComponentInstance} from "../../componentsInstances/componentInstance";
import {TargetUiObject} from "../../ui-models/ui-target-object";
import {TargetOrMemberType} from "../../../utils/constants";

/* The request and response should be same model, need to fix in BE */
export class PolicyTargetsMap {
    COMPONENT_INSTANCES:Array<string>;
    GROUPS:Array<string>;
}
//TODO remove this
export class PolicyTargetsRequest {

    requestItems:Array<PolicyTargetsRequestItem>;

    constructor(groups:Array<string>,instances:Array<string>){

        this.requestItems = [];

        if (instances && instances.length>0) {
            let instancesObj:PolicyTargetsRequestItem = {
                type: "component_Instances",
                uniqueIds: instances
            };
            this.requestItems.push(instancesObj);
        }
        if (groups && groups.length>0) {
            let groupsObj:PolicyTargetsRequestItem = {
                type: "groups",
                uniqueIds: groups
            };
            this.requestItems.push(groupsObj);
        }
    }
}

export class PolicyTargetsRequestItem {
    type: string;
    uniqueIds:Array<String>;
}
    
export class PolicyInstance implements IZoneInstanceMethod {
    componentName:string;
    description:string;
    empty:boolean;
    invariantName:string;
    invariantUUID:string;
    isFromCsar:boolean;

    name:string;
    normalizedName:string;
    type:string;
    policyTypeUid:string;
    policyUUID:string;
    properties:Array<PropertyModel>;
    targets:PolicyTargetsMap;
    uniqueId:string;
    version:string;
    iconSprite:string;
    icon:string;
    originArchived:boolean;
    instanceUniqueId: string;


    constructor(policy?:PolicyInstance) {
        this.componentName = policy.componentName;
        this.description = policy.description;
        this.empty = policy.empty;
        this.invariantName = policy.invariantName;
        this.invariantUUID = policy.invariantUUID;
        this.isFromCsar = policy.isFromCsar;
        
        this.name = policy.name;
        this.normalizedName =policy.normalizedName;
        this.type = policy.type;
        this.policyTypeUid = policy.policyTypeUid;
        this.policyUUID = policy.policyUUID;
        this.properties = CommonUtils.initProperties(policy.properties);
        this.targets = policy.targets;
        this.uniqueId = policy.uniqueId;
        this.version = policy.version;
        this.instanceUniqueId = policy.instanceUniqueId;

        this.iconSprite = '';
        this.icon = 'icon-policy';
    }

    public getTargetsAsUiObject(componentInstances?:Array<ComponentInstance>, groupInstances?:Array<GroupInstance>):Array<TargetUiObject> {
        let savedItems:Array<TargetUiObject> = [];

        //get all targets from component instances
        if (!_.isEmpty(this.targets.COMPONENT_INSTANCES)) {
            this.targets.COMPONENT_INSTANCES.forEach((targetInstanceId:string)=> {
                let componentInstance:ComponentInstance;
                if (componentInstances) {
                    componentInstance = _.find(componentInstances, function (_componentInstance:ComponentInstance) {
                        return _componentInstance.uniqueId === targetInstanceId;
                    })
                }
                savedItems.push(new TargetUiObject(targetInstanceId, TargetOrMemberType.COMPONENT_INSTANCES, componentInstance ? componentInstance.name : undefined));
            });
        }

        //get all targets from groupInstances
        if (!_.isEmpty(this.targets.GROUPS)) {
            this.targets.GROUPS.forEach((groupsTargetId:string)=> {
                let groupInstance:GroupInstance;
                if (groupInstances) {
                    groupInstance = _.find(groupInstances, function (_groupInstance:GroupInstance) {
                        return _groupInstance.uniqueId === groupsTargetId;
                    })
                }
                savedItems.push(new TargetUiObject(groupsTargetId, TargetOrMemberType.GROUPS, groupInstance? groupInstance.name : undefined));
            });
        }
        return savedItems;
    };

    public saveTargets = (newTargets:Array<TargetUiObject>):void => {
        this.targets.COMPONENT_INSTANCES = newTargets.filter(target => target.type === TargetOrMemberType.COMPONENT_INSTANCES).map(target => target.uniqueId);
        this.targets.GROUPS = newTargets.filter(target => target.type === TargetOrMemberType.GROUPS).map(target => target.uniqueId);
    }

    // This function is used for the zone to get and set the assignment
    public getSavedAssignments = ():Array<IZoneInstanceAssignment> => {
        return this.getTargetsAsUiObject();
    };

    public setSavedAssignments = (newMembers:Array<IZoneInstanceAssignment>):void => {
        this.saveTargets(newMembers);
    }

    public get iconClass() {
        return this.iconSprite + ' ' + this.icon;
    }

}