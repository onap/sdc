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
import {Inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import {
    Component,
    FilterPropertiesAssignmentData,
    InputBEModel,
    InstancePropertiesAPIMap,
    OperationModel
} from "app/models";
import {API_QUERY_PARAMS, COMPONENT_FIELDS} from "app/utils";
import {ComponentGenericResponse} from "../responses/component-generic-response";
import {InstanceBePropertiesMap} from "../../../models/properties-inputs/property-fe-map";
import {ComponentType, ServerTypeUrl, SERVICE_FIELDS} from "../../../utils/constants";
import {ISdcConfig, SdcConfigToken} from "../../config/sdc-config.config";
import {IDependenciesServerResponse} from "../responses/dependencies-server-response";
import {AutomatedUpgradeGenericResponse} from "../responses/automated-upgrade-response";
import {IAutomatedUpgradeRequestObj} from "../../pages/automated-upgrade/automated-upgrade.service";
import {ComponentInstance} from "../../../models/componentsInstances/componentInstance";
import {CommonUtils} from "../../../utils/common-utils";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {BEOperationModel, InterfaceModel} from "../../../models/operation";
import {PropertyBEModel} from "../../../models/properties-inputs/property-be-model";
import {PolicyInstance} from "../../../models/graph/zones/policy-instance";
import {
    ConstraintObject
} from "../../components/logic/service-dependencies/service-dependencies.component";
import {OutputBEModel} from "app/models/attributes-outputs/output-be-model";
import {HttpHelperService} from '../http-hepler.service';
import {
    BEInterfaceOperationModel,
    InterfaceOperationModel
} from "../../../models/interfaceOperation";

/*
PLEASE DO NOT USE THIS SERVICE IN ANGULAR2! Use the topology-template.service instead
 */
@Injectable()
export class ComponentServiceNg2 {

    protected baseUrl;

    constructor(protected http: HttpClient, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
    }

    protected getComponentDataByFieldsName(componentType: string, componentId: string, fields: Array<string>): Observable<ComponentGenericResponse> {

        let params: HttpParams = new HttpParams();
        fields.forEach((field: string): void => {
            params = params.append(API_QUERY_PARAMS.INCLUDE, field);
        });

        return this.http.get<ComponentGenericResponse>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/filteredDataByParams', {params: params})
        .map((res) => {
            return new ComponentGenericResponse().deserialize(res);
        });
    }

    protected getServerTypeUrl = (componentType: string): string => {
        switch (componentType) {
            case ComponentType.SERVICE:
                return ServerTypeUrl.SERVICES;
            default:
                return ServerTypeUrl.RESOURCES;
        }
    }

    getFullComponent(uniqueId: string): Observable<ComponentGenericResponse> {
        return this.http.get<ComponentGenericResponse>(this.baseUrl + uniqueId)
        .map((res) => {
            return new ComponentGenericResponse().deserialize(res);
        });
    }

    getComponentMetadata(uniqueId: string, type: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(type, uniqueId, [COMPONENT_FIELDS.COMPONENT_METADATA]);
    }

    getComponentInstanceAttributesAndProperties(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_PROPERTIES, COMPONENT_FIELDS.COMPONENT_INSTANCES_ATTRIBUTES]);
    }

    getComponentInstanceProperties(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_PROPERTIES]);
    }

    getComponentAttributes(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_ATTRIBUTES]);
    }

    getComponentCompositionData(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_RELATION, COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_POLICIES, COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_GROUPS]);
    }

    getComponentResourcePropertiesData(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_POLICIES, COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_GROUPS]);
    }

    getComponentResourceAttributesData(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_GROUPS]);
    }

    getComponentResourceInstances(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES]);
    }

    getComponentInputs(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INPUTS]);
    }

    getComponentInputsWithProperties(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INPUTS, COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_INSTANCES_PROPERTIES, COMPONENT_FIELDS.COMPONENT_PROPERTIES]);
    }

    getComponentDeploymentArtifacts(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_DEPLOYMENT_ARTIFACTS]);
    }

    getComponentInformationalArtifacts(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INFORMATIONAL_ARTIFACTS]);
    }

    getComponentInformationalArtifactsAndInstances(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INFORMATIONAL_ARTIFACTS, COMPONENT_FIELDS.COMPONENT_INSTANCES]);
    }

    getComponentToscaArtifacts(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_TOSCA_ARTIFACTS]);
    }

    getComponentProperties(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_PROPERTIES]);
    }

    getInterfaceOperations(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INTERFACE_OPERATIONS]);
    }

    getInterfaceOperation(component: Component, operation: OperationModel): Observable<OperationModel> {
        return this.http.get<OperationModel>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/interfaceOperations/' + operation.uniqueId);
    }

    // tslint:disable-next-line:member-ordering
    createInterfaceOperation(component: Component, operation: OperationModel): Observable<OperationModel> {
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
        return this.http.post<any>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/interfaceOperations', operationList)
        .map((res: any) => {
            const interf: InterfaceModel = _.find(res.interfaces, interf => interf.type === operation.interfaceType);
            const newOperation: OperationModel = _.find(interf.operations, op => op.name === operation.name);
            return new OperationModel({
                ...newOperation,
                interfaceType: interf.type,
                interfaceId: interf.uniqueId,
                artifactFileName: operation.artifactFileName
            });
        });
    }

    // tslint:disable-next-line:member-ordering
    updateInterfaceOperation(component: Component, operation: OperationModel): Observable<OperationModel> {
        const operationList = {
            interfaces: {
                [operation.interfaceType]: {
                    type: operation.interfaceType,
                    operations: {
                        [operation.name]: new BEOperationModel(operation)
                    }
                }
            }
        };
        return this.http.put<any>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/interfaceOperations', operationList)
        .map((res: any) => {
            const interf: InterfaceModel = _.find(res.interfaces, interf => interf.type === operation.interfaceType);
            const newOperation: OperationModel = _.find(interf.operations, op => op.name === operation.name);
            return new OperationModel({
                ...newOperation,
                interfaceType: interf.type,
                interfaceId: interf.uniqueId,
                artifactFileName: operation.artifactFileName
            });
        });
    }

    updateComponentInterfaceOperation(componentMetaDataId: string,
                                      operation: InterfaceOperationModel): Observable<InterfaceOperationModel> {
        const operationList = {
            interfaces: {
                [operation.interfaceType]: {
                    type: operation.interfaceType,
                    operations: {
                        [operation.name]: new BEInterfaceOperationModel(operation)
                    }
                }
            }
        };
        return this.http.put<any>(this.baseUrl + 'resources/' + componentMetaDataId + '/interfaceOperation', operationList)
        .map((res: any) => {
            const interf: InterfaceModel = _.find(res.interfaces, interf => interf.type === operation.interfaceType);
            const newOperation: OperationModel = _.find(interf.operations, op => op.name === operation.name);

            return new InterfaceOperationModel({
                ...newOperation,
                interfaceType: interf.type,
                interfaceId: interf.uniqueId,
            });
        });
    }

    createComponentInterfaceOperation(componentMetaDataId: string,
                                      componentMetaDataType: string,
                                      operation: InterfaceOperationModel): Observable<InterfaceOperationModel> {
        const operationList = {
            interfaces: {
                [operation.interfaceType]: {
                    type: operation.interfaceType,
                    operations: {
                        [operation.name]: new BEInterfaceOperationModel(operation)
                    }
                }
            }
        };
        console.log(operationList);
        console.log(this.baseUrl + componentMetaDataType + componentMetaDataId + '/resource/interfaceOperation')
        return this.http.post<any>(this.baseUrl + componentMetaDataType + componentMetaDataId + '/resource/interfaceOperation', operationList)
        .map((res: any) => {
            const interf: InterfaceModel = _.find(res.interfaces, interf => interf.type === operation.interfaceType);
            const newOperation: OperationModel = _.find(interf.operations, op => op.name === operation.name);

            return new InterfaceOperationModel({
                ...newOperation,
                interfaceType: interf.type,
                interfaceId: interf.uniqueId,
            });
        });
    }

    deleteInterfaceOperation(component: Component, operation: OperationModel): Observable<OperationModel> {
        return this.http.delete<OperationModel>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/interfaces/' + operation.interfaceId + '/operations/' + operation.uniqueId);
    }

    getInterfaceTypes(component: Component): Observable<{ [id: string]: Array<string> }> {
        return this.getInterfaceTypesByModel(component && component.model);
    }

    getInterfaceTypesByModel(model: string): Observable<{ [id: string]: Array<string> }> {
        return this.http.get<any>(this.baseUrl + 'interfaceLifecycleTypes' + ((model) ? '?model=' + model : ''))
        .map((res: any) => {
            const interfaceMap = {};
            if (!res) {
                return interfaceMap;
            }
            Object.keys(res).forEach(interfaceName => {
                const interface1 = res[interfaceName];
                if (!interface1.toscaPresentation.operations) {
                    return;
                }
                interfaceMap[interface1.toscaPresentation.type] = Object.keys(interface1.toscaPresentation.operations);
            });
            return interfaceMap;
        });
    }

    uploadInterfaceOperationArtifact(component: Component, newOperation: OperationModel, oldOperation: OperationModel) {
        const payload = {
            artifactType: "WORKFLOW",
            artifactName: oldOperation.artifactFileName,
            description: "Workflow Artifact Description",
            payloadData: oldOperation.artifactData
        };

        const headers = new HttpHeaders().append('Content-MD5', HttpHelperService.getHeaderMd5(payload));

        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uuid + '/interfaces/' + newOperation.interfaceId + '/operations/' + newOperation.uniqueId + '/artifacts/' + newOperation.implementation.artifactUUID,
            payload, {headers: headers}
        ).map((res: any) => {
            const fileName = res.artifactDisplayName || res.artifactName;
            newOperation.artifactFileName = fileName;
            return res;
        });
    }

    getCapabilitiesAndRequirements(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_REQUIREMENTS, COMPONENT_FIELDS.COMPONENT_CAPABILITIES]);
    }

    getDeploymentGraphData(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_RELATION, COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_GROUPS]);
    }

    createInput(component: Component, inputsToCreate: InstancePropertiesAPIMap, isSelf: boolean): Observable<any> {
        const inputs = isSelf ? {serviceProperties: inputsToCreate.componentInstanceProperties} : inputsToCreate;
        return this.http.post<any>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/create/inputs', inputs);
    }

    createListInput(component: Component, input: any, isSelf: boolean): Observable<any> {
        let inputs: any;
        if (isSelf) {
            // change componentInstanceProperties -> serviceProperties
            inputs = {
                componentInstInputsMap: {
                    serviceProperties: input.componentInstInputsMap.componentInstanceProperties
                },
                listInput: input.listInput
            };
        } else {
            inputs = input;
        }
        return this.http.post<any>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/create/listInput', inputs);
    }

    createPolicy(component: Component, policiesToCreate: InstancePropertiesAPIMap, isSelf: boolean): Observable<any> {
        const policiesList =
            isSelf ?
                {
                    'componentPropertiesToPolicies': {
                        ...policiesToCreate.componentInstanceProperties
                    }
                } :
                {
                    'componentInstancePropertiesToPolicies': {
                        ...policiesToCreate.componentInstanceProperties
                    }
                };
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/create/policies', policiesList);
    }

    deletePolicy(component: Component, policy: PolicyInstance) {
        return this.http.put<PolicyInstance>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/policies/' + policy.uniqueId + '/undeclare', policy);
    }

    restoreComponent(componentType: string, componentId: string) {
        return this.http.post(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/restore', {})
    }

    archiveComponent(componentType: string, componentId: string) {
        return this.http.post(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/archive', {})
    }

    deleteInput(component: Component, input: InputBEModel): Observable<InputBEModel> {

        return this.http.delete<InputBEModel>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/delete/' + input.uniqueId + '/input')
        .map((res) => {
            return new InputBEModel(res);
        })
    }

    deleteOutput(component: Component, output: OutputBEModel): Observable<OutputBEModel> {

        return this.http.delete<OutputBEModel>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/delete/' + output.uniqueId + '/output')
        .map((res) => {
            return new OutputBEModel(res);
        })
    }

    updateComponentInputs(component: Component, inputs: InputBEModel[]): Observable<InputBEModel[]> {

        return this.http.post<InputBEModel[]>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/update/inputs', inputs)
        .map((res) => {
            return res.map((input) => new InputBEModel(input));
        })
    }

    updateComponentOutputs(component: Component, outputs: OutputBEModel[]): Observable<OutputBEModel[]> {

        return this.http.post<OutputBEModel[]>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/update/outputs', outputs)
        .map((res) => {
            return res.map((output) => new OutputBEModel(output));
        })
    }

    filterComponentInstanceProperties(component: Component, filterData: FilterPropertiesAssignmentData): Observable<InstanceBePropertiesMap> {//instance-property-be-map
        let params: HttpParams = new HttpParams();
        filterData.selectedTypes.forEach((type: string) => {
            params = params.append('resourceType', type);
        });

        return this.http.get<InstanceBePropertiesMap>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/filteredproperties/' + filterData.propertyName, {params: params});
    }

    filterComponentInstanceAttributes(component: Component, filterData: FilterPropertiesAssignmentData): Observable<InstanceBePropertiesMap> {//instance-property-be-map
        let params: HttpParams = new HttpParams();
        filterData.selectedTypes.forEach((type: string) => {
            params = params.append('resourceType', type);
        });

        return this.http.get<InstanceBePropertiesMap>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/filteredproperties/' + filterData.propertyName, {params: params});
    }

    deleteComponent(componentType: string, componentId: string) {
        return this.http.delete(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + "?action=DELETE", {});
    }

    createServiceProperty(component: Component, propertyModel: PropertyBEModel): Observable<PropertyBEModel> {
        let serverObject = {};
        serverObject[propertyModel.name] = propertyModel;
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/properties', serverObject)
        .map((res: PropertyBEModel) => {
            const property: PropertyBEModel = new PropertyBEModel(res);
            return property;
        })
    }

    getServiceProperties(component: Component): Observable<PropertyBEModel[]> {
        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/properties')
        .map((res: PropertyBEModel[]) => {
            if (!res) {
                return new Array<PropertyBEModel>();
            }
            return CommonUtils.initBeProperties(res);
        });
    }

    updateServiceProperties(component: Component, properties: PropertyBEModel[]) {
        return this.http.put<PropertyBEModel[]>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/properties', properties)
        .map((res) => {
            const resJson = res;
            return _.map(resJson,
                (resValue: PropertyBEModel) => new PropertyBEModel(resValue));
        });
    }

    deleteServiceProperty(component: Component, property: PropertyBEModel): Observable<string> {
        return this.http.delete(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/properties/' + property.uniqueId)
        .map((res: Response) => {
            return property.uniqueId;
        })
    }

    getDependencies(componentType: string, componentId: string): Observable<IDependenciesServerResponse[]> {
        return this.http.get<IDependenciesServerResponse[]>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/dependencies');
    }

    automatedUpgrade(componentType: string, componentId: string, componentsIdsToUpgrade: IAutomatedUpgradeRequestObj[]): Observable<AutomatedUpgradeGenericResponse> {
        return this.http.post<AutomatedUpgradeGenericResponse>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/automatedupgrade', componentsIdsToUpgrade);
    }

    updateMultipleComponentInstances(componentId: string, instances: ComponentInstance[]): Observable<ComponentInstance[]> {
        return this.http.post<ComponentInstance[]>(this.baseUrl + componentId + '/resourceInstance/multipleComponentInstance', instances);
    }

    getServiceFilterConstraints(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [SERVICE_FIELDS.NODE_FILTER]);
    }

    createServiceFilterConstraints(component: Component, componentInstance: ComponentInstance, constraint: ConstraintObject): Observable<any> {
        return this.http.post<any>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstances/' + componentInstance.uniqueId + '/nodeFilter', constraint);
    }

    updateServiceFilterConstraints(component: Component, componentInstance: ComponentInstance, constraints: ConstraintObject[]): Observable<any> {
        return this.http.put<any>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstances/' + componentInstance.uniqueId + '/nodeFilter/', constraints);
    }

    deleteServiceFilterConstraints(component: Component, componentInstance: ComponentInstance, constraintIndex: number) {
        return this.http.delete<any>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstances/' + componentInstance.uniqueId + '/nodeFilter/' + constraintIndex);
    }

    protected analyzeComponentDataResponse(res: Response): ComponentGenericResponse {
        return new ComponentGenericResponse().deserialize(res);
    }
}
