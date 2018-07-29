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

import {Injectable, Inject} from '@angular/core';
import {Response, RequestOptions, Headers} from '@angular/http';
import { Observable } from 'rxjs/Observable';
import {PropertyFEModel, PropertyBEModel} from "app/models";
import {CommonUtils} from "app/utils";
import {Component, ComponentInstance, Capability, PropertyModel} from "app/models";
import { HttpService } from '../http.service';
import {SdcConfigToken, ISdcConfig} from "../../config/sdc-config.config";

@Injectable()
export class ComponentInstanceServiceNg2 {

    protected baseUrl;

    constructor(private http: HttpService, @Inject(SdcConfigToken) sdcConfig:ISdcConfig) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
    }

    getComponentInstanceProperties(component: Component, componentInstanceId: string): Observable<Array<PropertyBEModel>> {

        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstanceId + '/properties')
            .map((res: Response) => {
                return CommonUtils.initBeProperties(res.json());
        })
    }

    getComponentInstanceInputs(component: Component, componentInstance: ComponentInstance): Observable<Array<PropertyBEModel>> {
        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstance.uniqueId + '/' + componentInstance.componentUid + '/inputs')
            .map((res: Response) => {
                return CommonUtils.initInputs(res.json());
            })
    }

    updateInstanceProperties(component: Component, componentInstanceId: string, properties: PropertyBEModel[]) {

        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstance/' + componentInstanceId + '/properties', properties)
            .map((res: Response) => {
                return res.json().map((resProperty) => new PropertyBEModel(resProperty));
            });
    }

    getInstanceCapabilityProperties(component: Component, componentInstanceId: string, capability: Capability): Observable<Array<PropertyModel>> {

        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstanceId + '/capability/' + capability.type +
            '/capabilityName/' +  capability.name + '/ownerId/' + capability.ownerId + '/properties')
            .map((res: Response) => {
                capability.properties = res.json().map((capProp) => new PropertyModel(capProp));  // update capability properties
                return capability.properties;
            })
    }

    updateInstanceCapabilityProperties(component: Component, componentInstanceId: string, capability: Capability, properties: PropertyBEModel[]): Observable<Array<PropertyModel>> {

        return this.http.put(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstanceId + '/capability/' +  capability.type +
            '/capabilityName/' +  capability.name + '/ownerId/' + capability.ownerId + '/properties', properties)
            .map((res: Response) => {
                const savedProperties: PropertyModel[] = res.json().map((resProperty) => new PropertyModel(resProperty));
                savedProperties.forEach((savedProperty) => {
                    const propIdx = capability.properties.findIndex((p) => p.uniqueId === savedProperty.uniqueId);
                    if (propIdx !== -1) {
                        capability.properties.splice(propIdx, 1, savedProperty);
                    }
                });
                return savedProperties;
            })
    }

    updateInstanceInputs(component: Component, componentInstanceId: string, inputs: PropertyBEModel[]): Observable<PropertyBEModel[]> {

        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstance/' + componentInstanceId + '/inputs', inputs)
            .map((res: Response) => {
                return res.json().map((resInput) => new PropertyBEModel(resInput));
            });
    }

    getComponentGroupInstanceProperties(component: Component, groupInstanceId: string): Observable<Array<PropertyBEModel>> {
        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/groups/' + groupInstanceId + '/properties')
            .map((res: Response) => {
                return CommonUtils.initBeProperties(res.json());
            });
    }

    updateComponentGroupInstanceProperties(component: Component, groupInstanceId: string, properties: PropertyBEModel[]): Observable<Array<PropertyBEModel>> {
        return this.http.put(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/groups/' + groupInstanceId + '/properties', properties)
            .map((res: Response) => {
                return res.json().map((resProperty) => new PropertyBEModel(resProperty));
            });
    }

    getComponentPolicyInstanceProperties(component: Component, policyInstanceId: string): Observable<Array<PropertyBEModel>> {
        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/policies/' + policyInstanceId + '/properties')
            .map((res: Response) => {
                return CommonUtils.initBeProperties(res.json());
            });
    }

    updateComponentPolicyInstanceProperties(component: Component, policyInstanceId: string, properties: PropertyBEModel[]): Observable<Array<PropertyBEModel>> {
        return this.http.put(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/policies/' + policyInstanceId + '/properties', properties)
            .map((res: Response) => {
                return res.json().map((resProperty) => new PropertyBEModel(resProperty));
            });
    }
}
