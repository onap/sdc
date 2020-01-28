/**
 * Created by rc2122 on 6/4/2018.
 */
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
import {Inject, Injectable} from "@angular/core";
import {SdcConfigToken, ISdcConfig} from "app/ng2/config/sdc-config.config";
import {Observable} from "rxjs/Observable";
import { HttpClient, HttpResponse } from "@angular/common/http";
import { ComponentFactory } from "../../utils/component-factory";
import { DEFAULT_ICON, ComponentType } from "../../utils/constants";
import { ICsarComponent } from "../../models/csar-component";
import { IApi } from "../../models/app-config";
import { CacheService } from "./cache.service";
import { IComponentMetadata, ComponentMetadata } from "../../models/component-metadata";
import { IMainCategory, ISubCategory } from "../../models/category";
import { Resource } from "../../models/components/resource";

export interface OnboardingComponents {
    listCount: number;
    results: Array<ICsarComponent>
} 

@Injectable()
export class OnboardingService {
    private api:IApi;

    constructor(protected http: HttpClient,
                private cacheService:CacheService,
                @Inject(SdcConfigToken) sdcConfig:ISdcConfig,
                private componentFactory: ComponentFactory) {
                this.api = sdcConfig.api;
    }

    getOnboardingVSPs = (): Observable<Array<ICsarComponent>> =>{
        return this.http.get<OnboardingComponents>(this.api.GET_onboarding).map((onboardingVSPs) =>{
            return onboardingVSPs.results
        });
    }

    getOnboardingComponents = ():Observable<Array<IComponentMetadata>> => {
        return this.getOnboardingVSPs().map((onboardingComponents) => {
            let componentsMetadataList: Array<IComponentMetadata> = new Array();
            onboardingComponents.forEach((obc:ICsarComponent) => {
                let componentMetaData: ComponentMetadata = this.createFromCsarComponent(obc);
                componentsMetadataList.push(componentMetaData);
            });
            return componentsMetadataList;
        });
    };
    
    public createFromCsarComponent = (csar:ICsarComponent): ComponentMetadata => {
        let newMetadata = new ComponentMetadata();  
        newMetadata.name = csar.vspName;

        /**
         * Onboarding CSAR contains category and sub category that are uniqueId.
         * Need to find the category and sub category and extract the name from them.
         * First concat all sub categories to one array.
         * Then find the selected sub category and category.
         * @type {any}
         */
        let availableCategories = angular.copy(this.cacheService.get('resourceCategories'));
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

        newMetadata.categories = categories;
        newMetadata.vendorName = csar.vendorName;
        newMetadata.vendorRelease = csar.vendorRelease;
        newMetadata.csarUUID = csar.packageId;
        newMetadata.csarPackageType = csar.packageType;
        newMetadata.csarVersion = csar.version;
        newMetadata.packageId = csar.packageId;
        newMetadata.description = csar.description;
        newMetadata.selectedCategory = selectedCategory && selectedSubCategory ? selectedCategory.name + "_#_" + selectedSubCategory.name : '';
        newMetadata.filterTerm = newMetadata.name +  ' '  + newMetadata.description + ' ' + newMetadata.vendorName + ' ' + newMetadata.csarVersion;
        newMetadata.resourceType = csar.resourceType;
        newMetadata.componentType = ComponentType.RESOURCE;
        newMetadata.tags = [];
        newMetadata.icon = DEFAULT_ICON;
        newMetadata.iconSprite = "sprite-resource-icons";
        return newMetadata;
    };

    downloadOnboardingCsar = (packageId:string):Observable<HttpResponse<Blob>> => {
        return this.http.get(this.api.GET_onboarding + "/" + packageId, {observe: 'response', responseType: 'blob'});
    };

    getComponentFromCsarUuid = (csarUuid:string):Observable<ComponentMetadata> => {
        return this.http.get<ComponentMetadata>(this.api.root + this.api.GET_component_from_csar_uuid.replace(':csar_uuid', csarUuid))
            .map((response: any) => {
                // If the status is 400, this means that the component not found.
                // I do not want to return error from server, because a popup will appear in client with the error.
                // So returning success (200) with status 400.
                if (response.status !== 400) {
                    let componentMetadata = new ComponentMetadata();
                    componentMetadata = response;
                    return componentMetadata;
                }
            });
    };

    //TODO remove when workspace page convert to angular5
    convertMetaDataToComponent(componentMetadata: ComponentMetadata) {
        let newResource: Resource = <Resource>this.componentFactory.createEmptyComponent(ComponentType.RESOURCE);
        newResource.setComponentMetadata(componentMetadata);
        return newResource;
    }
}
