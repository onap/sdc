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
import {ISdcConfig} from "../ng2/config/sdc-config.config";

interface IEcompHeaderService {
    getMenuItems(userId):ng.IPromise<Array<any>>;
}

export class EcompHeaderService implements IEcompHeaderService {
    static '$inject' = ['$http', '$q', 'sdcConfig'];
    private sdcConfig:ISdcConfig;

    constructor(private $http:ng.IHttpService,
                private $q:ng.IQService,
                sdcConfig:ISdcConfig) {
        this.sdcConfig = sdcConfig;
    }

    getMenuItems = (userId):ng.IPromise<Array<any>> => {
        let defer = this.$q.defer<Array<any>>();
        //defer.resolve(this.mockData);
        let authConfig = this.sdcConfig.basicAuth;
        let httpOptions = {};
        if (authConfig.enabled) {
            httpOptions = {headers: {'Authorization': 'Basic ' + btoa(authConfig.userName + ":" + authConfig.userPass)}};
        }
        this.$http.get(this.sdcConfig.api.root + this.sdcConfig.api.GET_ecomp_menu_items.replace(':userId', userId), httpOptions)
            .then((response:any) => {
                defer.resolve(response.data);
            }, (response) => {
                defer.reject(response.data);
            });

        return defer.promise;
    };
}
