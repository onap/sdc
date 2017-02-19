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

    interface IOnboardingService {
        getOnboardingComponents(): ng.IPromise<Array<Models.Components.IComponent>>;
        getComponentFromCsarUuid(csarUuid:string): ng.IPromise<Models.Components.Component>;
        downloadOnboardingCsar(packageId:string):ng.IPromise<Models.IFileDownload>;
    }

    export class OnboardingService implements IOnboardingService {

        static '$inject' = ['$http', '$q', 'sdcConfig', 'ComponentFactory'];
        private api:Models.IApi;

        constructor(private $http:ng.IHttpService,
                    private $q:ng.IQService,
                    private sdcConfig:Models.IAppConfigurtaion,
                    private ComponentFactory: Sdc.Utils.ComponentFactory
        ) {
            this.api = sdcConfig.api;
        }

        getOnboardingComponents = ():ng.IPromise<Array<Models.Components.IComponent>> => {
            let defer = this.$q.defer<Array<Models.Components.IComponent>>();
            this.$http.get(this.api.GET_onboarding)
                .success((response:any) => {
                    let onboardingComponents:Array<Models.ICsarComponent> = response.results;
                    let componentsList:Array<Models.Components.IComponent> = new Array();

                    onboardingComponents.forEach((obc: Models.ICsarComponent) => {
                        let component:Models.Components.Component = this.ComponentFactory.createFromCsarComponent(obc);
                        componentsList.push(component);
                    });

                    defer.resolve(componentsList);
                })
                .error((response) => {
                    defer.reject(response);
                });

            return defer.promise;
        };

        downloadOnboardingCsar = (packageId:string):ng.IPromise<Models.IFileDownload> => {
            let defer = this.$q.defer();
            this.$http({
                    url: this.api.GET_onboarding + "/" + packageId,
                    method: "get",
                    responseType: "blob"
                })
                .success((response:any) => {
                    defer.resolve(response);
                })
                .error((err) => {
                    defer.reject(err);
                });

            return defer.promise;
        };

        getComponentFromCsarUuid = (csarUuid:string):ng.IPromise<Models.Components.Component> => {
            let defer = this.$q.defer<Models.Components.Component>();
            this.$http.get(this.api.root + this.api.GET_component_from_csar_uuid.replace(':csar_uuid', csarUuid))
                .success((response:any) => {
                    let component:Models.Components.Resource;
                    // If the status is 400, this means that the component not found.
                    // I do not want to return error from server, because a popup will appear in client with the error.
                    // So returning success (200) with status 400.
                    if (response.status!==400) {
                        component = new Models.Components.Resource(null, this.$q, <Models.Components.Resource>response);
                    }
                    defer.resolve(component);
                })
                .error((response) => {
                    defer.reject(response);
                });

            return defer.promise;
        };

    }
}
