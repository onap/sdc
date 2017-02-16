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
/// <reference path="../references"/>
module Sdc.Services {

    'use strict';

    interface IEcompHeaderService {
        getMenuItems(userId): ng.IPromise<Array<any>>;
    }

    export class EcompHeaderService implements IEcompHeaderService {
        static '$inject' = ['$http', '$q', 'sdcConfig'];
        private api:Models.IApi;

        constructor(private $http:ng.IHttpService,
                    private $q:ng.IQService,
                    private sdcConfig:Models.IAppConfigurtaion) {
            this.api = sdcConfig.api;
        }
        
        getMenuItems = (userId):ng.IPromise<Array<any>> => {
            let defer = this.$q.defer<Array<any>>();
            //defer.resolve(this.mockData);
            this.$http.get(this.api.root + this.api.GET_ecomp_menu_items.replace(':userId', userId))
                .success((response:any) => {
                    defer.resolve(response);
                })
                .error((response) => {
                    defer.reject(response);
                });

            return defer.promise;
        };

    }
}
