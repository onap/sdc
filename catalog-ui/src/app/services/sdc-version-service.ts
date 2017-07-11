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
import {Distribution} from "../models/distribution";
import {IAppConfigurtaion, IApi} from "../models/app-config";

export interface ISdcVersionService {
    getVersion():ng.IPromise<any>;
}
export class SdcVersionService implements ISdcVersionService {

    static '$inject' = ['$http', '$q', 'sdcConfig'];
    private api:IApi;

    constructor(private $http:ng.IHttpService, private $q:ng.IQService, sdcConfig:IAppConfigurtaion) {
        this.api = sdcConfig.api;
    }

    public getVersion():ng.IPromise<any> {
        let defer = this.$q.defer<Array<Distribution>>();
        this.$http.get(this.api.root + this.api.GET_SDC_Version)
            .then((version:any) => {
                defer.resolve(version.data);
            });
        return defer.promise;
    }
}

