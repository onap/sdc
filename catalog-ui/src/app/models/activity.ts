/**
 * Created by obarda on 19/11/2015.
 */
'use strict';

/*this is in uppercase because of the server response*/
export class Activity {
    TIMESTAMP:string;
    ACTION:string;
    MODIFIER:string;
    STATUS:string;
    DESC:string;
    COMMENT:string;
    //custom data
    public dateFormat:string;

    constructor() {
    }

    public toJSON = ():any => {
        this.dateFormat = undefined;
        return this;
    };

}


