'use strict';
import {Dictionary} from "app/utils";

interface ICacheService {
    get(key:string):any;
    set(key:string, value:any):void;
}

export class CacheService implements ICacheService {

    private storage:Dictionary<string, any>;

    constructor() {
        this.storage = new Dictionary<string, any>();
    };

    public get = (key:string):any => {
        return this.storage.getValue(key);
    };

    public set = (key:string, value:any):void => {
        this.storage.setValue(key, value);
    };

    public remove = (key:string):void => {
        if (this.storage.containsKey(key)) {
            this.storage.remove(key);
        }
    };

    public contains = (key:string):boolean => {
        return this.storage.containsKey(key);
    };
}
