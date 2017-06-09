'use strict';
import {Dictionary} from "app/utils";

export class SharingService {

    private uuidMap:Dictionary<string, string> = new Dictionary<string,string>();

    public getUuidValue = (uniqueId:string):string => {
        return this.uuidMap.getValue(uniqueId);
    };

    public addUuidValue = (uniqueId:string, uuid:string):void => {
        this.uuidMap.setValue(uniqueId, uuid);
    };

    public getUuidMap = ():Dictionary<string, string> => {
        return this.uuidMap;
    };

}
