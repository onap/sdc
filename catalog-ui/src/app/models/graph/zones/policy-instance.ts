import { PropertyModel } from 'app/models/properties';
import { CommonUtils } from 'app/utils/common-utils';
import * as _ from 'lodash';
import { TargetOrMemberType } from '../../../utils/constants';
import { ComponentInstance } from '../../components-instances/component-instance';
import { TargetUiObject } from '../../ui-models/ui-target-object';
import { GroupInstance } from './group-instance';
import { IZoneInstanceAssignment, IZoneInstanceMethod } from './zone-instance';

/* The request and response should be same model, need to fix in BE */
export class PolicyTargetsMap {
    COMPONENT_INSTANCES: string[];
    GROUPS: string[];
}
// TODO remove this
export class PolicyTargetsRequest {

    requestItems: PolicyTargetsRequestItem[];

    constructor(groups: string[], instances: string[]) {

        this.requestItems = [];

        if (instances && instances.length > 0) {
            const instancesObj: PolicyTargetsRequestItem = {
                type: 'component_Instances',
                uniqueIds: instances
            };
            this.requestItems.push(instancesObj);
        }
        if (groups && groups.length > 0) {
            const groupsObj: PolicyTargetsRequestItem = {
                type: 'groups',
                uniqueIds: groups
            };
            this.requestItems.push(groupsObj);
        }
    }
}

export class PolicyTargetsRequestItem {
    type: string;
    uniqueIds: String[];
}

export class PolicyInstance implements IZoneInstanceMethod {
    componentName: string;
    description: string;
    empty: boolean;
    invariantName: string;
    invariantUUID: string;
    isFromCsar: boolean;

    name: string;
    normalizedName: string;
    type: string;
    policyTypeUid: string;
    policyUUID: string;
    properties: PropertyModel[];
    targets: PolicyTargetsMap;
    uniqueId: string;
    version: string;
    iconSprite: string;
    icon: string;
    originArchived: boolean;
    instanceUniqueId: string;

    constructor(policy?: PolicyInstance) {
        if (policy) {
            this.componentName = policy.componentName;
            this.description = policy.description;
            this.empty = policy.empty;
            this.invariantName = policy.invariantName;
            this.invariantUUID = policy.invariantUUID;
            this.isFromCsar = policy.isFromCsar;

            this.name = policy.name;
            this.normalizedName = policy.normalizedName;
            this.type = policy.type;
            this.policyTypeUid = policy.policyTypeUid;
            this.policyUUID = policy.policyUUID;
            this.properties = CommonUtils.initProperties(policy.properties);
            this.targets = policy.targets;
            this.uniqueId = policy.uniqueId;
            this.version = policy.version;
            this.instanceUniqueId = policy.instanceUniqueId;
        }
        this.iconSprite = '';
        this.icon = 'icon-policy';
    }

    public getTargetsAsUiObject(componentInstances?: ComponentInstance[], groupInstances?: GroupInstance[]): TargetUiObject[] {
        const savedItems: TargetUiObject[] = [];

        // get all targets from component instances
        if (!_.isEmpty(this.targets.COMPONENT_INSTANCES)) {
            this.targets.COMPONENT_INSTANCES.forEach((targetInstanceId: string) => {
                let componentInstance: ComponentInstance;
                if (componentInstances) {
                    componentInstance = _.find(componentInstances, function(_componentInstance: ComponentInstance) {
                        return _componentInstance.uniqueId === targetInstanceId;
                    });
                }
                savedItems.push(new TargetUiObject(targetInstanceId, TargetOrMemberType.COMPONENT_INSTANCES, componentInstance ? componentInstance.name : undefined));
            });
        }

        // get all targets from groupInstances
        if (!_.isEmpty(this.targets.GROUPS)) {
            this.targets.GROUPS.forEach((groupsTargetId: string) => {
                let groupInstance: GroupInstance;
                if (groupInstances) {
                    groupInstance = _.find(groupInstances, function(_groupInstance: GroupInstance) {
                        return _groupInstance.uniqueId === groupsTargetId;
                    });
                }
                savedItems.push(new TargetUiObject(groupsTargetId, TargetOrMemberType.GROUPS, groupInstance ? groupInstance.name : undefined));
            });
        }
        return savedItems;
    }

    public saveTargets = (newTargets: TargetUiObject[]): void => {
        this.targets.COMPONENT_INSTANCES = newTargets.filter((target) => target.type === TargetOrMemberType.COMPONENT_INSTANCES).map((target) => target.uniqueId);
        this.targets.GROUPS = newTargets.filter((target) => target.type === TargetOrMemberType.GROUPS).map((target) => target.uniqueId);
    }

    // This function is used for the zone to get and set the assignment
    public getSavedAssignments = (): IZoneInstanceAssignment[] => {
        return this.getTargetsAsUiObject();
    }

    public setSavedAssignments = (newMembers: IZoneInstanceAssignment[]): void => {
        this.saveTargets(newMembers);
    }

    public get iconClass() {
        return this.iconSprite + ' ' + this.icon;
    }

}
