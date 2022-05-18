/**
 * Created by ob0695 on 6/26/2018.
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

import * as _ from "lodash";
import {Injectable, Inject} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import {
    Component,
    InputBEModel,
    InstancePropertiesAPIMap,
    FilterPropertiesAssignmentData,
    ArtifactModel,
    PropertyModel,
    IFileDownload,
    AttributeModel,
    Capability, Requirement, BEOperationModel, InterfaceModel
} from "app/models";
import {ArtifactGroupType, COMPONENT_FIELDS} from "app/utils";
import {ComponentGenericResponse} from "../responses/component-generic-response";
import {InstanceBePropertiesMap} from "../../../models/properties-inputs/property-fe-map";
import {API_QUERY_PARAMS} from "app/utils";
import {ComponentType, ServerTypeUrl, SERVICE_FIELDS} from "../../../utils/constants";
import {SdcConfigToken, ISdcConfig} from "../../config/sdc-config.config";
import {IDependenciesServerResponse} from "../responses/dependencies-server-response";
import {AutomatedUpgradeGenericResponse} from "../responses/automated-upgrade-response";
import {IAutomatedUpgradeRequestObj} from "../../pages/automated-upgrade/automated-upgrade.service";
import {ComponentInstance} from "../../../models/componentsInstances/componentInstance";
import {CommonUtils} from "../../../utils/common-utils";
import {RelationshipModel} from "../../../models/graph/relationship";
import {ServiceGenericResponse} from "../responses/service-generic-response";
import { HttpClient, HttpParams, HttpHeaders } from "@angular/common/http";
import { HttpHelperService } from "../http-hepler.service";
import {
    Component as TopologyTemplate,
    FullComponentInstance,
    Service,
    OperationModel,
} from 'app/models';
import { ConsumptionInput } from "../../components/logic/service-consumption/service-consumption.component";
import { ConstraintObject } from "../../components/logic/service-dependencies/service-dependencies.component";
import { ComponentMetadata } from "../../../models/component-metadata";
import { PolicyInstance } from "../../../models/graph/zones/policy-instance";
import { PropertyBEModel } from "../../../models/properties-inputs/property-be-model";
import {map} from "rxjs/operators";
import {CapabilitiesConstraintObject} from "../../components/logic/capabilities-constraint/capabilities-constraint.component";
import {
    BEInterfaceOperationModel,
    ComponentInterfaceDefinitionModel,
    InterfaceOperationModel
} from "../../../models/interfaceOperation";
import {AttributeBEModel} from "../../../models/attributes-outputs/attribute-be-model";
import {InstanceAttributesAPIMap} from "../../../models/attributes-outputs/attribute-fe-map";

/* we need to use this service from now, we will remove component.service when we finish remove the angular1.
 The service is duplicated since we can not use downgrades service with NGXS*/

@Injectable()
export class TopologyTemplateService {

    protected baseUrl;

    constructor(protected http: HttpClient, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
    }

    getFullComponent(componentType: string, uniqueId: string): Observable<Component> {
        return this.http.get<Component>(this.baseUrl + this.getServerTypeUrl(componentType) + uniqueId);
    }

    getComponentMetadata(uniqueId: string, type: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(type, uniqueId, [COMPONENT_FIELDS.COMPONENT_METADATA]);
    }

    getComponentInstanceAttributesAndProperties(uniqueId: string, type: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(type, uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_PROPERTIES, COMPONENT_FIELDS.COMPONENT_INSTANCES_ATTRIBUTES]);
    }

    getComponentInstanceAttributesAndPropertiesAndInputs(uniqueId: string, type: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(type, uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_PROPERTIES, COMPONENT_FIELDS.COMPONENT_INSTANCES_ATTRIBUTES, COMPONENT_FIELDS.COMPONENT_INPUTS]);
    }

    async getComponentAttributes(componentType: string, componentId: string): Promise<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_ATTRIBUTES]).toPromise();
    }

    getComponentCompositionData(componentUniqueId: string, componentType: string): Observable<ComponentGenericResponse> {
        const params: string[] = [COMPONENT_FIELDS.COMPONENT_INSTANCES_RELATION, COMPONENT_FIELDS.COMPONENT_INSTANCES,
            COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_POLICIES, COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_GROUPS];
        if (componentType === ComponentType.SERVICE) {
            params.push(COMPONENT_FIELDS.FORWARDING_PATHS);
        }
        return this.getComponentDataByFieldsName(componentType, componentUniqueId, params);
    }

    getComponentResourcePropertiesData(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId,
            [COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_POLICIES, COMPONENT_FIELDS.COMPONENT_NON_EXCLUDED_GROUPS]);
    }

    getComponentInstances(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_INSTANCES]);
    }

    getComponentInputs(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INPUTS]);
    }

    getComponentInputsValues(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_INPUTS]);
    }

    getComponentInputsWithProperties(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId,
            [COMPONENT_FIELDS.COMPONENT_INPUTS, COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_INSTANCES_PROPERTIES, COMPONENT_FIELDS.COMPONENT_PROPERTIES]);
    }

    getComponentOutputsWithAttributes(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId,
            [COMPONENT_FIELDS.COMPONENT_OUTPUTS, COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_INSTANCES_ATTRIBUTES, COMPONENT_FIELDS.COMPONENT_ATTRIBUTES,COMPONENT_FIELDS.COMPONENT_INSTANCES_OUTPUTS]);
    }

    getComponentDeploymentArtifacts(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_DEPLOYMENT_ARTIFACTS]);
    }

    getComponentInformationalArtifacts(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INFORMATIONAL_ARTIFACTS]);
    }

    getComponentInterfaceOperations(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_INTERFACE_OPERATIONS]);
    }

    getComponentInformationalArtifactsAndInstances(component: Component): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INFORMATIONAL_ARTIFACTS, COMPONENT_FIELDS.COMPONENT_INSTANCES]);
    }

    getComponentToscaArtifacts(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_TOSCA_ARTIFACTS]);
    }

    getComponentProperties(component: Component): Observable<ComponentGenericResponse> {
        return this.findAllComponentProperties(component.componentType, component.uniqueId);
    }

    findAllComponentProperties(componentType: string, componentUniqueId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentUniqueId, [COMPONENT_FIELDS.COMPONENT_PROPERTIES]);
    }

    getCapabilitiesAndRequirements(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_REQUIREMENTS, COMPONENT_FIELDS.COMPONENT_CAPABILITIES]);
    }

    getRequirementsAndCapabilitiesWithProperties(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId,
            [COMPONENT_FIELDS.COMPONENT_REQUIREMENTS, COMPONENT_FIELDS.COMPONENT_CAPABILITIES, COMPONENT_FIELDS.COMPONENT_CAPABILITIES_PROPERTIES]);
    }

    getDeploymentGraphData(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_RELATION, COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_GROUPS]);
    }

    createInput(component: Component, inputsToCreate: InstancePropertiesAPIMap, isSelf: boolean): Observable<any> {
        const inputs = isSelf ? { serviceProperties: inputsToCreate.componentInstanceProperties } : inputsToCreate;
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/create/inputs', inputs);
    }

    createOutput(component: Component, outputsToCreate: InstanceAttributesAPIMap, isSelf: boolean): Observable<any> {
        const outputs = isSelf ? { serviceProperties: outputsToCreate.componentInstanceAttributes } : outputsToCreate;
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/create/outputs', outputs);
    }

    restoreComponent(componentType: string, componentId: string) {
        return this.http.post(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/restore', {});
    }

    archiveComponent(componentType: string, componentId: string) {
        return this.http.post(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/archive', {});
    }

    deleteInput(component: Component, input: InputBEModel): Observable<InputBEModel> {
        return this.http.delete<InputBEModel>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/delete/' + input.uniqueId + '/input')
            .map((res) => {
                return new InputBEModel(res);
            });
    }

    updateComponentInputs(component: Component, inputs: InputBEModel[]): Observable<InputBEModel[]> {
        return this.http.post<InputBEModel[]>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/update/inputs', inputs)
            .map((res) => {
                return res.map((input) => new InputBEModel(input));
            });
    }

    filterComponentInstanceProperties(component: Component, filterData: FilterPropertiesAssignmentData): Observable<InstanceBePropertiesMap> {// instance-property-be-map
        let params: HttpParams = new HttpParams();
        _.forEach(filterData.selectedTypes, (type: string) => {
            params = params.append('resourceType', type);
        });

        // tslint:disable-next-line:object-literal-shorthand
        return this.http.get<InstanceBePropertiesMap>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/filteredproperties/' + filterData.propertyName, {params: params});
    }

     createServiceProperty(componentId: string, propertyModel: PropertyBEModel): Observable<PropertyBEModel> {
        const serverObject = {};
        serverObject[propertyModel.name] = propertyModel;
        return this.http.post<PropertyBEModel>(this.baseUrl + 'services/' + componentId + '/properties', serverObject)
            .map((res) => {
                const property: PropertyBEModel = new PropertyBEModel(res);
                return property;
            });
    }

    createServiceAttribute(componentId: string, attributeModel: AttributeBEModel): Observable<AttributeBEModel> {
        const serverObject = {};
        serverObject[attributeModel.name] = attributeModel;
        return this.http.post<AttributeBEModel>(this.baseUrl + 'services/' + componentId + '/attributes', serverObject)
            .map((res) => {
                const attribute: AttributeBEModel = new AttributeBEModel(res);
                return attribute;
            });
    }

    getServiceProperties(componentId: string): Observable<PropertyBEModel[]> {
        return this.http.get<any>(this.baseUrl + 'services/' + componentId + '/properties')
            .map((res) => {
                if (!res) {
                    return new Array<PropertyBEModel>();
                }
                return CommonUtils.initBeProperties(res);
            });
    }

    getServiceAttributes(componentId: string): Observable<AttributeBEModel[]> {
        return this.http.get<any>(this.baseUrl + 'services/' + componentId + '/attributes')
            .map((res) => {
                if (!res) {
                    return new Array<AttributeBEModel>();
                }
                return CommonUtils.initAttributes(res);
            });
    }

    updateServiceProperties(componentId: string, properties: PropertyBEModel[]) {
        return this.http.put<any>( this.baseUrl + 'services/' + componentId + '/properties', properties)
            .map((res) => {
                const resJson = res;
                return _.map(resJson,
                    (resValue: PropertyBEModel) => new PropertyBEModel(resValue));
            });
    }

    updateServiceAttributes(componentId: string, attributes: AttributeBEModel[]) {
        return this.http.put<any>( this.baseUrl + 'services/' + componentId + '/attributes', attributes)
            .map((res) => {
                const resJson = res;
                return _.map(resJson,
                    (resValue: AttributeBEModel) => new AttributeBEModel(resValue));
            });
    }

    deleteServiceProperty(componentId: string, property: PropertyBEModel): Observable<string> {
        return this.http.delete(this.baseUrl + 'services/' + componentId + '/properties/' + property.uniqueId )
            .map((res: Response) => {
                return property.uniqueId;
            });
    }

    createServiceInput(componentId: string, inputModel: InputBEModel): Observable<InputBEModel> {
        const serverObject = {};
        serverObject[inputModel.name] = inputModel;
        return this.http.post<InputBEModel>(this.baseUrl + 'services/' + componentId + '/create/input', serverObject)
            .map((res) => {
                const input: InputBEModel = new InputBEModel(res);
                return input;
            });
    }

    deleteServiceAttribute(componentId: string, attribute: AttributeBEModel): Observable<string> {
        return this.http.delete(this.baseUrl + 'services/' + componentId + '/attributes/' + attribute.uniqueId )
            .map((res: Response) => {
                return attribute.uniqueId;
            });
    }

    getDependencies(componentType: string, componentId: string): Observable<IDependenciesServerResponse[]> {
        return this.http.get<IDependenciesServerResponse[]>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/dependencies');
    }

    automatedUpgrade(componentType: string, componentId: string, componentsIdsToUpgrade: IAutomatedUpgradeRequestObj[]): Observable<AutomatedUpgradeGenericResponse> {
        return this.http.post<AutomatedUpgradeGenericResponse>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/automatedupgrade', componentsIdsToUpgrade);
    }

    updateComponentInstance(componentMetaDataId: string, componentType: string, componentInstance:ComponentInstance): Observable<ComponentInstance> {
        return this.http.post<ComponentInstance>(this.baseUrl + this.getServerTypeUrl(componentType) + componentMetaDataId + '/resourceInstance/' + componentInstance.uniqueId, componentInstance);
    }

    updateMultipleComponentInstances(componentId: string, componentType: string, instances: ComponentInstance[]): Observable<ComponentInstance[]> {
        return this.http.post<ComponentInstance[]>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/multipleComponentInstance', instances)
            .map((res) => {
                return CommonUtils.initComponentInstances(res);
            });
    }

    createRelation(componentId: string, componentType: string, link: RelationshipModel): Observable<RelationshipModel> {
        return this.http.post<RelationshipModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/associate', link)
            .map((res) => {
                return new RelationshipModel(res);
            });
    }

    deleteRelation(componentId: string, componentType: string, link: RelationshipModel): Observable<RelationshipModel> {
        return this.http.put<RelationshipModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/dissociate', link)
            .map((res) => {
                return new RelationshipModel(res);
            });
    }

    createComponentInstance(componentType: string, componentId: string, componentInstance: ComponentInstance): Observable<ComponentInstance> {
        return this.http.post<ComponentInstance>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance', componentInstance)
            .map((res) => {
                return new ComponentInstance(res);
            });
    }

    deleteComponentInstance(componentType: string, componentId: string, componentInstanceId: string): Observable<ComponentInstance> {
        return this.http.delete<ComponentInstance>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/' + componentInstanceId)
            .map((res) => {
                return new ComponentInstance(res);
            });
    }

    fetchRelation(componentType: string, componentId: string, linkId: string): Observable<RelationshipModel> {
        return this.http.get<RelationshipModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/relationId/' + linkId)
            .map((res) => {
                return new RelationshipModel(res);
            });
    }

    addOrUpdateArtifact = (componentType: string, componentId: string, artifact: ArtifactModel): Observable<ArtifactModel> => {
        let headerObj: HttpHeaders = new HttpHeaders();
        if (artifact.payloadData) {
            headerObj = headerObj.append('Content-MD5', HttpHelperService.getHeaderMd5(artifact));
        }

        let artifactID: string = '';
        if (artifact.uniqueId) {
            artifactID = '/' + artifact.uniqueId;
        }
        return this.http.post<ArtifactModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/artifacts' + artifactID, JSON.stringify(artifact), {headers: headerObj}).map(
            (res) => new ArtifactModel(res)
        );
    }

    deleteArtifact = (componentId: string, componentType: string, artifactId: string, artifactLabel: string): Observable<ArtifactModel> => {
        return this.http.delete<ArtifactModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/artifacts/' + artifactId + '?operation=' + artifactLabel)
            .map((res) => new ArtifactModel(res));
    }

    downloadArtifact = (componentType: string, componentId: string, artifactId: string): Observable<IFileDownload> => {
        return this.http.get<IFileDownload>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/artifacts/' + artifactId);
    }

    // ------------------------------------------------ Properties API --------------------------------------------------//
    addProperty = (componentType: string, componentId: string, property: PropertyModel):Observable<PropertyModel> => {
        return this.http.post<PropertyModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/properties', property.convertToServerObject())
        .map((response) => {
            return new PropertyModel(response);
        });
    }

    updateProperty = (componentType: string, componentId: string, property: PropertyModel): Observable<PropertyModel> => {
        var propertiesList:PropertyBEModel[]  = [property];
        return this.http.put<PropertyModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/properties', propertiesList)
        .map((response) => {
            return new PropertyModel(response[Object.keys(response)[0]]);
        });
    }

    deleteProperty = (componentType: string, componentId: string, propertyId: string): Observable<PropertyModel> => {
        return this.http.delete<PropertyModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/properties/' + propertyId);
    }

    // ------------------------------------------------ Attributes API --------------------------------------------------//
    addAttribute = (componentType: string, componentId: string, attribute: AttributeModel): Observable<AttributeModel> => {
        return this.http.post<AttributeModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/attributes', attribute.convertToServerObject())
            .map((response) => {
                return new AttributeModel(response);
            });
    }

    updateAttribute = (componentType: string, componentId: string, attribute: AttributeModel): Observable<AttributeModel> => {
        const payload = attribute.convertToServerObject();

        return this.http.put<AttributeModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/attributes/' + attribute.uniqueId, payload)
            .map((response) => {
                return new AttributeModel(response);
            });
    }

    // Async Methods
    addAttributeAsync = async (componentType: string, componentId: string, attribute: AttributeModel): Promise<AttributeModel> => {
        return this.addAttribute(componentType, componentId, attribute).toPromise();
    }

    updateAttributeAsync = async (componentType: string, componentId: string, attribute: AttributeModel): Promise<AttributeModel> => {
        return this.updateAttribute(componentType, componentId, attribute).toPromise();
    }

    deleteAttributeAsync = async (componentType: string, componentId: string, attribute: AttributeModel): Promise<any> => {
        return this.http.delete<any>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/attributes/' + attribute.uniqueId, {}).toPromise();
    }

    getArtifactsByType(componentType: string, componentId: string, artifactsType: ArtifactGroupType) {
        return this.getComponentDataByFieldsName(componentType, componentId, [this.convertArtifactTypeToUrl(artifactsType)]);
    }

    getServiceConsumptionData(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [
            // COMPONENT_FIELDS.COMPONENT_INSTANCES_INTERFACES,
            COMPONENT_FIELDS.COMPONENT_INSTANCES_PROPERTIES,
            // COMPONENT_FIELDS.COMPONENT_INSTANCES_INPUTS,
            COMPONENT_FIELDS.COMPONENT_INPUTS,
            COMPONENT_FIELDS.COMPONENT_INSTANCES,
            COMPONENT_FIELDS.COMPONENT_CAPABILITIES
        ]);
    }

    getServiceConsumptionInputs(componentMetaDataId: string, serviceInstanceId: string, interfaceId: string, operation: OperationModel): Observable<ConsumptionInput[]> {
        return this.http.get<ConsumptionInput[]>
        (this.baseUrl + 'services/' + componentMetaDataId + '/consumption/' + serviceInstanceId + '/interfaces/' + interfaceId + '/operations/' + operation.uniqueId + '/inputs');
    }

    createOrUpdateServiceConsumptionInputs(componentMetaDataId: string, serviceInstanceId: string, consumptionInputsList: Array<{[id: string]: ConsumptionInput[]}>): Observable<any> {
        return this.http.post(this.baseUrl + 'services/' + componentMetaDataId + '/consumption/' + serviceInstanceId, consumptionInputsList);
    }

    getServiceFilterConstraints(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [SERVICE_FIELDS.NODE_FILTER]);
    }

    getSubstitutionFilterConstraints(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [SERVICE_FIELDS.SUBSTITUTION_FILTER]);
    }

    getComponentInstanceProperties(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_PROPERTIES]);
    }

    getComponentInstanceCapabilityProperties(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId,
            [COMPONENT_FIELDS.COMPONENT_CAPABILITIES, COMPONENT_FIELDS.COMPONENT_CAPABILITIES_PROPERTIES]);
    }

    createServiceFilterConstraints(componentMetaDataId: string, componentInstanceId: string, constraint: ConstraintObject, componentType: string, constraintType: string): Observable<any> {
        return this.http.post<any>(this.baseUrl + this.getServerTypeUrl(componentType) + componentMetaDataId + '/componentInstance/' + componentInstanceId + '/' + constraintType + '/nodeFilter', constraint);
    }

    createServiceFilterCapabilitiesConstraints(componentMetaDataId: string, componentInstanceId: string, constraint: CapabilitiesConstraintObject, componentType: string, constraintType: string): Observable<any> {
        return this.http.post<any>(this.baseUrl + this.getServerTypeUrl(componentType) + componentMetaDataId + '/componentInstance/' + componentInstanceId + '/' + constraintType + '/nodeFilter', constraint);
    }

    updateServiceFilterConstraints(componentMetaDataId: string, componentInstanceId: string, constraints: ConstraintObject, componentType: string, constraintType: string, constraintIndex: number):Observable<any>{
        return this.http.put<any>(this.baseUrl + this.getServerTypeUrl(componentType) + componentMetaDataId + '/componentInstance/' + componentInstanceId + '/' + constraintType + '/' + constraintIndex + '/nodeFilter', constraints)
    }

    updateServiceFilterCapabilitiesConstraint(componentMetaDataId: string, componentInstanceId: string, constraints: CapabilitiesConstraintObject, componentType: string, constraintType: string, constraintIndex: number):Observable<any>{
        return this.http.put<any>(this.baseUrl + this.getServerTypeUrl(componentType) + componentMetaDataId + '/componentInstance/' + componentInstanceId + '/' + constraintType + '/' + constraintIndex + '/nodeFilter', constraints)
    }

    deleteServiceFilterConstraints(componentMetaDataId: string, componentInstanceId: string, constraintIndex: number, componentType: string, constraintType: string): Observable<any>{
        return this.http.delete<any>(this.baseUrl + this.getServerTypeUrl(componentType) + componentMetaDataId + '/componentInstance/' + componentInstanceId + '/' + constraintType + '/' + constraintIndex + '/nodeFilter')
    }

    getComponentPropertiesAndInputsForSubstitutionFilter(componentType: string, componentId: string): Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_PROPERTIES, COMPONENT_FIELDS.COMPONENT_INPUTS]);
    }

    createSubstitutionFilterConstraints(componentMetaDataId: string, constraint: ConstraintObject, componentType: string, constraintType: string): Observable<any> {
        return this.http.post<any>(this.baseUrl + this.getServerTypeUrl(componentType) + componentMetaDataId + '/substitutionFilter/' + constraintType, constraint);
    }

    updateSubstitutionFilterConstraints(componentMetaDataId: string, constraint: ConstraintObject[], componentType: string, constraintType: string): Observable<any>{
        return this.http.put<any>(this.baseUrl + this.getServerTypeUrl(componentType) + componentMetaDataId + '/substitutionFilter/' + constraintType, constraint);
    }

    deleteSubstitutionFilterConstraints(componentMetaDataId: string, constraintIndex: number, componentType: string, constraintType: string): Observable<any>{
        return this.http.delete<any>(this.baseUrl + this.getServerTypeUrl(componentType) + componentMetaDataId + '/substitutionFilter/' + constraintType + "/" + constraintIndex)
    }

    deletePolicy(component: Component, policy: PolicyInstance): Observable<PolicyInstance> {
        return this.http.put<PolicyInstance>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/policies/' + policy.uniqueId + '/undeclare', policy)
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
                // tslint:disable-next-line:object-literal-key-quotes
                {'componentPropertiesToPolicies': {
                        ...policiesToCreate.componentInstanceProperties
                    }
                } :
                // tslint:disable-next-line:object-literal-key-quotes
                {'componentInstancePropertiesToPolicies': {
                        ...policiesToCreate.componentInstanceProperties
                    }
                };
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/create/policies', policiesList);
    }

    protected getComponentDataByFieldsName(componentType: string, componentId: string, fields: string[]): Observable<ComponentGenericResponse> {
        let params: HttpParams = new HttpParams();
        _.forEach(fields, (field: string): void => {
            params = params.append(API_QUERY_PARAMS.INCLUDE, field);
        });
        // tslint:disable-next-line:object-literal-shorthand
        return this.http.get<ComponentGenericResponse>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/filteredDataByParams', {params: params})
            .map((res) => {
                return componentType === ComponentType.SERVICE ? new ServiceGenericResponse().deserialize(res) :
                        new ComponentGenericResponse().deserialize(res);
            });
    }

    private getServerTypeUrl = (componentType: string): string => {
        switch (componentType) {
            case ComponentType.SERVICE:
            case ComponentType.SERVICE_PROXY:
            case ComponentType.SERVICE_SUBSTITUTION:
                return ServerTypeUrl.SERVICES;
            default:
                return ServerTypeUrl.RESOURCES;
        }
    }

    private convertArtifactTypeToUrl = (artifactType: ArtifactGroupType): string => {
        switch (artifactType) {
            case ArtifactGroupType.TOSCA:
                return COMPONENT_FIELDS.COMPONENT_TOSCA_ARTIFACTS;
            case ArtifactGroupType.INFORMATION:
                return COMPONENT_FIELDS.COMPONENT_INFORMATIONAL_ARTIFACTS;
            case ArtifactGroupType.DEPLOYMENT:
                return COMPONENT_FIELDS.COMPONENT_DEPLOYMENT_ARTIFACTS;
            case ArtifactGroupType.SERVICE_API:
                return COMPONENT_FIELDS.SERVICE_API_ARTIFACT;
        }
    }

    // createCapability(component: Component, capabilityData: Capability): Observable<Capability[]> {
    createCapability(type: string, uniqueId: string, capabilityData: Capability): Observable<Capability[]> {
        let capBEObj = {
            'capabilities': {
                [capabilityData.type]: [capabilityData]
            }
        };
        return this.http.post<any>(this.baseUrl + type + uniqueId + '/capabilities', capBEObj);
    }

    updateCapability(type: string, uniqueId: string, capabilityData: Capability): Observable<Capability[]> {
        let capBEObj = {
            'capabilities': {
                [capabilityData.type]: [capabilityData]
            }
        };
        return this.http.put<any>(this.baseUrl + type + uniqueId + '/capabilities', capBEObj);
    }

    deleteCapability(component: Component, capId: string): Observable<Capability> {
        return this.http.delete<Capability>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/capabilities/' + capId);
    }

    createRequirement(type: string, uniqueId: string, requirementData: Requirement): Observable<any> {
        let reqBEObj = {
            'requirements': {
                [requirementData.capability]: [requirementData]
            }
        };
        return this.http.post(this.baseUrl + type + uniqueId + '/requirements', reqBEObj);
    }

    updateRequirement(type: string, uniqueId: string, requirementData: Requirement): Observable<any> {
        let reqBEObj = {
            'requirements': {
                [requirementData.capability]: [requirementData]
            }
        };
        return this.http.put(this.baseUrl + type + uniqueId + '/requirements', reqBEObj);
    }

    deleteRequirement(component: Component, reqId: string): Observable<Requirement> {
        return this.http.delete<Requirement>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/requirements/' + reqId);
    }

    getDirectiveList(): Observable<string[]> {
        return this.http.get<ListDirectiveResponse>(this.baseUrl + "directives")
        .pipe(map(response => response.directives));
    }

    updateComponentInstanceInterfaceOperation(componentMetaDataId: string,
                                              componentMetaDataType: string,
                                              componentInstanceId: string,
                                              operation: InterfaceOperationModel): Observable<ComponentInstance> {
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
        return this.http.put<ComponentInstance>(this.baseUrl + this
        .getServerTypeUrl(componentMetaDataType) + componentMetaDataId + '/componentInstance/' + componentInstanceId + '/interfaceOperation', operationList);
    }

}
