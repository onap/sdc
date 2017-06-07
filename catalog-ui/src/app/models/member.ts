/**
 * Created by obarda on 8/2/2016.
 */
'use strict';

export class Members {

    [index:string]:string;

    constructor(members?:Members) {
        _.forEach(members, (memberId:string, index) => {
            this[index] = memberId;
        });
    }
}

