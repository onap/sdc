/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
import * as _ from "lodash";
import {Injectable} from "@angular/core";
import {DEFAULT_ICON, ResourceType, ComponentType} from "./constants";
// Keep barrel imports for ServiceService/ResourceService. component-factory.ts lives in the
// "app/utils" barrel, which is loaded transitively by many modules (e.g. artifacts.ts). Using
// the "app/services" barrel for these two preserves the loading order that prevents the pre-existing
// resource-service ↔ app/models circular dep from crashing (failure-catalog §TT).
// component-factory.spec.ts mocks "app/services" to break the dep there.
//
// CacheService uses a DIRECT DEEP PATH (not "app/services-ng2") to avoid a separate circular dep:
// "app/services-ng2" → catalog.service.ts → data-type-catalog-component.ts → "app/models",
// but "app/models" is already loading when component-factory.ts is triggered (via app/utils →
// artifacts.ts), causing "service.ts extends Component (undefined)" to crash.
import {ServiceService, ResourceService} from "app/services";
import {CacheService} from "../ng2/services/cache.service";
import {IMainCategory, ISubCategory, ICsarComponent, Component, Resource, Service} from "app/models";
import {ComponentServiceNg2} from "../ng2/services/component-services/component.service";
import {ComponentGenericResponse} from "../ng2/services/responses/component-generic-response";


@Injectable()
export class ComponentFactory {

    constructor(private ResourceService: ResourceService,
                private ServiceService: ServiceService,
                private cacheService: CacheService,
                private ComponentServiceNg2: ComponentServiceNg2) {
    }

    public createComponent = (component:Component):Component => {
        let newComponent:Component;
        switch (component.componentType) {

            case 'SERVICE':
                newComponent = new Service(this.ServiceService, <Service> component);
                break;

            case 'RESOURCE':
                newComponent = new Resource(this.ResourceService, <Resource> component);
                break;

        }
        return newComponent;
    };

    public createService = (service:Service):Service => {
        let newService:Service = new Service(this.ServiceService, <Service> service);
        return newService;
    };

    public createResource = (resource:Resource):Resource => {
        let newResource:Resource = new Resource(this.ResourceService, <Resource> resource);
        return newResource;
    };

    public updateComponentFromCsar = (csarComponent:Resource, oldComponent: Resource): Component => {
          _.pull(oldComponent.tags, oldComponent.name);
          if (!oldComponent.isAlreadyCertified()) {
            oldComponent.name = csarComponent.name;
            oldComponent.categories = csarComponent.categories;
            oldComponent.selectedCategory = csarComponent.selectedCategory;
        }
          oldComponent.vendorName = csarComponent.vendorName;
          oldComponent.vendorRelease = csarComponent.vendorRelease;
          oldComponent.csarUUID = csarComponent.csarUUID;
          oldComponent.csarPackageType = csarComponent.csarPackageType;
          oldComponent.csarVersion = csarComponent.csarVersion;
          oldComponent.csarVersionId = csarComponent.csarVersionId;
          oldComponent.packageId = csarComponent.packageId;
          oldComponent.description = csarComponent.description;
          oldComponent.filterTerm = oldComponent.name +  ' '  + oldComponent.description + ' ' + oldComponent.vendorName + ' ' + oldComponent.csarVersion;
          return oldComponent;
    }

    public createFromCsarComponent = (csar:ICsarComponent):Component => {
        let newResource:Resource = <Resource>this.createEmptyComponent(ComponentType.RESOURCE);
        newResource.name = csar.vspName;

        /**
         * Onboarding CSAR contains category and sub category that are uniqueId.
         * Need to find the category and sub category and extract the name from them.
         * First concat all sub categories to one array.
         * Then find the selected sub category and category.
         * @type {any}
         */
        let availableCategories = _.cloneDeep(this.cacheService.get('resourceCategories'));
        let allSubs = [];
        _.each(availableCategories, (main:IMainCategory)=> {
            if (main.subcategories) {
                allSubs = allSubs.concat(main.subcategories);
            }
        });

        let selectedCategory:IMainCategory = _.find(availableCategories, function (main:IMainCategory) {
            return main.uniqueId === csar.category;
        });

        let selectedSubCategory:ISubCategory = _.find(allSubs, (sub:ISubCategory)=> {
            return sub.uniqueId === csar.subCategory;
        });

        // Build the categories and sub categories array (same format as component category)
        let categories:Array<IMainCategory> = new Array();
        let subcategories:Array<ISubCategory> = new Array();
        if (selectedCategory && selectedSubCategory) {
            subcategories.push(selectedSubCategory);
            selectedCategory.subcategories = subcategories;
            categories.push(selectedCategory);
        }

        // Fill the component with details from CSAR

        newResource.categories = categories;
        newResource.vendorName = csar.vendorName;
        newResource.vendorRelease = csar.vendorRelease;
        newResource.csarUUID = csar.packageId;
        newResource.csarPackageType = csar.packageType;
        newResource.csarVersion = csar.version;
        newResource.packageId = csar.packageId;
        newResource.description = csar.description;
        newResource.resourceType = csar.resourceType;
        newResource.selectedCategory = selectedCategory && selectedSubCategory ? selectedCategory.name + "_#_" + selectedSubCategory.name : '';
        newResource.filterTerm = newResource.name +  ' '  + newResource.description + ' ' + newResource.vendorName + ' ' + newResource.csarVersion;
        return newResource;
    };

    public createEmptyComponent = (componentType:string, resourceType?:string):Component => {
        let newComponent:Component;

        switch (componentType) {
            case ComponentType.SERVICE_PROXY:
            case ComponentType.SERVICE:
            case ComponentType.SERVICE_SUBSTITUTION:
                newComponent = new Service(this.ServiceService);
                break;

            case ComponentType.RESOURCE:
            case ResourceType.VF:
            case ResourceType.VL:
            case ResourceType.VFC:
            case ResourceType.CP:
            case ResourceType.CR:
            case ResourceType.PNF:
            case ResourceType.CVFC:
            case ResourceType.CONFIGURATION:
                newComponent = new Resource(this.ResourceService);
                if (resourceType){
                    (<Resource> newComponent).resourceType = resourceType;
                }
                break;
        }
        newComponent.componentType = componentType;
        newComponent.tags = [];
        newComponent.icon = DEFAULT_ICON;
        return newComponent;
    };

    public getComponentFromServer = (componentType:string, componentId:string):Promise<Component> => {
        let newComponent:Component = this.createEmptyComponent(componentType);
        newComponent.setUniqueId(componentId);
        return newComponent.getComponent();
    };

    public createComponentOnServer = (componentObject:Component):Promise<Component> => {
        let component:Component = this.createComponent(componentObject);
        return component.createComponentOnServer();

    };

    public importComponentOnServer = (componentObject: Component): Promise<Component> => {
        let component: Component = this.createComponent(componentObject);
        return component.importComponentOnServer();

    };

    public getComponentWithMetadataFromServer = (componentType:string, componentId:string):Promise<Component> => {
        return new Promise<Component>((resolve) => {
            let component = this.createEmptyComponent(componentType);
            component.setUniqueId(componentId);
            this.ComponentServiceNg2.getComponentMetadata(component.uniqueId, component.componentType).subscribe((response:ComponentGenericResponse) => {
                component.setComponentMetadata(response.metadata);
                component.derivedFromGenericType = response.derivedFromGenericType;
                component.derivedFromGenericVersion = response.derivedFromGenericVersion;
                component.model = response.model;
                resolve(component);
            });
        });
    }
}
