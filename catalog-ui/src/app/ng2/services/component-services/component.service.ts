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

import * as _ from "lodash";
import {Injectable, Inject} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import {Response, URLSearchParams} from '@angular/http';
import { Component, ComponentInstance, InputBEModel, InstancePropertiesAPIMap, FilterPropertiesAssignmentData,
    PropertyBEModel, OperationModel, BEOperationModel, Capability, Requirement
} from "app/models";
import {downgradeInjectable} from '@angular/upgrade/static';
import {COMPONENT_FIELDS, CommonUtils, SERVICE_FIELDS} from "app/utils";
import {ComponentGenericResponse} from "../responses/component-generic-response";
import {InstanceBePropertiesMap} from "../../../models/properties-inputs/property-fe-map";
import {API_QUERY_PARAMS} from "app/utils";
import { ComponentType, ServerTypeUrl } from "../../../utils/constants";
import { HttpService } from '../http.service';
import {SdcConfigToken, ISdcConfig} from "../../config/sdc-config.config";
import {ConstraintObject} from 'app/ng2/components/logic/service-dependencies/service-dependencies.component';
import {IDependenciesServerResponse} from "../responses/dependencies-server-response";
import {AutomatedUpgradeGenericResponse} from "../responses/automated-upgrade-response";
import {IAutomatedUpgradeRequestObj} from "../../pages/automated-upgrade/automated-upgrade.service";

declare var angular:angular.IAngularStatic;

@Injectable()
export class ComponentServiceNg2 {

    protected baseUrl;

    constructor(protected http:HttpService, @Inject(SdcConfigToken) sdcConfig:ISdcConfig) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
    }

    protected getComponentDataByFieldsName(componentType:string, componentId: string, fields:Array<string>):Observable<ComponentGenericResponse> {

        let params:URLSearchParams = new URLSearchParams();
        _.forEach(fields, (field:string):void => {
            params.append(API_QUERY_PARAMS.INCLUDE, field);
        });

        return this.http.get(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/filteredDataByParams', {search: params})
            .map((res:Response) => {
                return this.analyzeComponentDataResponse(res);
            });
    }

    protected analyzeComponentDataResponse(res: Response):ComponentGenericResponse {
        return new ComponentGenericResponse().deserialize(res.json());
    }

    private getServerTypeUrl = (componentType:string):string => {
        switch (componentType) {
            case ComponentType.SERVICE:
                return ServerTypeUrl.SERVICES;
            default:
                return ServerTypeUrl.RESOURCES;
        }
    }

    getComponentMetadata(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_METADATA]);
    }

    getComponentInstanceAttributesAndProperties(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_PROPERTIES, COMPONENT_FIELDS.COMPONENT_INSTANCES_ATTRIBUTES]);
    }

    getComponentInstanceProperties(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_PROPERTIES]);
    }

    getComponentAttributes(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_ATTRIBUTES]);
    }

    getComponentCompositionData(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_RELATION, COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_POLICIES, COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_GROUPS]);
    }

    getComponentResourcePropertiesData(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_POLICIES, COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_GROUPS]);
    }

    getComponentResourceInstances(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES]);
    }

    getComponentInputs(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INPUTS]);
    }

    getComponentDeploymentArtifacts(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_DEPLOYMENT_ARTIFACTS]);
    }

    getComponentInformationalArtifacts(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INFORMATIONAL_ARTIFACTS]);
    }

    getComponentInformationalArtifactsAndInstances(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INFORMATIONAL_ARTIFACTS, COMPONENT_FIELDS.COMPONENT_INSTANCES]);
    }

    getComponentToscaArtifacts(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_TOSCA_ARTIFACTS]);
    }

    getComponentProperties(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_PROPERTIES]);
    }

    getInterfaces(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INTERFACE_OPERATIONS]);
    }

    getInterfaceOperation(component:Component, operation:OperationModel):Observable<OperationModel> {
        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/interfaces/' + operation.interfaceId + '/operations/' + operation.uniqueId)
            .map((res:Response) => {
                return res.json();
            });
    }

    createInterfaceOperation(component:Component, operation:OperationModel):Observable<OperationModel> {
        const operationList = {
            'interfaces': {
                [operation.interfaceType]: {
                    'type': operation.interfaceType,
                    'operations': {
                        [operation.name]: new BEOperationModel(operation)
                    }
                }
            }
        };
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/interfaceOperations', operationList)
            .map((res:Response) => {
                const interf = _.find(res.json().interfaces, (interf: any) => interf.type === operation.interfaceType);
                const newOperation = _.find(interf.operations, (op:OperationModel) => op.name === operation.name);
                return new OperationModel({
                    ...newOperation,
                    interfaceType: interf.type,
                    interfaceId: interf.uniqueId
                });
            });
    }

    updateInterfaceOperation(component:Component, operation:OperationModel):Observable<OperationModel> {
        const operationList = {
            'interfaces': {
                [operation.interfaceType]: {
                    'type': operation.interfaceType,
                    'operations': {
                        [operation.name]: new BEOperationModel(operation)
                    }
                }
            }
        };
        return this.http.put(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/interfaceOperations', operationList)
            .map((res:Response) => {
                const interf = _.find(res.json().interfaces, (interf: any) => interf.type === operation.interfaceType);
                const newOperation = _.find(interf.operations, (op:OperationModel) => op.name === operation.name);
                return new OperationModel({
                    ...newOperation,
                    interfaceType: interf.type,
                    interfaceId: interf.uniqueId
                });
            });
    }

    deleteInterfaceOperation(component:Component, operation:OperationModel):Observable<OperationModel> {
        return this.http.delete(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/interfaces/' + operation.interfaceId + '/operations/' + operation.uniqueId)
            .map((res:Response) => {
                return res.json();
            });
    }

    getInterfaceTypes(component:Component):Observable<{[id:string]: Array<string>}> {
        return this.http.get(this.baseUrl + 'interfaceLifecycleTypes')
            .map((res:Response) => {
                const interfaceMap = {};
                _.forEach(res.json(), (interf:any) => {
                    interfaceMap[interf.toscaPresentation.type] = _.keys(interf.toscaPresentation.operations);
                });
                return interfaceMap;
            });
    }

    getCapabilitiesAndRequirements(componentType: string, componentId:string):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_REQUIREMENTS, COMPONENT_FIELDS.COMPONENT_CAPABILITIES]);
    }

    createCapability(component: Component, capabilityData: Capability): Observable<Array<Capability>> {
        let capBEObj = {
            'capabilities': {
                [capabilityData.type]: [capabilityData]
            }
        };
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/capabilities', capBEObj)
            .map((res: Response) => {
                return res.json();
            });
    }

    updateCapability(component: Component, capabilityData: Capability): Observable<Array<Capability>> {
        let capBEObj = {
            'capabilities': {
                [capabilityData.type]: [capabilityData]
            }
        };
        return this.http.put(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/capabilities', capBEObj)
            .map((res: Response) => {
                return res.json();
            });
    }

    deleteCapability(component: Component, capId: string): Observable<Capability> {
        return this.http.delete(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/capabilities/' + capId)
            .map((res: Response) => {
                return res.json();
            });
    }

    createRequirement(component: Component, requirementData: Requirement): Observable<any> {
        let reqBEObj = {
            'requirements': {
                [requirementData.capability]: [requirementData]
            }
        };
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/requirements', reqBEObj)
            .map((res: Response) => {
                return res.json();
            });
    }

    updateRequirement(component: Component, requirementData: Requirement): Observable<any> {
        let reqBEObj = {
            'requirements': {
                [requirementData.capability]: [requirementData]
            }
        };
        return this.http.put(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/requirements', reqBEObj)
            .map((res: Response) => {
                return res.json();
            });
    }

    deleteRequirement(component: Component, reqId: string): Observable<Requirement> {
        return this.http.delete(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/requirements/' + reqId)
            .map((res: Response) => {
                return res.json();
            });
    }

    getDeploymentGraphData(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_RELATION, COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_GROUPS]);
    }

    createInput(component:Component, inputsToCreate:InstancePropertiesAPIMap, isSelf:boolean):Observable<any> {
        let inputs = isSelf ? { serviceProperties: inputsToCreate.componentInstanceProperties } : inputsToCreate;
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/create/inputs', inputs)
            .map(res => {
                return res.json();
            })
    }

    restoreComponent(componentType:string, componentId:string){ 
        return this.http.post(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/restore', {})
    }

    archiveComponent(componentType:string, componentId:string){
        return this.http.post(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/archive', {})
    }


    deleteInput(component:Component, input:InputBEModel):Observable<InputBEModel> {
        return this.http.delete(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/delete/' + input.uniqueId + '/input')
            .map((res:Response) => {
                return new InputBEModel(res.json());
            });
    }

    updateComponentInputs(component:Component, inputs:InputBEModel[]):Observable<InputBEModel[]> {
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/update/inputs', inputs)
            .map((res:Response) => {
                return res.json().map((input) => new InputBEModel(input));
            })
    }

    filterComponentInstanceProperties(component: Component, filterData:FilterPropertiesAssignmentData): Observable<InstanceBePropertiesMap> {//instance-property-be-map
        let params: URLSearchParams = new URLSearchParams();
        _.forEach(filterData.selectedTypes, (type:string) => {
            params.append('resourceType', type);
        });

        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/filteredproperties/' + filterData.propertyName, {search: params})
            .map((res: Response) => {
                return res.json();
            });
    }

    createServiceProperty(component: Component, propertyModel:PropertyBEModel): Observable<PropertyBEModel> {
        let serverObject = {};
        serverObject[propertyModel.name] = propertyModel;
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/properties', serverObject)
            .map(res => {
                let property:PropertyBEModel = new PropertyBEModel(res.json());
                return property;
            })
    }

    getServiceProperties(component: Component): Observable<Array<PropertyBEModel>> {
        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/properties')
            .map((res: Response) => {
                if (!res.text()){
                    return new Array<PropertyBEModel>();
                }
                return CommonUtils.initBeProperties(res.json());
            });
    }

    updateServiceProperties(component: Component, properties: PropertyBEModel[]) {
        return this.http.put( this.baseUrl + component.getTypeUrl() + component.uniqueId + '/properties', properties)
            .map((res: Response) => {
                const resJson = res.json();
                return _.map(resJson,
                    (resValue:PropertyBEModel) => new PropertyBEModel(resValue));
            });
    }

    deleteServiceProperty(component:Component, property:PropertyBEModel):Observable<string> {
        return this.http.delete(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/properties/' + property.uniqueId )
            .map((res:Response) => {
                return property.uniqueId;
            })
    }

    getDependencies(componentType:string, componentId: string):Observable<Array<IDependenciesServerResponse>> {
        return this.http.get(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/dependencies')
            .map((res:Response) => {
                return res.json();
            });
    }

    automatedUpgrade(componentType:string, componentId: string, componentsIdsToUpgrade:Array<IAutomatedUpgradeRequestObj>):Observable<AutomatedUpgradeGenericResponse> {
        return this.http.post(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/automatedupgrade', componentsIdsToUpgrade)
            .map((res:Response) => {
                return res.json();
            });
    }

    updateComponentInstance(component:Component, componentInstance:ComponentInstance):Observable<ComponentInstance> {
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstance/' + componentInstance.uniqueId, componentInstance)
            .map((res:Response) => {
                return res.json();
            });
    }

    getServiceFilterConstraints(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [SERVICE_FIELDS.NODE_FILTER]);
    }

    createServiceFilterConstraints(component:Component, componentInstance:ComponentInstance, constraint:ConstraintObject):Observable<any> {
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstances/' + componentInstance.uniqueId + '/nodeFilter', constraint)
            .map((res:Response) => {
                return res.json();
            });
    }

    updateServiceFilterConstraints(component:Component, componentInstance:ComponentInstance, constraints:Array<ConstraintObject>):Observable<any> {
        return this.http.put(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstances/' + componentInstance.uniqueId + '/nodeFilter/', constraints)
            .map((res:Response) => {
                return res.json();
            });
    }

    deleteServiceFilterConstraints(component:Component, componentInstance:ComponentInstance, constraintIndex:number) {
        return this.http.delete(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstances/' + componentInstance.uniqueId + '/nodeFilter/' + constraintIndex)
            .map((res:Response) => {
                return res.json();
            });
    }
}

