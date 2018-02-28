import { Type, Component } from "@angular/core";
import { PolicyInstance } from "app/models/graph/zones/policy-instance";

export class ZoneConfig {
    title:string;
    defaultIconText:string;
    type:string; 'policy|group';
    tagModeId:string;
    instances:Array<ZoneInstanceConfig>;
    showZone:boolean;


    constructor (title:string, defaultText:string, type:string, showZone:boolean) {
        this.title = title;
        this.defaultIconText = defaultText;
        this.type = type;
        this.tagModeId = this.type + "-tagging";
        this.instances = [];
        this.showZone = showZone;
    }
}

export class ZoneInstanceConfig {

    name:string;
    assignments:Array<string>; //targets or members
    instanceData:PolicyInstance; // | GroupInstance;
    mode:ZoneInstanceMode;

    constructor(instance:PolicyInstance) { /* | GroupInstance */

        this.name = instance.name;
        this.instanceData = instance;
        this.mode = ZoneInstanceMode.NONE;

        if(instance instanceof PolicyInstance) {
            this.assignments = instance.targets;
        }
    }

}

export enum ZoneInstanceMode {
    NONE,
    HOVER,
    SELECTED,
    TAG
}