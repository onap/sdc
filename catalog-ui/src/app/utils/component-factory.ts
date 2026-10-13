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
import { Injectable } from '@angular/core';
import { IMainCategory, ISubCategory } from 'app/models/category';
import { Component } from 'app/models/components/component';
import { Resource } from 'app/models/components/resource';
import { Service } from 'app/models/components/service';
import { ICsarComponent } from 'app/models/csar-component';
import { LegacyResourceService } from 'app/services/legacy/resource.service';
import { LegacyServiceService } from 'app/services/legacy/service.service';
import * as _ from 'lodash';
import { CacheService } from '../services/cache.service';
import { ComponentService } from '../services/component-services/component.service';
import { ComponentGenericResponse } from '../services/responses/component-generic-response';
import { ComponentType, DEFAULT_ICON, ResourceType } from './constants';

@Injectable()
export class ComponentFactory {

    constructor(private LegacyResourceService: LegacyResourceService,
                private LegacyServiceService: LegacyServiceService,
                private cacheService: CacheService,
                private ComponentService: ComponentService) {
    }

    public createComponent = (component: Component): Component => {
        let newComponent: Component;
        switch (component.componentType) {

            case 'SERVICE':
                newComponent = new Service(this.LegacyServiceService, component as Service);
                break;

            case 'RESOURCE':
                newComponent = new Resource(this.LegacyResourceService, component as Resource);
                break;

        }
        return newComponent;
    }

    public createService = (service: Service): Service => {
        const newService: Service = new Service(this.LegacyServiceService, service as Service);
        return newService;
    }

    public createResource = (resource: Resource): Resource => {
        const newResource: Resource = new Resource(this.LegacyResourceService, resource as Resource);
        return newResource;
    }

    public updateComponentFromCsar = (csarComponent: Resource, oldComponent: Resource): Component => {
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

    public createFromCsarComponent = (csar: ICsarComponent): Component => {
        const newResource: Resource = this.createEmptyComponent(ComponentType.RESOURCE) as Resource;
        newResource.name = csar.vspName;

        /**
         * Onboarding CSAR contains category and sub category that are uniqueId.
         * Need to find the category and sub category and extract the name from them.
         * First concat all sub categories to one array.
         * Then find the selected sub category and category.
         * @type {any}
         */
        const availableCategories = _.cloneDeep(this.cacheService.get('resourceCategories'));
        let allSubs = [];
        _.each(availableCategories, (main: IMainCategory) => {
            if (main.subcategories) {
                allSubs = allSubs.concat(main.subcategories);
            }
        });

        const selectedCategory: IMainCategory = _.find(availableCategories, function(main: IMainCategory) {
            return main.uniqueId === csar.category;
        });

        const selectedSubCategory: ISubCategory = _.find(allSubs, (sub: ISubCategory) => {
            return sub.uniqueId === csar.subCategory;
        });

        // Build the categories and sub categories array (same format as component category)
        const categories: IMainCategory[] = new Array();
        const subcategories: ISubCategory[] = new Array();
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
        newResource.selectedCategory = selectedCategory && selectedSubCategory ? selectedCategory.name + '_#_' + selectedSubCategory.name : '';
        newResource.filterTerm = newResource.name +  ' '  + newResource.description + ' ' + newResource.vendorName + ' ' + newResource.csarVersion;
        return newResource;
    }

    public createEmptyComponent = (componentType: string, resourceType?: string): Component => {
        let newComponent: Component;

        switch (componentType) {
            case ComponentType.SERVICE_PROXY:
            case ComponentType.SERVICE:
            case ComponentType.SERVICE_SUBSTITUTION:
                newComponent = new Service(this.LegacyServiceService);
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
                newComponent = new Resource(this.LegacyResourceService);
                if (resourceType) {
                    (newComponent as Resource).resourceType = resourceType;
                }
                break;
        }
        newComponent.componentType = componentType;
        newComponent.tags = [];
        newComponent.icon = DEFAULT_ICON;
        return newComponent;
    }

    public getComponentFromServer = (componentType: string, componentId: string): Promise<Component> => {
        const newComponent: Component = this.createEmptyComponent(componentType);
        newComponent.setUniqueId(componentId);
        return newComponent.getComponent();
    }

    public createComponentOnServer = (componentObject: Component): Promise<Component> => {
        const component: Component = this.createComponent(componentObject);
        return component.createComponentOnServer();

    }

    public importComponentOnServer = (componentObject: Component): Promise<Component> => {
        const component: Component = this.createComponent(componentObject);
        return component.importComponentOnServer();

    }

    public getComponentWithMetadataFromServer = (componentType: string, componentId: string): Promise<Component> => {
        return new Promise<Component>((resolve) => {
            const component = this.createEmptyComponent(componentType);
            component.setUniqueId(componentId);
            this.ComponentService.getComponentMetadata(component.uniqueId, component.componentType).subscribe((response: ComponentGenericResponse) => {
                component.setComponentMetadata(response.metadata);
                component.derivedFromGenericType = response.derivedFromGenericType;
                component.derivedFromGenericVersion = response.derivedFromGenericVersion;
                component.model = response.model;
                resolve(component);
            });
        });
    }
}
