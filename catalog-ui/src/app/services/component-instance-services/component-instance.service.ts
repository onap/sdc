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

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { ArtifactGroupModel, ArtifactModel } from 'app/models/artifacts';
import { AttributeModel } from 'app/models/attributes';
import { Capability } from 'app/models/capability';
import { ComponentInstance } from 'app/models/components-instances/component-instance';
import { Component } from 'app/models/components/component';
import { IFileDownload } from 'app/models/file-download';
import { PropertyModel } from 'app/models/properties';
import { PropertyBEModel } from 'app/models/properties-inputs/property-be-model';
import { Requirement } from 'app/models/requirement';
import { CommonUtils } from 'app/utils/common-utils';
import { ComponentInstanceFactory } from 'app/utils/component-instance-factory';
import { ComponentType, ServerTypeUrl } from 'app/utils/constants';
import 'rxjs/add/operator/map';
import { Observable } from 'rxjs/Observable';
import { ISdcConfig, SdcConfigToken } from '../../config/sdc-config.config';
import { AttributeBEModel } from '../../models/attributes-outputs/attribute-be-model';
import { OutputBEModel } from '../../models/attributes-outputs/output-be-model';
import { InputBEModel } from '../../models/properties-inputs/input-be-model';
import { HttpHelperService } from '../http-helper.service';

@Injectable()
export class ComponentInstanceService {

    protected baseUrl;

    constructor(private http: HttpClient, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
    }
    getComponentInstanceProperties(component: Component, componentInstanceId: string): Observable<PropertyBEModel[]> {
        return this.http.get<PropertyBEModel[]>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstanceId + '/properties')
            .map((res) => {
                return CommonUtils.initBeProperties(res);
        });
    }

    getComponentInstanceAttributes(component: Component, componentInstanceId: string): Observable<AttributeBEModel[]> {
        return this.http.get<AttributeBEModel[]>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstanceId + '/attributes')
            .map((res) => {
                return CommonUtils.initBeAttributes(res);
        });
    }

    getComponentInstanceInputs(component: Component, componentInstance: ComponentInstance): Observable<PropertyBEModel[]> {
        return this.getComponentInstanceInputsByIdAndType(component.uniqueId, component.componentType, componentInstance);
    }

    getComponentInstanceInputsByIdAndType(componentId: string, componentType: string, componentInstance: ComponentInstance): Observable<PropertyBEModel[]> {
        return this.http.get<InputBEModel[]>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/componentInstances/' + componentInstance.uniqueId + '/' + componentInstance.componentUid + '/inputs')
            .map((res) => {
                return CommonUtils.initInputs(res);
            });
    }

    getComponentInstanceOutputs(component: Component, componentInstance: ComponentInstance): Observable<AttributeBEModel[]> {
        return this.http.get<OutputBEModel[]>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstance.uniqueId + '/' + componentInstance.componentUid + '/outputs')
            .map((res) => {
                return CommonUtils.initOutputs(res);
            });
    }

    getComponentInstanceArtifactsByGroupType = (componentType: string, componentId: string, componentInstanceId: string, artifactGroupType: string): Observable<ArtifactGroupModel> => {

        return this.http.get<ArtifactGroupModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstances/' + componentInstanceId + '/artifactsByType/' + artifactGroupType)
            .map((res) => {
                return new ArtifactGroupModel(res);
            });
    }

    getArtifactByGroupType = (componentType: string, componentId: string, artifactGroupType: string): Observable<ArtifactGroupModel> => {

        return this.http.get<ArtifactGroupModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/artifactsByType/' + artifactGroupType)
            .map((response) => new ArtifactGroupModel(response));
    }

    changeResourceInstanceVersion = (componentType: string, componentId: string, componentInstanceId: string, componentUid: string): Observable<ComponentInstance> => {
        return this.http.post<ComponentInstance>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/' + componentInstanceId + '/changeVersion', {componentUid: componentUid})
        .map((res) => {
            return ComponentInstanceFactory.createComponentInstance(res);
        });
    }

    updateComponentInstance = (componentType: string, componentId: string, componentInstance: ComponentInstance): Observable<ComponentInstance> => {
        return this.http.post<ComponentInstance>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/' + componentInstance.uniqueId, componentInstance.toJSON())
            .map((response) => {
                return ComponentInstanceFactory.createComponentInstance(response);
            });
    }

    updateInstanceProperties(componentType: string, componentId: string, componentInstanceId: string, properties: PropertyBEModel[]) {

        return this.http.post<PropertyModel[]>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/' + componentInstanceId + '/properties', properties)
            .map((res) => {
                return res.map((resProperty) => {
                    const newProp = new PropertyModel(resProperty);
                    newProp.resourceInstanceUniqueId = componentInstanceId;
                    return newProp;
                });
            });
    }

    updateInstanceAttributes(componentType: string, componentId: string, componentInstanceId: string, attributes: AttributeBEModel[]) {

        return this.http.post<AttributeModel[]>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/' + componentInstanceId + '/attributes', attributes)
            .map((res) => {
                return res.map((resAttribute) => {
                    const newAttrib = new AttributeModel(resAttribute);
                    newAttrib.resourceInstanceUniqueId = componentInstanceId;
                    return newAttrib;
                });
            });
    }

    getInstanceCapabilityProperties(componentType: string, componentId: string, componentInstanceId: string, capability: Capability): Observable<PropertyModel[]> {

        return this.http.get<PropertyModel[]>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/componentInstances/' + componentInstanceId + '/capability/' + capability.type +
            '/capabilityName/' +  capability.name + '/ownerId/' + capability.ownerId + '/properties')
            .map((res) => {
                capability.properties = res.map((capProp) => new PropertyModel(capProp));  // update capability properties
                return capability.properties;
            });
    }

    updateInstanceCapabilityProperties(component: Component, componentInstanceId: string, capability: Capability, properties: PropertyBEModel[]): Observable<PropertyModel[]> {

        return this.http.put<PropertyModel[]>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstanceId + '/capability/' +  capability.type +
            '/capabilityName/' +  capability.name + '/ownerId/' + capability.ownerId + '/properties', properties)
            .map((res) => {
                const savedProperties: PropertyModel[] = res.map((resProperty) => new PropertyModel(resProperty));
                savedProperties.forEach((savedProperty) => {
                    const propIdx = capability.properties.findIndex((p) => p.uniqueId === savedProperty.uniqueId);
                    if (propIdx !== -1) {
                        capability.properties.splice(propIdx, 1, savedProperty);
                    }
                });
                return savedProperties;
            });
    }

    updateInstanceRequirement(componentType: string, componentId: string, componentInstanceId: string, requirement: Requirement): Observable<Requirement> {
        return this.http.put<Requirement>(this.baseUrl + componentType + componentId + '/componentInstances/' + componentInstanceId + '/requirement/' +  requirement.capability +
            '/requirementName/' +  requirement.name, requirement);
    }

    updateInstanceCapability(componentTypeUrl: string, componentId: string, componentInstanceId: string, capability: Capability): Observable<Capability> {
        const url = `${this.baseUrl}${componentTypeUrl}${componentId}/componentInstances/${componentInstanceId}/capability/`;
        return this.http.put<Capability>(url, capability);
    }

    updateInstanceInputs(component: Component, componentInstanceId: string, inputs: PropertyBEModel[]): Observable<PropertyBEModel[]> {

        return this.http.post<PropertyModel[]>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstance/' + componentInstanceId + '/inputs', inputs)
            .map((res) => {
                return res.map((resInput) => new PropertyBEModel(resInput));
            });
    }

    updateInstanceOutputs(component: Component, componentInstanceId: string, outputs: AttributeBEModel[]): Observable<AttributeBEModel[]> {

        return this.http.post<AttributeModel[]>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstance/' + componentInstanceId + '/outputs', outputs)
            .map((res) => {
                return res.map((resOutput) => new AttributeBEModel(resOutput));
            });
    }

    getComponentGroupInstanceProperties(component: Component, groupInstanceId: string): Observable<PropertyBEModel[]> {
        return this.http.get<PropertyBEModel[]>(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/groups/' + groupInstanceId + '/properties')
            .map((res) => {
                return CommonUtils.initBeProperties(res);
            });
    }

    updateComponentGroupInstanceProperties(componentType: string, componentId: string, groupInstanceId: string, properties: PropertyBEModel[]): Observable<PropertyBEModel[]> {
        return this.http.put<PropertyBEModel[]>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/groups/' + groupInstanceId + '/properties', properties)
            .map((res) => {
                return res.map((resProperty) => new PropertyBEModel(resProperty));
            });
    }

    // Service group-instance property update: PUT {svcId}/resourceInstance/{ri}/groupInstance/{gi}. Mirrors the
    // legacy LegacyServiceService.updateGroupInstanceProperties (app/services/legacy/service.service.ts), used by the
    // module-property modal for a service (a resource uses updateComponentGroupInstanceProperties above instead).
    updateGroupInstanceProperties(componentType: string, componentId: string, resourceInstanceId: string, groupInstanceId: string, properties: PropertyBEModel[]): Observable<PropertyBEModel[]> {
        return this.http.put<PropertyBEModel[]>(
            this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/' + resourceInstanceId + '/groupInstance/' + groupInstanceId, properties)
            .map((res) => {
                return res.map((resProperty) => new PropertyBEModel(resProperty));
            });
    }

    getComponentPolicyInstanceProperties(componentType: string, componentId: string, policyInstanceId: string): Observable<PropertyBEModel[]> {
        return this.http.get<PropertyBEModel[]>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/policies/' + policyInstanceId + '/properties')
            .map((res) => {
                return CommonUtils.initBeProperties(res);
            });
    }

    updateComponentPolicyInstanceProperties(componentType: string, componentId: string, policyInstanceId: string, properties: PropertyBEModel[]): Observable<PropertyBEModel[]> {
        return this.http.put<PropertyBEModel[]>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/policies/' + policyInstanceId + '/properties', properties)
            .map((res) => {
                return res.map((resProperty) => new PropertyBEModel(resProperty));
            });
    }

    addInstanceArtifact = (componentType: string, componentId: string, instanceId: string, artifact: ArtifactModel): Observable<ArtifactModel> => {
        let headerObj = new HttpHeaders();
        if (artifact.payloadData) {
            headerObj = headerObj.set('Content-MD5', HttpHelperService.getHeaderMd5(artifact));
        }
        return this.http.post<ArtifactModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/' + instanceId + '/artifacts', JSON.stringify(artifact), {headers: headerObj})
        .map((res) => {
            return new ArtifactModel(res);
        });
    }

    updateInstanceArtifact = (componentType: string, componentId: string, instanceId: string, artifact: ArtifactModel): Observable<ArtifactModel> => {
        return this.http.post<ArtifactModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/' + instanceId + '/artifacts/' + artifact.uniqueId, artifact).map((res) => {
            return new ArtifactModel(res);
        });
    }

    deleteInstanceArtifact = (componentId: string, componentType: string, instanceId: string, artifactId: string, artifactLabel: string): Observable<ArtifactModel> => {
        return this.http.delete<ArtifactModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/' + instanceId + '/artifacts/' + artifactId + '?operation=' + artifactLabel)
            .map((res) => {
                return new ArtifactModel(res);
            });
    }

    downloadInstanceArtifact = (componentType: string, componentId: string, instanceId: string, artifactId: string): Observable<IFileDownload> => {
        return this.http.get<IFileDownload>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstances/' + instanceId + '/artifacts/' + artifactId);
    }

    uploadInstanceEnvFile = (componentType: string, componentId: string, instanceId: string, artifact: ArtifactModel): Observable<ArtifactModel> => {
        let headerObj = new HttpHeaders();
        if (artifact.payloadData) {
            headerObj = headerObj.set('Content-MD5', HttpHelperService.getHeaderMd5(artifact));
        }
        return this.http.post<ArtifactModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/' + instanceId + '/artifacts/' + artifact.uniqueId, JSON.stringify(artifact), {headers: headerObj});
    }

    updateInstanceAttribute = (componentType: string, componentId: string, attribute: AttributeModel): Observable<AttributeModel> => {
        const instanceId = attribute.resourceInstanceUniqueId;
        return this.http.post<AttributeModel>(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/resourceInstance/' + instanceId + '/attribute', attribute)
            .map((response) => {
                const newAttribute = new AttributeModel(response);
                newAttribute.readonly = true;
                newAttribute.resourceInstanceUniqueId = instanceId;
                return newAttribute;
            });
    }

    private getServerTypeUrl = (componentType: string): string => {
        switch (componentType) {
            case ComponentType.SERVICE:
                return ServerTypeUrl.SERVICES;
            default:
                return ServerTypeUrl.RESOURCES;
        }
    }

}
