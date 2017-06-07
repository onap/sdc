'use strict';

export class DistributionStatuses {
    public omfComponentID:string;
    public url:string;
    public timestamp:string;
    public status:string;

    constructor() {
    }
}


export class DistributionComponent {
    public omfComponentID:string;
    public url:string;
    public timestamp:string;
    public status:string;

    constructor() {
    }
}

export class Distribution {
    public distributionID:string;
    public timestamp:string;
    public userId:string;
    public deployementStatus:string;
    public distributionComponents:Array<DistributionComponent>;
    public statusCount:any;
    //custom data
    public dateFormat:string;

    constructor() {
    }

    public toJSON = ():any => {
        this.dateFormat = undefined;
        return this;
    };
}


