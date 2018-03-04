import { PropertyModel } from "app/models";
import { CommonUtils } from "app/utils";


export class PolicyInstance {
    componentName:string;
    description:string;
    empty:boolean;
    invariantName:string;
    invariantUUID:string;
    isFromCsar:boolean;

    name:string;
    normalizedName:string;
    policyTypeName:string;
    policyTypeUid:string;
    policyUUID:string;
    properties:Array<PropertyModel>;
    targets:Array<string>;
    uniqueId:string;
    version:string;

    constructor(policy?:PolicyInstance) {
        this.componentName = policy.componentName;
        this.description = policy.description;
        this.empty = policy.empty;
        this.invariantName = policy.invariantName;
        this.invariantUUID = policy.invariantUUID;
        this.isFromCsar = policy.isFromCsar;
        
        this.name = policy.name;
        this.normalizedName =policy.normalizedName;
        this.policyTypeName = policy.policyTypeName;
        this.policyTypeUid = policy.policyTypeUid;
        this.policyUUID = policy.policyUUID;
        this.properties = CommonUtils.initProperties(policy.properties);
        this.targets = policy.targets;
        this.uniqueId = policy.uniqueId;
        this.version = policy.version;

    }

}