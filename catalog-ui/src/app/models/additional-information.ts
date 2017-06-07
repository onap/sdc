'use strict';
export interface IAdditionalInformationModel {
    uniqueId:string;
    key:string;
    value:string;
}


export class AdditionalInformationModel implements IAdditionalInformationModel {
    uniqueId:string;
    key:string;
    value:string;

    constructor() {
        this.uniqueId = '';
        this.key = '';
        this.value = '';

    }
}
