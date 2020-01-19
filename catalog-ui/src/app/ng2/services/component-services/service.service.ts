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

import { Injectable, Inject } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import {Service} from "app/models";

import {SdcConfigToken, ISdcConfig} from "../../config/sdc-config.config";
import {ForwardingPath} from "app/models/forwarding-path";
import {ComponentMetadata} from "app/models/component-metadata";
import {ComponentType} from "app/utils";
import {Component} from "app/models/components/component";
import {ComponentGenericResponse} from "app/ng2/services/responses/component-generic-response";
import {COMPONENT_FIELDS, SERVICE_FIELDS} from "app/utils/constants";
import {ComponentServiceNg2} from "./component.service";
import {ServicePathMapItem} from "app/models/graph/nodes-and-links-map";
import { HttpClient, HttpParams } from '@angular/common/http';
import { OperationModel } from '../../../models/operation';
import { ConsumptionInput } from '../../components/logic/service-consumption/service-consumption.component';

@Injectable()
export class ServiceServiceNg2 extends ComponentServiceNg2 {

    protected baseUrl = "";

    constructor(protected http: HttpClient, @Inject(SdcConfigToken) sdcConfig:ISdcConfig) {
        super(http, sdcConfig);
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
    }

    validateConformanceLevel(service: Service): Observable<boolean> {

        return this.http.get<boolean>(this.baseUrl + service.getTypeUrl() + service.uuid + '/conformanceLevelValidation');
    }

    getNodesAndLinksMap(serviceId: string):Observable<Array<ServicePathMapItem>> {
        return this.http.get<Array<ServicePathMapItem>>(this.baseUrl + 'services/' + serviceId + '/linksMap');
    }

    createOrUpdateServicePath(serviceId: string, inputsToCreate: ForwardingPath):Observable<ForwardingPath> {
        if (inputsToCreate.uniqueId) {
            return this.updateServicePath(serviceId, inputsToCreate);
        } else {
            return this.createServicePath(serviceId, inputsToCreate);
        }
    }

    createServicePath(serviceId: string, inputsToCreate: ForwardingPath):Observable<ForwardingPath> {
        let input = new ServicePathRequestData(inputsToCreate);
        return this.http.post<ForwardingPath>(this.baseUrl  + 'services/' + serviceId +   '/paths', input).map((res:any) => {
            return this.parseServicePathResponse(res);
        });
    }

    deleteServicePath(serviceId: string, id: string):Observable<any> {
        return this.http.delete<any>(this.baseUrl  + 'services/' + serviceId + '/paths/' + id);
    }

    updateServicePath(serviceId: string, inputsToUpdate:ForwardingPath):Observable<ForwardingPath> {
        let input = new ServicePathRequestData(inputsToUpdate);

        return this.http.put<{[key:string]:ForwardingPath}>(this.baseUrl  + 'services/' + serviceId + '/paths', input)
            .map((res) => {
                return this.parseServicePathResponse(res);
            });
    }

    getServiceConsumptionData(service: Service):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(service.componentType, service.uniqueId, [
            COMPONENT_FIELDS.COMPONENT_INSTANCES_INTERFACES,
            COMPONENT_FIELDS.COMPONENT_INSTANCES_PROPERTIES,
            COMPONENT_FIELDS.COMPONENT_INSTANCES_INPUTS,
            COMPONENT_FIELDS.COMPONENT_INPUTS,
            COMPONENT_FIELDS.COMPONENT_INSTANCES,
            COMPONENT_FIELDS.COMPONENT_CAPABILITIES
        ]);
    }

    getServiceConsumptionInputs(service: Service, serviceInstanceId: String, interfaceId: string, operation: OperationModel): Observable<any> {
        return this.http.get<any>(this.baseUrl + service.getTypeUrl() + service.uniqueId + '/consumption/' + serviceInstanceId + '/interfaces/' + interfaceId +
            '/operations/' + operation.uniqueId + '/inputs');
    }

    createOrUpdateServiceConsumptionInputs(service: Service, serviceInstanceId: String, consumptionInputsList: Array<{[id: string]: Array<ConsumptionInput>}>): Observable<any> {
        return this.http.post(this.baseUrl + service.getTypeUrl() + service.uniqueId + '/consumption/' + serviceInstanceId, consumptionInputsList);
    }

    checkComponentInstanceVersionChange(componentType:string, componentId:string, instanceId:string, newInstanceId:string):Observable<Array<string>> {
        let queries = {componentInstanceId: instanceId, newComponentInstanceId: newInstanceId};

        let params:HttpParams = new HttpParams();
        _.map(_.keys(queries), (key:string):void => {
            params = params.append(key, queries[key]);
        });

        let url = this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/paths-to-delete';
        return this.http.get<any>(url, {params: params}).map((res) => {
            return res.forwardingPathToDelete;
        });
    }

    getComponentCompositionData(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_RELATION, COMPONENT_FIELDS.COMPONENT_INSTANCES, SERVICE_FIELDS.FORWARDING_PATHS, COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_POLICIES, COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_GROUPS]);
    }

    private parseServicePathResponse(res: { [key:string]:ForwardingPath }):ForwardingPath {
        let resJSON = res;
        let pathId = Object.keys(resJSON.forwardingPaths)[0];
        let forwardingPath = resJSON.forwardingPaths[pathId];
        let path:ForwardingPath = new ForwardingPath();
        path.deserialize(forwardingPath);
        path.uniqueId = pathId;
        return path;
    }
}

class ServicePathRequestData {
    forwardingPaths: { [key:string]:ForwardingPath } = {};
    componentMetadataDefinition: ComponentMetadata;
    toscaType: string = "topology_template";

    constructor(fp? : ForwardingPath) {
        this.componentMetadataDefinition = new ComponentMetadata();
        this.componentMetadataDefinition.ecompGeneratedNaming = true;
        this.componentMetadataDefinition.componentType = ComponentType.SERVICE;
        if (fp) {
            let id = fp.uniqueId ? fp.uniqueId : "NEW";
            this.forwardingPaths[fp.uniqueId] = fp;
        }
    }
}

