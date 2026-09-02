/**
 * Created by ob0695 on 10.04.2018.
 */
import {ZoneInstanceType, ZoneInstance, IZoneInstanceAssignment} from "./zone-instance";
import {Observable} from "rxjs/Rx";
import { CANVAS_TAG_MODE } from "app/utils/constants";

export class Zone {
    title:string;
    type:ZoneInstanceType;
    defaultIconText:string;
    instances:Array<ZoneInstance>;
    visible:boolean;
    minimized:boolean;

    constructor(title:string, defaultText:string, type:ZoneInstanceType) {
        this.title = title;
        this.defaultIconText = defaultText;
        this.type = type;
        this.instances = [];
        this.visible = false;
        this.minimized = false;
    }


    public getTagModeId = () => {
        let tagModeId = ZoneInstanceType[this.type].toUpperCase();
        return CANVAS_TAG_MODE[tagModeId + "_TAGGING"];
    }

    public getHoverTagModeId = () => {
        let tagModeId = ZoneInstanceType[this.type].toUpperCase();
        return CANVAS_TAG_MODE[tagModeId + "_TAGGING_HOVER"];
    }

    public removeInstance = (instanceId:string) => {
        this.instances = this.instances.filter(instance => instance.instanceData.uniqueId != instanceId);
    };
}


export interface IZoneService {
    updateZoneInstanceAssignments(topologyTemplateType:string, topologyTemplateId:string, zoneInstanceId:string, assignments:Array<IZoneInstanceAssignment>):Observable<any>;
    updateName(topologyTemplateType:string, topologyTemplateId:string, zoneInstanceId:string, newName:string):Observable<any>;
    deleteZoneInstance(topologyTemplateType:string, topologyTemplateId:string, zoneInstanceId:string):Observable<any>;
}