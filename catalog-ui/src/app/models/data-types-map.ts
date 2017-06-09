/**
 * Created by rcohen on 9/25/2016.
 */
'use strict';
import {DataTypeModel} from "./data-types";

export class DataTypesMapData {
    [dataTypeId:string]:Array<DataTypeModel>;
}

export class DataTypesMap {
    dataTypesMap:DataTypesMapData;

    constructor(dataTypesMap:DataTypesMapData) {
        this.dataTypesMap = dataTypesMap;
    }
}
