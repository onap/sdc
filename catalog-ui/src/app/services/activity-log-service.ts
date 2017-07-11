/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
