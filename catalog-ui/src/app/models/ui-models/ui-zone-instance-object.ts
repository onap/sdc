import { UiBaseObject } from "app/models/ui-models/ui-base-object";
import { ZoneInstanceType } from "../graph/zones/zone-instance";


export class UIZoneInstanceObject extends UiBaseObject{
    type:ZoneInstanceType;
    
    constructor(uniqueId: string,  type?: ZoneInstanceType, name?:string) {
        super(uniqueId, type, name);
    }
}