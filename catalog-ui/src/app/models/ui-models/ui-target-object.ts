import {IZoneInstanceAssignment} from "../graph/zones/zone-instance";
import {UiBaseObject} from "./ui-base-object";
import {TargetOrMemberType} from "../../utils/constants";

export class TargetUiObject extends UiBaseObject implements IZoneInstanceAssignment {
    constructor(uniqueId:string, type:TargetOrMemberType, name:string) {
        super(uniqueId, type, name);
    }
}