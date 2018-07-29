import {IZoneInstanceAssignment} from "../graph/zones/zone-instance";
import {UiBaseObject} from "./ui-base-object";
import {TargetOrMemberType} from "../../utils/constants";

export class MemberUiObject extends UiBaseObject implements IZoneInstanceAssignment {
    constructor(uniqueId: string,  name:string) {
        super(uniqueId, TargetOrMemberType.COMPONENT_INSTANCES, name);
    }
}