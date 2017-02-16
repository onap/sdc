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

    interface IEntityService {
        getAllComponents(): ng.IPromise<Array<Models.Components.Component>>;
    }

    interface IComponentsArray {
        services:Array<Models.Components.Service>;
        resources:Array<Models.Components.Resource>;
        products:Array<Models.Components.Product>;
    }

    export class EntityService implements IEntityService {
        static '$inject' = ['$http', '$q', 'sdcConfig', 'Sdc.Services.SharingService','ComponentFactory','Sdc.Services.CacheService'];
        private api:Models.IApi;

        constructor(private $http:ng.IHttpService,
                    private $q:ng.IQService,
                    private sdcConfig:Models.IAppConfigurtaion,
                    private sharingService:Sdc.Services.SharingService,
                    private ComponentFactory: Sdc.Utils.ComponentFactory,
                    private cacheService:Sdc.Services.CacheService
        ) {
            this.api = sdcConfig.api;
        }

        getCatalog = ():ng.IPromise<Array<Models.Components.Component>> => {
            let defer = this.$q.defer<Array<Models.Components.Component>>();
            this.$http.get(this.api.root + this.api.GET_catalog)
                .success((followedResponse:IComponentsArray) => {

                    let componentsList:Array<Models.Components.Component> = new Array();

                    followedResponse.services.forEach((serviceResponse: Models.Components.Service) => {
                        let component:Models.Components.Service = this.ComponentFactory.createService(serviceResponse); // new Models.Components.Service(serviceResponse);
                        componentsList.push(component);
                        this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                    });

                    followedResponse.resources.forEach((resourceResponse:Models.Components.Resource) => {
                        let component:Models.Components.Resource = this.ComponentFactory.createResource(resourceResponse);
                        componentsList.push(component);
                        this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                    });

                     followedResponse.products.forEach((productResponse:Models.Components.Product) => {

                         let component:Models.Components.Product =  this.ComponentFactory.createProduct(productResponse);
                         componentsList.push(component);
                         this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                    });

                    this.cacheService.set('breadcrumbsComponents',componentsList);
                    defer.resolve(componentsList);
                })
                .error((responce) => {
                    defer.reject(responce);
                });
            return defer.promise;
        };

        getAllComponents = ():ng.IPromise<Array<Models.Components.Component>> => {
            let defer = this.$q.defer<Array<Models.Components.Component>>();
            this.$http.get(this.api.root + this.api.GET_element)
                .success((componentResponse:IComponentsArray) => {
                    let componentsList:Array<Models.Components.Component> = [];

                    componentResponse.services && componentResponse.services.forEach((serviceResponse:Models.Components.Service) => {
                            let component:Models.Components.Service = this.ComponentFactory.createService(serviceResponse);
                            componentsList.push(component);
                             this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                        });

                    componentResponse.resources && componentResponse.resources.forEach((resourceResponse:Models.Components.Resource) => {
                        let component:Models.Components.Resource = this.ComponentFactory.createResource(resourceResponse);
                        componentsList.push(component);
                        this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                    });

                    componentResponse.products && componentResponse.products.forEach((productsResponse:Models.Components.Product) => {
                        let component:Models.Components.Product = this.ComponentFactory.createProduct(productsResponse);
                        componentsList.push(component);
                        this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                    });
                    this.cacheService.set('breadcrumbsComponents',componentsList);
                    defer.resolve(componentsList);
                });

            return defer.promise;
        };
    }
}
