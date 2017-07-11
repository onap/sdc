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
import {Component, IComponent} from "../models/components/component";
import {ICsarComponent} from "../models/csar-component";
import {IAppConfigurtaion, IApi} from "../models/app-config";
import {IFileDownload} from "../models/file-download";
import {Resource} from "../models/components/resource";
import {ComponentFactory} from "../utils/component-factory";

interface IOnboardingService {
    getOnboardingComponents():ng.IPromise<Array<IComponent>>;
    getComponentFromCsarUuid(csarUuid:string):ng.IPromise<Component>;
    downloadOnboardingCsar(packageId:string):ng.IPromise<IFileDownload>;
}

export class OnboardingService implements IOnboardingService {

    static '$inject' = ['$http', '$q', 'sdcConfig', 'ComponentFactory'];
    private api:IApi;

    constructor(private $http:ng.IHttpService,
                private $q:ng.IQService,
                private sdcConfig:IAppConfigurtaion,
                private ComponentFactory:ComponentFactory) {
        this.api = sdcConfig.api;
    }

    getOnboardingComponents = ():ng.IPromise<Array<IComponent>> => {
        let defer = this.$q.defer<Array<IComponent>>();
        this.$http.get(this.api.GET_onboarding)
            .then((response:any) => {
                let onboardingComponents:Array<ICsarComponent> = response.data.results;
                let componentsList:Array<IComponent> = new Array();

                onboardingComponents.forEach((obc:ICsarComponent) => {
                    let component:Component = this.ComponentFactory.createFromCsarComponent(obc);
                    componentsList.push(component);
                });

                defer.resolve(componentsList);
            },(response) => {
                defer.reject(response);
            });

        return defer.promise;
    };

    downloadOnboardingCsar = (packageId:string):ng.IPromise<IFileDownload> => {
        let defer = this.$q.defer();
        this.$http({
            url: this.api.GET_onboarding + "/" + packageId,
            method: "get",
            responseType: "blob"
        })
            .then((response:any) => {
                defer.resolve(response.data);
            }, (err) => {
                defer.reject(err);
            });

        return defer.promise;
    };

    getComponentFromCsarUuid = (csarUuid:string):ng.IPromise<Component> => {
        let defer = this.$q.defer<Component>();
        this.$http.get(this.api.root + this.api.GET_component_from_csar_uuid.replace(':csar_uuid', csarUuid))
            .then((response:any) => {
                let component:Resource;
                // If the status is 400, this means that the component not found.
                // I do not want to return error from server, because a popup will appear in client with the error.
                // So returning success (200) with status 400.
                if (response.data.status !== 400) {
                    component = new Resource(null, this.$q, <Resource>response.data);
                }
                defer.resolve(component);
            },(response) => {
                defer.reject(response.data);
            });

        return defer.promise;
    };

}
