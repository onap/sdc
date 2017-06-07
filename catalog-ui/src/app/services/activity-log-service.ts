'use strict';
import {Activity} from "../models/activity";
import {IAppConfigurtaion, IApi} from "../models/app-config";

// Define an interface of the object you want to use, providing it's properties
export interface IActivityLogService {
    getActivityLogService(type:string, id:string):ng.IPromise<Array<Activity>>;
}

export class ActivityLogService implements IActivityLogService {


    static '$inject' = ['$http', '$q', 'sdcConfig'];
    private api:IApi;

    constructor(private $http:ng.IHttpService, private $q:ng.IQService, sdcConfig:IAppConfigurtaion) {
        this.api = sdcConfig.api;
    }

    getActivityLogService = (type:string, id:string):ng.IPromise<Array<Activity>> => {
        let defer = this.$q.defer<any>();
        this.$http.get(this.api.root + this.api.GET_activity_log.replace(':type', type).replace(':id', id))
            .then((activityLog:any) => {
                defer.resolve(activityLog.data);
            });
        return defer.promise;
    }
}
