/**
 * Created by rcohen on 9/25/2016.
 */
'use strict';
import {PropertyBEModel} from "./properties-inputs/property-be-model";

export class DataTypeModel {

    //server data
    name:string;
    uniqueId:string;
    derivedFromName:string;
    derivedFrom:DataTypeModel;
    creationTime:string;
    modificationTime:string;
    properties: Array<PropertyBEModel>;

    constructor(dataType:DataTypeModel) {
        if (dataType) {
            this.uniqueId = dataType.uniqueId;
            this.name = dataType.name;
            this.derivedFromName = dataType.derivedFromName;
            this.creationTime = dataType.creationTime;
            this.modificationTime = dataType.modificationTime;
            this.properties = dataType.properties;
        }
    }

    public toJSON = ():any => {

        return this;
    };
}

