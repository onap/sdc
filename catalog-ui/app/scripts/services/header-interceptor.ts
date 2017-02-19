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

    //Method name should be exactly "response" - http://docs.angularjs.org/api/ng/service/$http
    export interface IInterceptor {
        request: Function;

    }

    export class HeaderInterceptor implements IInterceptor {
        public static $inject = [
            '$log',
            '$injector',
            '$q',
            'uuid4',
            'Sdc.Services.SharingService',
            'sdcConfig',
            '$location'


        ];

        public static Factory($log: ng.ILogService,
                              $injector: ng.auto.IInjectorService,
                              $q: ng.IQService,
                              uuid4: any,
                              sharingService: Sdc.Services.SharingService,
                              sdcConfig: Models.IAppConfigurtaion,
                              $location: ng.ILocationService) {
            return new HeaderInterceptor($log, $injector, $q, uuid4, sharingService, sdcConfig, $location);
        }

        constructor(private $log: ng.ILogService,
                    private $injector: ng.auto.IInjectorService,
                    private $q: ng.IQService,
                    private uuid4: any,
                    private sharingService: Sdc.Services.SharingService,
                    private sdcConfig: Models.IAppConfigurtaion,
                    private $location: ng.ILocationService) {
            this.$log.debug('initializing AuthenticationInterceptor');
        }

        public request = (requestSuccess): ng.IPromise<any> => {
            requestSuccess.headers['X-ECOMP-RequestID'] = this.uuid4.generate();
            /**
             * For every request to the server, that the service id, or resource id is sent in the URL, need to pass UUID in the header.
             * Check if the unique id exists in uuidMap, and if so get the UUID and add it to the header.
             */
            let map: Utils.Dictionary<string, string> = this.sharingService.getUuidMap();
            if (map && requestSuccess.url.indexOf(this.sdcConfig.api.root) === 0) {
                this.$log.debug("url: " + requestSuccess.url);
                map.forEach((key: string) => {
                    if (requestSuccess.url.indexOf(key) !== -1) {
                        requestSuccess.headers['X-ECOMP-ServiceID'] = this.sharingService.getUuidValue(key);
                    }
                });
            }
            return requestSuccess;
        };

        public response = (responseSuccess): ng.IPromise<any> => {
            let responseData = responseSuccess.data;
            if (responseData) {
                let data = JSON.stringify(responseData);
                if (data && (data.indexOf("Global Logon: Login") > 0)) {
                    this.$location.path('dashboard/welcome');
                    window.location.reload();
                }
            }
            return responseSuccess;
        }
    }
}
