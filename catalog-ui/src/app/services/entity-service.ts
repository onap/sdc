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
import { Service, IApi, IAppConfigurtaion, Resource, Component} from "../models";
import {SharingService} from "./sharing-service";
import {ComponentFactory} from "../utils/component-factory";
import {CacheService} from "./cache-service";
import {ResourceType} from "app/utils";

interface IEntityService {
    getAllComponents():ng.IPromise<Array<Component>>;
}

interface IComponentsArray {
    services:Array<Service>;
    resources:Array<Resource>;
}

export class EntityService implements IEntityService {
    static '$inject' = ['$http', '$q', 'sdcConfig', 'Sdc.Services.SharingService', 'ComponentFactory', 'Sdc.Services.CacheService'];
    private api:IApi;

    constructor(private $http:ng.IHttpService,
                private $q:ng.IQService,
                private sdcConfig:IAppConfigurtaion,
                private sharingService:SharingService,
                private ComponentFactory:ComponentFactory,
                private cacheService:CacheService) {
        this.api = sdcConfig.api;
    }

    getCatalog = ():ng.IPromise<Array<Component>> => {
        let defer = this.$q.defer<Array<Component>>();
        this.$http.get(this.api.root + this.api.GET_catalog, {params: {excludeTypes: [ResourceType.VFCMT, ResourceType.CONFIGURATION]}})
            .then((response:any) => {
                let followedResponse: IComponentsArray =  response.data;
                let componentsList:Array<Component> = new Array();

                followedResponse.services.forEach((serviceResponse:Service) => {
                    let component:Service = this.ComponentFactory.createService(serviceResponse); // new Service(serviceResponse);
                    componentsList.push(component);
                    this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                });

                followedResponse.resources.forEach((resourceResponse:Resource) => {
                    let component:Resource = this.ComponentFactory.createResource(resourceResponse);
                    componentsList.push(component);
                    this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                });

                this.cacheService.set('breadcrumbsComponents', componentsList);
                defer.resolve(componentsList);
            },(responce) => {
                defer.reject(responce);
            });
        return defer.promise;
    };

    getAllComponents = ():ng.IPromise<Array<Component>> => {
        let defer = this.$q.defer<Array<Component>>();
        this.$http.get(this.api.root + this.api.GET_element)
            .then((response:any) => {
                let componentResponse:IComponentsArray = response.data;
                let componentsList:Array<Component> = [];

                componentResponse.services && componentResponse.services.forEach((serviceResponse:Service) => {
                    let component:Service = this.ComponentFactory.createService(serviceResponse);
                    componentsList.push(component);
                    this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                });

                componentResponse.resources && componentResponse.resources.forEach((resourceResponse:Resource) => {
                    let component:Resource = this.ComponentFactory.createResource(resourceResponse);
                    componentsList.push(component);
                    this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                });
                
                this.cacheService.set('breadcrumbsComponents', componentsList);
                defer.resolve(componentsList);
            });

        return defer.promise;
    };
}
