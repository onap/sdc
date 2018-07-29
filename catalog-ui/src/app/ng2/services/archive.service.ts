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
import { HttpService } from "./http.service";
import { SdcConfigToken, ISdcConfig } from "../config/sdc-config.config";
import { Component } from "../../models";
import { ComponentFactory } from 'app/utils/component-factory';


@Injectable()
export class ArchiveService {
    protected baseUrl;

    constructor(private http: HttpService, @Inject(SdcConfigToken) sdcConfig:ISdcConfig, private componentFactory:ComponentFactory/*, @Inject(ComponentFactory) componentFactory */) {
        this.baseUrl = sdcConfig.api.root ;
    }

    public getArchiveCatalog() {
        let archiveCatalogItems:Component[] = [];
        let archiveCatalogResourceItems:Component[] = []; 
        let archiveCatalogServiceItems:Component[] = [];

        return this.http.get(this.baseUrl + '/v1/catalog/archive/', {}).map(res => {
            let archiveCatalogObject = res.json();
            if (archiveCatalogObject.resources) archiveCatalogResourceItems = this.getResourceItems(archiveCatalogObject.resources);
            if (archiveCatalogObject.services) archiveCatalogServiceItems = this.getServiceItems(archiveCatalogObject.services);
            archiveCatalogItems = [].concat(archiveCatalogResourceItems, archiveCatalogServiceItems);
            
            return archiveCatalogItems;
        });
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

}    