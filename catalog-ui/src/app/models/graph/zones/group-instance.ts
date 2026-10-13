import { PropertyModel } from 'app/models/properties';
import { CommonUtils } from 'app/utils/common-utils';
import * as _ from 'lodash';
import { ComponentInstance } from '../../components-instances/component-instance';
import { MemberUiObject } from '../../ui-models/ui-member-object';
import { IZoneInstanceAssignment } from './zone-instance';

export class GroupInstance {

    public artifacts: string[];
    public artifactsUuid: string[];
    public description: string;
    public empty: boolean;
    public groupUUID: string;
    public invariantUUID: string;
    public members: string[];
    public name: string;
    public ownerId: string;
    public properties: PropertyModel[];
    public propertyValueCounter: number;
    public type: string;
    public typeUid: string;
    public uniqueId: string;
    public version: string;
    public iconSprite: string;
    public icon: string;
    public originArchived?: boolean;

    constructor(group?: GroupInstance) {
        if (group) {
            this.name = group.name;
            this.groupUUID = group.groupUUID;
            this.invariantUUID = group.invariantUUID;
            this.propertyValueCounter = group.propertyValueCounter;
            this.type = group.type;
            this.typeUid = group.typeUid;
            this.uniqueId = group.uniqueId;
            this.version = group.version;
            this.artifacts = group.artifacts;
            this.artifactsUuid = group.artifactsUuid;
            this.properties = CommonUtils.initProperties(group.properties);
            this.members = _.values(group.members);
            this.description = group.description;
            this.empty = group.empty;
            this.ownerId = group.ownerId;
        }
        this.iconSprite = '';
        this.icon = 'icon-group';
    }

    public getMembersAsUiObject(componentInstances?: ComponentInstance[]): MemberUiObject[] {
        const savedItems: MemberUiObject[] = [];
        if (!_.isEmpty(this.members)) {
            _.forEach(this.members, (memberId: string) => {
                let componentInstance: ComponentInstance;
                if (componentInstances) {
                    componentInstance = _.find(componentInstances, function(_componentInstance: ComponentInstance) {
                        return _componentInstance.uniqueId === memberId;
                    });
                }
                savedItems.push(new MemberUiObject(memberId, componentInstance ? componentInstance.name : undefined));
            });
        }
        return savedItems;
    }

    public setMembers = (newMembers: MemberUiObject[]): void => {
        this.members = newMembers.map((member) => member.uniqueId);
    }

    // This function is used for the zone
    public getSavedAssignments = (): IZoneInstanceAssignment[] => {
        return this.getMembersAsUiObject();
    }

    public setSavedAssignments = (newMembers: IZoneInstanceAssignment[]): void => {
        this.setMembers(newMembers);
    }

    public get iconClass() {
        return this.iconSprite + ' ' + this.icon;
    }

}
