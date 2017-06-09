/**
 * Created by rcohen on 10/31/2016.
 */
'use strict';

export class HeatParameterModel {
    uniqueId:string;
    name:string;
    type:string;
    description:string;
    currentValue:string;
    defaultValue:string;

    constructor(parameter?:HeatParameterModel) {
    }

}

