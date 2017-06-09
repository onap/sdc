/**
 * Created by obarda on 4/20/2016.
 */
'use strict';
import {PropertyModel} from "./properties";

//this is an object contains keys, when each key has matching array.
// for example: key = tosca.capabilities.network.Linkable and the match array is array of capabilities objects
export class CapabilitiesGroup {
    constructor(capabilityGroupObj?:CapabilitiesGroup) {
        _.forEach(capabilityGroupObj, (capabilitiesArrayObj:Array<Capability>, instance) => {
            this[instance] = [];
            _.forEach(capabilitiesArrayObj, (capability:Capability):void => {
                this[instance].push(new Capability(capability));
            });
        });
    }

    public findValueByKey(keySubstring:string):Array<Capability> {
        let key:string = _.find(Object.keys(this), (key)=> {
            return _.includes(key.toLowerCase(), keySubstring);
        });
        return this[key];
    }
}

export class Capability {

    //server data
    name:string;
    ownerId:string;
    ownerName:string;
    type:string;
    uniqueId:string;
    capabilitySources:Array<String>;
    minOccurrences:string;
    maxOccurrences:string;
    properties:Array<PropertyModel>;
    description:string;
    validSourceTypes:Array<string>;
    //custom
    selected:boolean;
    filterTerm:string;

    constructor(capability?:Capability) {

        if (capability) {
            //server data
            this.name = capability.name;
            this.ownerId = capability.ownerId;
            this.ownerName = capability.ownerName;
            this.type = capability.type;
            this.uniqueId = capability.uniqueId;
            this.capabilitySources = capability.capabilitySources;
            this.minOccurrences = capability.minOccurrences;
            this.maxOccurrences = capability.maxOccurrences;
            this.properties = capability.properties;
            this.description = capability.description;
            this.validSourceTypes = capability.validSourceTypes;
            this.selected = capability.selected;
            this.initFilterTerm();

        }
    }

    public getFullTitle():string {
        let maxOccurrences:string = this.maxOccurrences === 'UNBOUNDED' ? '∞' : this.maxOccurrences;
        return this.ownerName + ': ' + this.name + ': [' + this.minOccurrences + ', ' + maxOccurrences + ']';
    }

    public toJSON = ():any => {
        this.selected = undefined;
        this.filterTerm = undefined;
        return this;
    };

    private initFilterTerm = ():void => {
        this.filterTerm = this.name + " " +
            (this.type ? (this.type.replace("tosca.capabilities.", "") + " " ) : "") +
            (this.description || "") + " " +
            (this.ownerName || "") + " " +
            (this.validSourceTypes ? (this.validSourceTypes.join(',') + " ") : "") +
            this.minOccurrences + "," + this.maxOccurrences;
        if (this.properties && this.properties.length) {
            _.forEach(this.properties, (prop:PropertyModel)=> {
                this.filterTerm += " " + prop.name +
                    " " + (prop.description || "") +
                    " " + prop.type +
                    (prop.schema && prop.schema.property ? (" " + prop.schema.property.type) : "");
            });
        }
    }
}


