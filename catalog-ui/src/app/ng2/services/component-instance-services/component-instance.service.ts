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
import {Component, ComponentInstance, InputModel} from "app/models";
import { HttpService } from '../http.service';
import {SdcConfigToken, ISdcConfig} from "../../config/sdc-config.config";
import {isEqual} from "lodash";

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

    updateInstanceProperty(component: Component, componentInstanceId: string, property: PropertyBEModel): Observable<PropertyBEModel> {

        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstance/' + componentInstanceId + '/property', property)
            .map((res: Response) => {
                return new PropertyBEModel(res.json());
        })
    }

    getInstanceCapabilityProperties(component: Component, componentInstanceId: string, capabilityType: string, capabilityName: string): Observable<Array<PropertyBEModel>> {

        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstanceId + '/capability/' + capabilityType +
            '/capabilityName/' +  capabilityName + '/properties')
            .map((res: Response) => {
                return CommonUtils.initBeProperties(res.json());
            })
    }

    updateInstanceCapabilityProperties(component: Component, componentInstanceId: string, capabilityType: string, capabilityName: string, properties: PropertyBEModel[]): Observable<PropertyBEModel[]> {

        return this.http.put(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstanceId + '/capability/' +  capabilityType +
            '/capabilityName/' +  capabilityName +'/properties', properties)
            .map((res: Response) => {
                return res.json().map((resProperty) => new PropertyBEModel(resProperty));
            })
    }

    updateInstanceInput(component: Component, componentInstanceId: string, input: PropertyBEModel): Observable<PropertyBEModel> {

        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstance/' + componentInstanceId + '/input', input)
            .map((res: Response) => {
                return new PropertyBEModel(res.json());
            })
    }

    hasPropertyChanged(property: PropertyFEModel) {
        let oldValue: any = property.value;
        const newValue = property.getJSONValue();
        return ((oldValue || newValue) && !isEqual(oldValue, newValue));
    }
}
