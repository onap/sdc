import {PolicyInstance} from "app/models/graph/zones/policy-instance";
import {GroupInstance} from "./group-instance";
import {Component as TopologyTemplate} from "app/models";
import {IUiBaseObject} from "../../ui-models/ui-base-object";
import { Subject } from "rxjs";

export enum ZoneInstanceMode {
    NONE,
    HOVER,
    SELECTED,
    TAG
}

export enum ZoneInstanceType {
    GROUP,
    POLICY
}

export enum ZoneInstanceAssignmentType {
    COMPONENT_INSTANCES,
    GROUPS
}

export interface IZoneInstanceMethod {

    getSavedAssignments():Array<IZoneInstanceAssignment>;
    setSavedAssignments(newAssignments:Array<IZoneInstanceAssignment>):void;
}

export interface IZoneInstanceAssignment extends IUiBaseObject{
    type: ZoneInstanceAssignmentType
}

export class ZoneInstance {

    parentComponentType:string;
    parentComponentID:string;
    instanceData: PolicyInstance | GroupInstance;
    mode:ZoneInstanceMode;
    type:ZoneInstanceType;
    handle:string;
    assignments:Array<IZoneInstanceAssignment>; //temp assignments visible on the UI; not the saved values on the BE
    hidden:boolean;
    forceSave:Subject<Function>;

    constructor(instance: PolicyInstance | GroupInstance, topologyTemplateType: string, topologyTemplateId: string) {

        this.instanceData = instance;
        this.parentComponentType = topologyTemplateType;
        this.parentComponentID = topologyTemplateId;

        if (instance instanceof PolicyInstance) {
            this.type = ZoneInstanceType.POLICY;
        } else {
            this.type = ZoneInstanceType.GROUP;
        }

        this.assignments = this.instanceData.getSavedAssignments();
        this.mode = ZoneInstanceMode.NONE;
        this.hidden = false;
        this.forceSave = new Subject();
    }

    public isAlreadyAssigned = (nodeId:string):boolean => {
        let matchingAssignments = this.assignments.filter((assignment) => {
            return assignment.uniqueId == nodeId;
        });
        return matchingAssignments && matchingAssignments.length > 0;
    }

    public addOrRemoveAssignment = (nodeId:string, nodeType:ZoneInstanceAssignmentType)=> { //change temp assignments, unsaved but visible in UI.

        if (!this.isAlreadyAssigned(nodeId)) {
            this.assignments.push(<IZoneInstanceAssignment>{uniqueId: nodeId, type: nodeType});
        } else {
            this.assignments = this.assignments.filter(assignment => assignment.uniqueId != nodeId);
        }
    }
    
    public isZoneAssignmentChanged(oldAssignments:Array<IZoneInstanceAssignment>, newAssignments:Array<IZoneInstanceAssignment>):boolean {
        if (oldAssignments.length != newAssignments.length) {
            return true;
        }
        let difference:Array<IZoneInstanceAssignment> = oldAssignments.filter((oldAssignment) => {
            return !newAssignments.find(newAssignment => newAssignment.uniqueId == oldAssignment.uniqueId);
        });
        if (difference.length) {
            return true;
        }

        return false;
    }

    public updateInstanceData (instanceData:  PolicyInstance | GroupInstance):void {
        this.instanceData = instanceData;
        this.assignments = this.instanceData.getSavedAssignments();
    }

    public showHandle = (handleId:string) => {
        this.handle = handleId;
    }

    public hideHandle = ():void => {
        this.handle = null;
    }
}
