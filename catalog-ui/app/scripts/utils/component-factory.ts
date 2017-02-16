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
/**
 * Created by obarda on 2/8/2016.
 */
/// <reference path="../references"/>
module Sdc.Utils {
    'use strict';
    import Resource = Sdc.Models.Components.Resource;

    export class ComponentFactory {

        static '$inject' = [
            'Sdc.Services.Components.ResourceService',
            'Sdc.Services.Components.ServiceService',
            'Sdc.Services.Components.ProductService',
            'Sdc.Services.CacheService',
            '$q'
        ];

        constructor(
                    private ResourceService:Services.Components.ResourceService,
                    private ServiceService:Services.Components.ServiceService,
                    private ProductService:Services.Components.ProductService,
                    private cacheService:Services.CacheService,
                    private $q: ng.IQService) {
        }

        public createComponent = (component:Models.Components.Component):Models.Components.Component => {
            let newComponent:Models.Components.Component;
            switch (component.componentType) {

                case 'SERVICE':
                    newComponent = new Models.Components.Service(this.ServiceService, this.$q, <Models.Components.Service> component);
                    break;

                case 'RESOURCE':
                    newComponent = new Models.Components.Resource(this.ResourceService, this.$q, <Models.Components.Resource> component);
                    break;

                case 'PRODUCT':
                    newComponent = new Models.Components.Product(this.ProductService, this.$q, <Models.Components.Product> component);
                    break;
            }
            return newComponent;
        };

        public createProduct = (product:Models.Components.Product):Models.Components.Product => {
            let newProduct:Models.Components.Product = new Models.Components.Product(this.ProductService, this.$q, <Models.Components.Product> product);
            return newProduct;
        };

        public createService = (service:Models.Components.Service):Models.Components.Service => {
            let newService:Models.Components.Service = new Models.Components.Service(this.ServiceService, this.$q, <Models.Components.Service> service);
            return newService;
        };

        public createResource = (resource:Models.Components.Resource):Models.Components.Resource => {
            let newResource:Models.Components.Resource = new Models.Components.Resource(this.ResourceService, this.$q, <Models.Components.Resource> resource);
            return newResource;
        };

        public createFromCsarComponent = (csar:Models.ICsarComponent):Models.Components.Component => {
            let newResource:Sdc.Models.Components.Resource = <Sdc.Models.Components.Resource>this.createEmptyComponent(Sdc.Utils.Constants.ComponentType.RESOURCE);
            newResource.name = csar.vspName;

            /**
             * Onboarding CSAR contains category and sub category that are uniqueId.
             * Need to find the category and sub category and extract the name from them.
             * First concat all sub categories to one array.
             * Then find the selected sub category and category.
             * @type {any}
             */
            let availableCategories = angular.copy(this.cacheService.get('resourceCategories'));
            let allSubs = [];
            _.each(availableCategories, (main:Models.IMainCategory)=>{
                if (main.subcategories) {
                    allSubs = allSubs.concat(main.subcategories);
                }
            });

            let selectedCategory:Models.IMainCategory = _.find(availableCategories, function(main:Models.IMainCategory){
                return main.uniqueId === csar.category;
            });

            let selectedSubCategory:Models.ISubCategory = _.find(allSubs,(sub:Models.ISubCategory)=>{
                return sub.uniqueId === csar.subCategory;
            });

            // Build the categories and sub categories array (same format as component category)
            let categories:Array<Models.IMainCategory> = new Array();
            let subcategories:Array<Models.ISubCategory> = new Array();
            if (selectedCategory && selectedSubCategory) {
                subcategories.push(selectedSubCategory);
                selectedCategory.subcategories = subcategories;
                categories.push(selectedCategory);
            }

            // Fill the component with details from CSAR
            newResource.selectedCategory = selectedCategory && selectedSubCategory ? selectedCategory.name + "_#_" + selectedSubCategory.name : '';
            newResource.categories = categories;
            newResource.vendorName = csar.vendorName;
            newResource.vendorRelease = csar.vendorRelease;
            newResource.csarUUID = csar.packageId;
            newResource.csarPackageType = csar.packageType;
            newResource.csarVersion = csar.version;
            newResource.packageId = csar.packageId;
            newResource.description = csar.description;
            return newResource;
        };

        public createEmptyComponent = (componentType: string):Models.Components.Component => {
            let newComponent:Models.Components.Component;

            switch (componentType) {

                case Utils.Constants.ComponentType.SERVICE:
                    newComponent = new Models.Components.Service(this.ServiceService, this.$q);
                    break;

                case Utils.Constants.ComponentType.RESOURCE:
                case Utils.Constants.ResourceType.VF:
                case Utils.Constants.ResourceType.VL:
                case Utils.Constants.ResourceType.VFC:
                case Utils.Constants.ResourceType.CP:
                    newComponent = new Models.Components.Resource(this.ResourceService, this.$q);
                    break;

                case Utils.Constants.ComponentType.PRODUCT:
                    newComponent = new Models.Components.Product(this.ProductService, this.$q);
                    break;
            }
            newComponent.componentType = componentType;
            newComponent.tags = [];
            newComponent.icon = Utils.Constants.DEFAULT_ICON;
            return newComponent;
        };


        public getServiceFromServer = (componentId: string): ng.IPromise<Models.Components.Service> => {
            let service: Models.Components.Service = <Models.Components.Service>this.createEmptyComponent(Utils.Constants.ComponentType.SERVICE);
            service.setUniqueId(componentId);
            return service.getComponent();
        };

        public getResourceFromServer = (componentId: string): ng.IPromise<Models.Components.Resource> => {
            let resource: Models.Components.Resource = <Models.Components.Resource>this.createEmptyComponent(Utils.Constants.ComponentType.RESOURCE);
            resource.setUniqueId(componentId);
            return resource.getComponent();
        };

        public getComponentFromServer = (componentType: string, componentId: string): ng.IPromise<Models.Components.Component> => {
            let newComponent: Models.Components.Component = this.createEmptyComponent(componentType);
            newComponent.setUniqueId(componentId);
            return newComponent.getComponent();
        };
        
        public createComponentOnServer = (componentObject:Models.Components.Component):ng.IPromise<Models.Components.Component> => {
            let component: Models.Components.Component = this.createComponent(componentObject);
            return component.createComponentOnServer();

        };
    }
}
