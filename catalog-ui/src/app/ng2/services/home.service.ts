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
import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Component, IApi, Resource, Service } from 'app/models';
import { ComponentFactory } from 'app/utils/component-factory';
import { CredentialService } from "./credential.service";
import { Observable } from 'rxjs';
import { ISdcConfig, SdcConfigToken } from '../config/sdc-config.config';
import { SharingService } from './sharing.service';
import { HttpHeaders } from '@angular/common/http';

// tslint:disable-next-line:interface-name
interface IComponentsArray {
    services: Service[];
    resources: Resource[];
}

@Injectable()
export class HomeService {
    private api: IApi;
    private smallObjectAttributes = [
        'uniqueId', 'name', 'componentType', 'resourceType', 'lastUpdateDate', 'lifecycleState', 'distributionStatus',
        'icon', 'version'
    ];

    constructor(private http: HttpClient,
                private credentialService:CredentialService,
                @Inject(SdcConfigToken) private sdcConfig: ISdcConfig,
                private sharingService: SharingService,
                private componentFactory: ComponentFactory) {
        this.api = sdcConfig.api;
        this.credentialService = credentialService;
    }

    private getAuthHeaders():HttpHeaders {
        let authHeaders: HttpHeaders = new HttpHeaders();
        if (this.credentialService.basicAuthEnabled()) {
            authHeaders = authHeaders.set('Authorization', this.credentialService.getBasicAuth())
        }
        return authHeaders;
    }

    public getAllComponents(smallObjects?: boolean): Observable<Component[]> {
        return this.http.get<IComponentsArray>(this.api.root + this.api.GET_element, {headers: this.getAuthHeaders()})
            .map((response) => {
                const componentResponse: IComponentsArray = response;
                let componentsList: Component[] = [];

                componentResponse.services && componentResponse.services.forEach((serviceResponse: Service) => {
                    serviceResponse = (smallObjects) ? _.pick(serviceResponse, this.smallObjectAttributes) : serviceResponse;
                    const component: Service = this.componentFactory.createService(serviceResponse);
                    componentsList.push(component);
                    this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                });

                componentResponse.resources && componentResponse.resources.forEach((resourceResponse: Resource) => {
                    resourceResponse = (smallObjects) ? _.pick(resourceResponse, this.smallObjectAttributes) : resourceResponse;
                    const component: Resource = this.componentFactory.createResource(resourceResponse);
                    componentsList.push(component);
                    this.sharingService.addUuidValue(component.uniqueId, component.uuid);
                });

                componentsList = _.orderBy(componentsList, ['lastUpdateDate'], ['desc']);

                return componentsList;
            });
    }
}
