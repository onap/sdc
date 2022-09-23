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

import { Injectable, Inject } from "@angular/core";
import { Observable } from "rxjs/Observable";
import { SdcConfigToken, ISdcConfig } from "../config/sdc-config.config";
import {Component, DataTypeModel, DataTypesMap, IApi, IComponentsArray} from "app/models";
import { ComponentFactory } from 'app/utils/component-factory';
import {ResourceType} from "../../utils/constants";
import {SharingService} from "./sharing.service";
import { HttpClient, HttpParams } from "@angular/common/http";
import {DataTypesService} from "../../services/data-types-service";

@Injectable()
export class CatalogService {
    protected api:IApi;
    protected baseUrl:string;
    protected baseMicroServiceUrl:string;

    constructor(private http: HttpClient,
                @Inject(SdcConfigToken) sdcConfig:ISdcConfig,
                private componentFactory:ComponentFactory,
                private dataTypesService:DataTypesService,
                private sharingService:SharingService) {
        this.api = sdcConfig.api;
        this.baseUrl = sdcConfig.api.root ;
        this.baseMicroServiceUrl = sdcConfig.api.uicache_root;
    }

    public getCatalog(): Observable<Array<Component | DataTypeModel>> {
        let searchParams = new HttpParams();
        searchParams = searchParams.append('excludeTypes', ResourceType.VFCMT).append('excludeTypes', ResourceType.CONFIGURATION);
        return this.http.get<IComponentsArray>(this.baseMicroServiceUrl + this.api.GET_uicache_catalog, {params: searchParams})
        .map(res => this.processComponentsResponse(res, true));
    }

    public getArchiveCatalog() {
        return this.http.get<IComponentsArray>(this.baseUrl + '/v1/catalog/archive/', {})
            .map(res => this.processComponentsResponse(res));
    }

    private processComponentsResponse(componentsArr: IComponentsArray, addSharing:boolean = false) {
        let dataTypesMap = this.dataTypesService.getAllDataTypes();
        const componentsList: any[] = [];
        if (componentsArr.resources) {
            componentsList.push(...this.getResourceItems(componentsArr.resources));
        }
        if (componentsArr.services) {
            componentsList.push(...this.getServiceItems(componentsArr.services));
        }
        if (dataTypesMap) {
            componentsList.push(...this.getDataTypesItems(dataTypesMap));
        }
        if (addSharing) {
            componentsList.forEach((item) => this.sharingService.addUuidValue(item.uniqueId, item.uuid));
        }
        return componentsList;
    }
    
    private getResourceItems(resources){
        let resourceItems = resources.map((resource)=>{
            return this.componentFactory.createResource(resource)
        })
        return resourceItems;
    }

    private getServiceItems(services){
        let serviceItems = services.map((service)=>{
            return this.componentFactory.createService(service)
        })
        return serviceItems;
    }

    private getDataTypesItems(dataTypesMap: DataTypesMap) {
        const dataTypes = new Array<DataTypeModel>();
        for(const dataTypeKey of Object.keys(dataTypesMap)) {
            dataTypes.push(new DataTypeModel(dataTypesMap[dataTypeKey]))
        }
        let dataTypeItems = dataTypes.map((dataType)=>{
            return this.componentFactory.createDataType(dataType)
        })
        return dataTypeItems;
    }

}    