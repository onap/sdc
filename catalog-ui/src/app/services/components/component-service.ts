/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
'use strict';
import * as _ from "lodash";
import {Inject, Injectable} from "@angular/core";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {
    ArtifactModel,
    IFileDownload,
    InstancesInputsPropertiesMap,
    InputModel,
    IValidate,
    RelationshipModel,
    PropertyModel,
    Component,
    ComponentInstance,
    AttributeModel,
    IAppConfigurtaion,
    Resource,
    Module,
    DisplayModule,
    ArtifactGroupModel,
    InputsAndProperties,
    AsdcComment
} from "app/models";
import {ComponentInstanceFactory, CommonUtils} from "app/utils";
// Direct imports to avoid loading the services-ng2 barrel which causes a circular dep
// at module-load time once @Injectable emits type metadata (emitDecoratorMetadata):
// services-ng2 barrel -> catalog.service -> data-type-catalog-component -> service.ts,
// which tries to extend Component from the still-loading models.ts (partial) -> crash.
import {SharingService} from "../../ng2/services/sharing.service";
import {DataTypesService} from "../data-types-service";
import {ComponentMetadata} from "../../models/component-metadata";
import {SdcConfigToken} from "../../ng2/config/sdc-config.config";

export interface IComponentService {

    getComponent(id:string);
    updateComponent(component:Component):ng.IPromise<Component>;
    changeLifecycleState(component:Component, state:string, userRemarks:AsdcComment):ng.IPromise<ComponentMetadata> ;
    validateName(newName:string, subtype?:string):ng.IPromise<IValidate>;
    createComponent(component:Component):ng.IPromise<Component>;
    //importComponent
    importComponent(component: Component): ng.IPromise<Component>;
    addOrUpdateArtifact(componentId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    deleteArtifact(componentId:string, artifact:string, artifactLabel):ng.IPromise<ArtifactModel>;
    addProperty(componentId:string, property:PropertyModel):ng.IPromise<PropertyModel>;
    updateProperty(componentId:string, property:PropertyModel):ng.IPromise<PropertyModel>;
    addAttribute(componentId:string, attribute:AttributeModel):ng.IPromise<AttributeModel>;
    updateAttribute(componentId:string, attribute:AttributeModel):ng.IPromise<AttributeModel>;
    deleteProperty(componentId:string, propertyId:string):ng.IPromise<PropertyModel>;
    deleteAttribute(componentId:string, attributeId:string):ng.IPromise<AttributeModel>;
    checkResourceInstanceVersionChange(componentId:string, componentInstanceId:string, componentUid:string):ng.IPromise<any>;
    changeResourceInstanceVersion(componentId:string, componentInstanceId:string, componentUid:string):ng.IPromise<ComponentInstance>;
    updateInstanceArtifact(componentId:string, instanceId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    addInstanceArtifact(componentId:string, instanceId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    deleteInstanceArtifact(componentId:string, instanceId:string, artifact:string, artifactLabel):ng.IPromise<ArtifactModel>;
    createComponentInstance(componentId:string, componentInstance:ComponentInstance):ng.IPromise<ComponentInstance>;
    updateComponentInstance(componentId:string, componentInstance:ComponentInstance):ng.IPromise<ComponentInstance>;
    updateMultipleComponentInstances(componentId:string, instances:Array<ComponentInstance>):ng.IPromise< Array<ComponentInstance>>;
    downloadArtifact(componentId: string, artifactId: string, vendorName?: string): ng.IPromise<IFileDownload>;
    uploadInstanceEnvFile(componentId:string, instanceId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    downloadInstanceArtifact(componentId:string, instanceId:string, artifactId:string):ng.IPromise<IFileDownload>;
    deleteComponentInstance(componentId:string, componentInstanceId:string):ng.IPromise<ComponentInstance>;
    createRelation(componentId:string, link:RelationshipModel):ng.IPromise<RelationshipModel>;
    deleteRelation(componentId:string, link:RelationshipModel):ng.IPromise<RelationshipModel>;
    fetchRelation(componentId:string, linkId:string):ng.IPromise<RelationshipModel>;
    getRequirementsCapabilities(componentId:string):ng.IPromise<any>;
    updateInstanceProperties(componentId:string, componentInstanceId:string, properties:PropertyModel[]):ng.IPromise<PropertyModel[]>;
    updateInstanceAttribute(componentId:string, attribute:AttributeModel):ng.IPromise<AttributeModel>;
    getComponentInstancesFilteredByInputsAndProperties(componentId:string, searchText:string):ng.IPromise<Array<ComponentInstance>>
    getComponentInstanceInputs(componentId:string, instanceId:string, originComponentUid):ng.IPromise<Array<InputModel>>;
    getComponentInputs(componentId:string):ng.IPromise<Array<InputModel>>;
    getComponentInstanceInputProperties(componentId:string, instanceId:string, inputId:string):ng.IPromise<Array<PropertyModel>>;
    getComponentInstanceProperties(componentId:string, instanceId:string):ng.IPromise<Array<PropertyModel>>;
    getModuleForDisplay(componentId:string, moduleId:string):ng.IPromise<DisplayModule>;
    getComponentInstanceModule(componentId:string, componentInstanceId:string, moduleId:string):ng.IPromise<DisplayModule>;
    updateGroupMetadata(componentId:string, group:Module):ng.IPromise<Module>;
    getComponentInputInputsAndProperties(serviceId:string, input:string):ng.IPromise<InputsAndProperties>;
    createInputsFromInstancesInputs(serviceId:string, instancesInputsMap:InstancesInputsPropertiesMap):ng.IPromise<Array<InputModel>>;
    createInputsFromInstancesInputsProperties(resourceId:string, instanceInputsPropertiesMap:InstancesInputsPropertiesMap):ng.IPromise<Array<PropertyModel>>;
    deleteComponentInput(serviceId:string, inputId:string):ng.IPromise<InputModel>;
    getArtifactByGroupType(componentId:string, artifactGroupType:string):ng.IPromise<ArtifactGroupModel>;
    getComponentInstanceArtifactsByGroupType(componentId:string, componentInstanceId:string, artifactGroupType:string):ng.IPromise<ArtifactGroupModel>;
}

@Injectable()
export class ComponentService implements IComponentService {

    protected baseUrl: string;
    protected typeName: string = '';   // subclasses set 'resources' / 'services'

    constructor(@Inject(SdcConfigToken) sdcConfig: IAppConfigurtaion,
                http: HttpClient,
                sharingService: SharingService,
                dataTypeService: DataTypesService,
                @Inject('$q') $q: ng.IQService) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
        // §SS: keep Angular deps OFF the enumerable surface. The Component/Resource/Service model
        // classes hold this service (enumerable `componentService`) and their toJSON() does
        // angular.copy(this); HttpClient's object graph reaches an ngUpgrade Scope, so an enumerable
        // http field makes angular.copy throw `ng:cpws` and hangs create/import (failure-catalog §SS).
        Object.defineProperty(this, 'http', {value: http, enumerable: false, writable: false, configurable: true});
        Object.defineProperty(this, 'sharingService', {value: sharingService, enumerable: false, writable: false, configurable: true});
        Object.defineProperty(this, 'dataTypeService', {value: dataTypeService, enumerable: false, writable: false, configurable: true});
        Object.defineProperty(this, '$q', {value: $q, enumerable: false, writable: false, configurable: true});
    }

    // typed non-enumerable fields (declared for the compiler; assigned via defineProperty above)
    protected http: HttpClient;
    protected sharingService: SharingService;
    protected dataTypeService: DataTypesService;
    protected $q: ng.IQService;

    // URL helper: {baseUrl}{typeName}/{seg}/{seg}... (typeName '' for base, no leading segment)
    protected url = (...segments: Array<string>): string => {
        const parts = this.typeName ? [this.typeName, ...segments] : [...segments];
        return this.baseUrl + parts.filter(s => s !== undefined && s !== null && s !== '').join('/');
    };

    //this function is override by each service, we need to change this method to abstract when updating typescript version
    protected createComponentObject = (component: Component): Component => {
        return component;
    };

    private getHeaderMd5 = (object: any): HttpHeaders => {
        // This is ugly workaround preserved from master: md5 is only correct if JSON.stringify runs twice.
        JSON.stringify(object);
        const md5Result = md5(JSON.stringify(object)).toLowerCase();
        return new HttpHeaders({'Content-MD5': btoa(md5Result)});
    };

    public getComponent = (id: string): ng.IPromise<Component> => {
        return this.http.get(this.url(id)).toPromise().then(
            (response: Component) => this.createComponentObject(response)) as any;
    };

    public updateComponent = (component: Component): ng.IPromise<Component> => {
        if (component instanceof Resource) {
            let resource: Resource = <Resource>component;
            if (resource.importedFile) {
                return this.updateResourceWithPayload(resource);
            } else {
                if (component.csarUUID) {
                    return this.updateResource(component);
                } else {
                    return this.updateResourceMetadata(component);
                }
            }
        } else {
            return this.updateService(component);
        }
    };

    private updateService = (component: Component): ng.IPromise<Component> => {
        return this.http.put(this.url(component.uniqueId, 'metadata'), JSON.stringify(component)).toPromise()
            .then((response: Component) => this.createComponentObject(response)) as any;
    };

    private updateResource = (component: Component): ng.IPromise<Component> => {
        return this.http.put(this.url(component.uniqueId), JSON.stringify(component)).toPromise()
            .then((response: Component) => this.createComponentObject(response)) as any;
    };

    private updateResourceMetadata = (component: Component): ng.IPromise<Component> => {
        return this.http.put(this.url(component.uniqueId, 'metadata'), JSON.stringify(component)).toPromise()
            .then((response: Component) => this.createComponentObject(response)) as any;
    };

    /**
     * Only resource can be updated with payload data
     * @param component
     * @returns {IPromise<T>}
     */
    private updateResourceWithPayload = (resource: Resource): ng.IPromise<Component> => {
        resource.payloadData = resource.importedFile.base64;
        resource.payloadName = resource.importedFile.filename;
        const headers = this.getHeaderMd5(resource);
        return this.http.put(this.url(resource.uniqueId), JSON.stringify(resource), {headers}).toPromise()
            .then((response: Component) => this.createComponentObject(response)) as any;
    };

    public createComponent = (component: Component): ng.IPromise<Component> => {
        const headers = this.getHeaderMd5(component);
        return this.http.post(this.url(), JSON.stringify(component), {headers}).toPromise()
            .then((response: Component) => this.createComponentObject(response)) as any;
    };

    public importComponent = (component: Component): ng.IPromise<Component> => {
        component.vendorName = "xfr";
        component.vendorRelease = "xfr";
        const headers = this.getHeaderMd5(component);
        return this.http.post(this.url('importService'), JSON.stringify(component), {headers}).toPromise()
            .then((response: Component) => {
                const result: Component = this.createComponentObject(response);
                this.dataTypeService.loadDataTypesCache(result.model);
                return result;
            }) as any;
    };

    public validateName = (newName: string, subtype?: string): ng.IPromise<IValidate> => {
        let params = new HttpParams();
        if (subtype !== undefined) { params = params.set('subtype', subtype); }
        return this.http.get(this.url('validate-name', newName), {params}).toPromise() as any;
    };

    public changeLifecycleState = (component: Component, state: string, commentObj: AsdcComment): ng.IPromise<ComponentMetadata> => {
        const onOk = (response: ComponentMetadata) => {
            this.sharingService.addUuidValue(response.uniqueId, response.uuid);
            return new ComponentMetadata().deserialize(response);
        };
        if (commentObj.userRemarks) {
            const headers = this.getHeaderMd5(commentObj);
            return this.http.post(this.url(component.uniqueId, state), JSON.stringify(commentObj), {headers}).toPromise().then(onOk) as any;
        }
        return this.http.post(this.url(component.uniqueId, state), null).toPromise().then(onOk) as any;
    };

    // ------------------------------------------------ Artifacts API --------------------------------------------------//
    public addOrUpdateArtifact = (componentId: string, artifact: ArtifactModel): ng.IPromise<ArtifactModel> => {
        let headers: HttpHeaders | undefined;
        if (artifact.payloadData) {
            headers = this.getHeaderMd5(artifact);
        }
        const options = headers ? {headers} : {};
        return this.http.post(this.url(componentId, 'artifacts', artifact.uniqueId), JSON.stringify(artifact), options).toPromise() as any;
    };

    public downloadArtifact = (componentId: string, artifactId: string, vendorName?: string): ng.IPromise<IFileDownload> => {
        if (vendorName === 'IsService') {
            return this.http.get(this.url('importService', componentId, 'artifacts', artifactId)).toPromise() as any;
        } else {
            return this.http.get(this.url(componentId, 'artifacts', artifactId)).toPromise() as any;
        }
    };

    public deleteArtifact = (componentId: string, artifactId: string, artifactLabel: string): ng.IPromise<ArtifactModel> => {
        const params = new HttpParams().set('operation', artifactLabel);
        return this.http.delete(this.url(componentId, 'artifacts', artifactId), {params}).toPromise() as any;
    };

    public getArtifactByGroupType = (componentId: string, artifactGroupType: string): ng.IPromise<ArtifactGroupModel> => {
        return this.http.get(this.url(componentId, 'artifactsByType', artifactGroupType)).toPromise()
            .then((response: any) => new ArtifactGroupModel(response)) as any;
    };

    public getComponentInstanceArtifactsByGroupType = (componentId: string, componentInstanceId: string, artifactGroupType: string): ng.IPromise<ArtifactGroupModel> => {
        return this.http.get(this.url(componentId, 'resourceInstances', componentInstanceId, 'artifactsByType', artifactGroupType)).toPromise()
            .then((response: any) => new ArtifactGroupModel(response)) as any;
    };

    // ------------------------------------------------ Properties API --------------------------------------------------//
    public addProperty = (componentId: string, property: PropertyModel): ng.IPromise<PropertyModel> => {
        return this.http.post(this.url(componentId, 'properties'), property.convertToServerObject()).toPromise()
            .then((response: any) => new PropertyModel(response[Object.keys(response)[0]])) as any;
    };

    public updateProperty = (componentId: string, property: PropertyModel): ng.IPromise<PropertyModel> => {
        return this.http.put(this.url(componentId, 'properties', property.uniqueId), property.convertToServerObject()).toPromise()
            .then((response: any) => new PropertyModel(response[Object.keys(response)[0]])) as any;
    };

    public deleteProperty = (componentId: string, propertyId: string): ng.IPromise<PropertyModel> => {
        return this.http.delete(this.url(componentId, 'properties', propertyId)).toPromise() as any;
    };

    // ------------------------------------------------ Attributes API --------------------------------------------------//
    public addAttribute = (componentId: string, attribute: AttributeModel): ng.IPromise<AttributeModel> => {
        return this.http.post(this.url(componentId, 'attributes'), attribute.convertToServerObject()).toPromise()
            .then((response: any) => new AttributeModel(response)) as any;
    };

    public updateAttribute = (componentId: string, attribute: AttributeModel): ng.IPromise<AttributeModel> => {
        return this.http.put(this.url(componentId, 'attributes', attribute.uniqueId), attribute.convertToServerObject()).toPromise()
            .then((response: any) => new AttributeModel(response)) as any;
    };

    public deleteAttribute = (componentId: string, attributeId: string): ng.IPromise<AttributeModel> => {
        return this.http.delete(this.url(componentId, 'attributes', attributeId)).toPromise() as any;
    };

    // ------------------------------------------------ Component Instances API --------------------------------------------------//

    public createComponentInstance = (componentId: string, componentInstance: ComponentInstance): ng.IPromise<ComponentInstance> => {
        return this.http.post(this.url(componentId, 'resourceInstance'), JSON.stringify(componentInstance)).toPromise()
            .then((response: any) => {
                let createdInstance: ComponentInstance = ComponentInstanceFactory.createComponentInstance(response);
                console.log("Component Instance created", createdInstance);
                return createdInstance;
            }, (err) => {
                console.log("Failed to create componentInstance. With Name: " + componentInstance.name);
                return Promise.reject(err);
            }) as any;
    };

    public updateComponentInstance = (componentId: string, componentInstance: ComponentInstance): ng.IPromise<ComponentInstance> => {
        return this.http.post(this.url(componentId, 'resourceInstance', componentInstance.uniqueId), JSON.stringify(componentInstance)).toPromise()
            .then((response: any) => {
                let updatedInstance: ComponentInstance = ComponentInstanceFactory.createComponentInstance(response);
                console.log("Component Instance was updated", updatedInstance);
                return updatedInstance;
            }, (err) => {
                console.log("Failed to update componentInstance. With ID: " + componentInstance.uniqueId + "Name: " + componentInstance.name);
                return Promise.reject(err);
            }) as any;
    };

    public updateMultipleComponentInstances = (componentId: string, instances: Array<ComponentInstance>): ng.IPromise<Array<ComponentInstance>> => {
        return this.http.post(this.url(componentId, 'resourceInstance/multipleComponentInstance'), JSON.stringify(instances)).toPromise()
            .then((response: any) => {
                console.log("Multiple Component Instances was updated", response);
                let updateInstances: Array<ComponentInstance> = new Array<ComponentInstance>();
                _.forEach(response, (componentInstance: ComponentInstance) => {
                    let updatedComponentInstance: ComponentInstance = ComponentInstanceFactory.createComponentInstance(componentInstance);
                    updateInstances.push(updatedComponentInstance);
                });
                return updateInstances;
            }, (err) => {
                console.log("Failed to update Multiple componentInstance.");
                return Promise.reject(err);
            }) as any;
    };

    public deleteComponentInstance = (componentId: string, componentInstanceId: string): ng.IPromise<ComponentInstance> => {
        return this.http.delete(this.url(componentId, 'resourceInstance', componentInstanceId)).toPromise()
            .then(() => {
                console.log("Component Instance was deleted");
            }, (err) => {
                console.log("Failed to delete componentInstance. With ID: " + componentInstanceId);
                return Promise.reject(err);
            }) as any;
    };

    public checkResourceInstanceVersionChange = (componentId: string, componentInstanceId: string, componentUid: string): ng.IPromise<ComponentInstance> => {
        return this.http.get(this.url(componentId, 'resourceInstance', componentInstanceId, componentUid, 'checkForwardingPathOnVersionChange')).toPromise() as any;
    };

    public changeResourceInstanceVersion = (componentId: string, componentInstanceId: string, componentUid: string): ng.IPromise<ComponentInstance> => {
        return this.http.post(this.url(componentId, 'resourceInstance', componentInstanceId, 'changeVersion'), {'componentUid': componentUid}).toPromise()
            .then((response: any) => ComponentInstanceFactory.createComponentInstance(response)) as any;
    };

    public downloadInstanceArtifact = (componentId: string, instanceId: string, artifactId: string): ng.IPromise<IFileDownload> => {
        return this.http.get(this.url(componentId, 'resourceInstances', instanceId, 'artifacts', artifactId)).toPromise() as any;
    };

    public updateInstanceArtifact = (componentId: string, instanceId: string, artifact: ArtifactModel): ng.IPromise<ArtifactModel> => {
        let headers: HttpHeaders | undefined;
        if (artifact.payloadData) {
            headers = this.getHeaderMd5(artifact);
        }
        const options = headers ? {headers} : {};
        return this.http.post(this.url(componentId, 'resourceInstance', instanceId, 'artifacts', artifact.uniqueId), JSON.stringify(artifact), options).toPromise()
            .then((response: any) => new ArtifactModel(response)) as any;
    };

    public addInstanceArtifact = (componentId: string, instanceId: string, artifact: ArtifactModel): ng.IPromise<ArtifactModel> => {
        let headers: HttpHeaders | undefined;
        if (artifact.payloadData) {
            headers = this.getHeaderMd5(artifact);
        }
        const options = headers ? {headers} : {};
        return this.http.post(this.url(componentId, 'resourceInstance', instanceId, 'artifacts', artifact.uniqueId), JSON.stringify(artifact), options).toPromise()
            .then((response: any) => new ArtifactModel(response)) as any;
    };

    public deleteInstanceArtifact = (componentId: string, instanceId: string, artifactId: string, artifactLabel: string): ng.IPromise<ArtifactModel> => {
        const params = new HttpParams().set('operation', artifactLabel);
        return this.http.delete(this.url(componentId, 'resourceInstance', instanceId, 'artifacts', artifactId), {params}).toPromise() as any;
    };

    public uploadInstanceEnvFile = (componentId: string, instanceId: string, artifact: ArtifactModel): ng.IPromise<ArtifactModel> => {
        let headers: HttpHeaders | undefined;
        if (artifact.payloadData) {
            headers = this.getHeaderMd5(artifact);
        }
        const options = headers ? {headers} : {};
        return this.http.post(this.url(componentId, 'resourceInstance', instanceId, 'artifacts', artifact.uniqueId), JSON.stringify(artifact), options).toPromise()
            .then((response: any) => new ArtifactModel(response)) as any;
    };

    public updateInstanceProperties = (componentId: string, componentInstanceId: string, properties: PropertyModel[]): ng.IPromise<PropertyModel[]> => {
        return this.http.post(this.url(componentId, 'resourceInstance', componentInstanceId, 'properties'), JSON.stringify(properties)).toPromise()
            .then((response: any) => {
                const newProperties = response.map((res) => {
                    const newProperty = new PropertyModel(res);
                    newProperty.readonly = true;
                    newProperty.resourceInstanceUniqueId = componentInstanceId;
                    return newProperty;
                });
                return newProperties;
            }) as any;
    };

    public updateInstanceAttribute = (componentId: string, attribute: AttributeModel): ng.IPromise<AttributeModel> => {
        let instanceId = attribute.resourceInstanceUniqueId;
        return this.http.post(this.url(componentId, 'resourceInstance', instanceId, 'attribute'), JSON.stringify(attribute)).toPromise()
            .then((response: any) => {
                let newAttribute = new AttributeModel(response);
                newAttribute.readonly = true;
                newAttribute.resourceInstanceUniqueId = instanceId;
                return newAttribute;
            }) as any;
    };

    public createRelation = (componentId: string, link: RelationshipModel): ng.IPromise<RelationshipModel> => {
        const linkPayload: RelationshipModel = new RelationshipModel(link);
        linkPayload.relationships.forEach((rel) => {
            delete rel.capability;
            delete rel.requirement;
        });
        return this.http.post(this.url(componentId, 'resourceInstance', 'associate'), JSON.stringify(linkPayload)).toPromise()
            .then((response: any) => {
                let relation: RelationshipModel = new RelationshipModel(response);
                console.log("Link created successfully ", relation);
                return relation;
            }, (err) => {
                console.log("Failed to create Link From: " + link.fromNode + "To: " + link.toNode);
                return Promise.reject(err);
            }) as any;
    };

    public deleteRelation = (componentId: string, link: RelationshipModel): ng.IPromise<RelationshipModel> => {
        const linkPayload: RelationshipModel = new RelationshipModel(link);
        linkPayload.relationships.forEach((rel) => {
            delete rel.capability;
            delete rel.requirement;
        });
        return this.http.put(this.url(componentId, 'resourceInstance', 'dissociate'), JSON.stringify(linkPayload)).toPromise()
            .then((response: any) => {
                let relation: RelationshipModel = new RelationshipModel(response);
                console.log("Link deleted successfully ", relation);
                return relation;
            }, (err) => {
                console.log("Failed to delete Link From: " + link.fromNode + "To: " + link.toNode);
                return Promise.reject(err);
            }) as any;
    };

    public fetchRelation = (componentId: string, linkId: string): ng.IPromise<RelationshipModel> => {
        return this.http.get(this.url(componentId, 'relationId', linkId)).toPromise()
            .then((response: any) => {
                let relation: RelationshipModel = new RelationshipModel(response);
                console.log("Link fetched successfully ", relation);
                return relation;
            }, (err) => {
                console.log("Failed to fetch Link Id: " + linkId);
                return Promise.reject(err);
            }) as any;
    };

    public getRequirementsCapabilities = (componentId: string): ng.IPromise<any> => {
        return this.http.get(this.url(componentId, 'requirmentsCapabilities')).toPromise()
            .then((response: any) => {
                console.log("Component requirement capabilities recived: ", response);
                return response;
            }, (err) => {
                console.log("Failed to get requirements & capabilities");
                return Promise.reject(err);
            }) as any;
    };

    public getModuleForDisplay = (componentId: string, moduleId: string): ng.IPromise<DisplayModule> => {
        return this.http.get(this.url(componentId, 'groups', moduleId)).toPromise()
            .then((response: any) => {
                console.log("module loaded successfully: ", response);
                return new DisplayModule(response);
            }, (err) => {
                console.log("Failed to get module with id: ", moduleId);
                return Promise.reject(err);
            }) as any;
    };

    public getComponentInstanceModule = (componentId: string, componentInstanceId: string, moduleId: string): ng.IPromise<DisplayModule> => {
        return this.http.get(this.url(componentId, 'resourceInstance', componentInstanceId, 'groupInstance', moduleId)).toPromise()
            .then((response: any) => {
                console.log("module loaded successfully: ", response);
                return new DisplayModule(response);
            }, (err) => {
                console.log("Failed to get module with id: ", moduleId);
                return Promise.reject(err);
            }) as any;
    };

    public getComponentInstancesFilteredByInputsAndProperties = (componentId: string, searchText?: string): ng.IPromise<Array<ComponentInstance>> => {
        let params = new HttpParams();
        if (searchText !== undefined) { params = params.set('searchText', searchText); }
        return this.http.get(this.url(componentId, 'componentInstances'), {params}).toPromise()
            .then((response: any) => {
                console.log("component instances return successfully: ", response);
                return CommonUtils.initComponentInstances(response);
            }, (err) => {
                console.log("Failed to get component instances of component with id: " + componentId);
                return Promise.reject(err);
            }) as any;
    };

    public getComponentInstanceInputs = (componentId: string, instanceId: string, originComponentUid): ng.IPromise<Array<InputModel>> => {
        return this.http.get(this.url(componentId, 'componentInstances', instanceId, originComponentUid, 'inputs')).toPromise()
            .then((response: any) => {
                console.log("component instance input return successfully: ", response);
                let inputsArray: Array<InputModel> = new Array<InputModel>();
                _.forEach(response, (inputObj: InputModel) => {
                    inputsArray.push(new InputModel(inputObj));
                });
                return inputsArray;
            }, (err) => {
                console.log("Failed to get component instance input with id: " + instanceId);
                return Promise.reject(err);
            }) as any;
    };

    public getComponentInputs = (componentId: string): ng.IPromise<Array<InputModel>> => {
        return this.http.get(this.url(componentId, 'inputs')).toPromise()
            .then((response: any) => {
                console.log("component inputs return successfully: ", response);
                let inputsArray: Array<InputModel> = new Array<InputModel>();
                _.forEach(response, (inputObj: InputModel) => {
                    inputsArray.push(new InputModel(inputObj));
                });
                return inputsArray;
            }, (err) => {
                console.log("Failed to get component inputs for component with id: " + componentId);
                return Promise.reject(err);
            }) as any;
    };

    public getComponentInstanceInputProperties = (componentId: string, instanceId: string, inputId: string): ng.IPromise<Array<PropertyModel>> => {
        return this.http.get(this.url(componentId, 'componentInstances', instanceId, inputId, 'properties')).toPromise()
            .then((response: any) => {
                console.log("component instance input properties return successfully: ", response);
                let propertiesArray: Array<PropertyModel> = new Array<PropertyModel>();
                _.forEach(response, (propertyObj: PropertyModel) => {
                    propertiesArray.push(new PropertyModel(propertyObj));
                });
                return propertiesArray;
            }, (err) => {
                console.log("Failed to get component instance input properties with instanceId: " + instanceId + "and input id: " + inputId);
                return Promise.reject(err);
            }) as any;
    };

    public getComponentInstanceProperties = (componentId: string, instanceId: string): ng.IPromise<Array<PropertyModel>> => {
        return this.http.get(this.url(componentId, 'componentInstances', instanceId, 'properties')).toPromise()
            .then((response: any) => {
                console.log("component instance  properties return successfully: ", response);
                let propertiesArray: Array<PropertyModel> = new Array<PropertyModel>();
                _.forEach(response, (propertyObj: PropertyModel) => {
                    propertiesArray.push(new PropertyModel(propertyObj));
                });
                return propertiesArray;
            }, (err) => {
                console.log("Failed to get component instance  properties with instanceId: " + instanceId);
                return Promise.reject(err);
            }) as any;
    };

    public updateGroupMetadata = (componentId: string, group: Module): ng.IPromise<Module> => {
        return this.http.put(this.url(componentId, 'groups', group.uniqueId, 'metadata'), JSON.stringify(group)).toPromise()
            .then((response: Module) => {
                console.log("group metadata updated successfully: ", response);
                return new Module(response);
            }, (err) => {
                console.log("Failed to update group metadata for component: " + componentId + " for group with id: " + group.uniqueId);
                return Promise.reject(err);
            }) as any;
    };

    public getComponentInputInputsAndProperties = (serviceId: string, inputId: string): ng.IPromise<InputsAndProperties> => {
        return this.http.get(this.url(serviceId, 'inputs', inputId)).toPromise()
            .then((response: InputsAndProperties) => {
                let inputsArray: Array<InputModel> = new Array<InputModel>();
                _.forEach(response.inputs, (inputObj: InputModel) => {
                    inputsArray.push(new InputModel(inputObj));
                });

                let propertiesArray: Array<PropertyModel> = new Array<PropertyModel>();
                _.forEach(response.properties, (property: PropertyModel) => {
                    propertiesArray.push(new PropertyModel(property));
                });

                return new InputsAndProperties(inputsArray, propertiesArray);
            }, (err) => {
                console.log("failed to get inputs of input : ", err);
                return Promise.reject(err);
            }) as any;
    };

    createInputsFromInstancesInputsProperties = (resourceId: string, instancePropertyMap: InstancesInputsPropertiesMap): ng.IPromise<Array<PropertyModel>> => {
        return this.http.post(this.url(resourceId, 'create/properties'), instancePropertyMap).toPromise()
            .then((response: any) => {
                let inputsArray: Array<PropertyModel> = new Array<PropertyModel>();
                _.forEach(response, (inputObj: PropertyModel) => {
                    inputsArray.push(new PropertyModel(inputObj));
                });
                return inputsArray;
            }, (err) => {
                console.log("failed to create service inputs from VF instances inputs : ", err);
                return Promise.reject(err);
            }) as any;
    };

    createInputsFromInstancesInputs = (serviceId: string, instancesMap: InstancesInputsPropertiesMap): ng.IPromise<Array<InputModel>> => {
        return this.http.post(this.url(serviceId, 'create/inputs'), instancesMap).toPromise()
            .then((response: any) => {
                let inputsArray: Array<InputModel> = new Array<InputModel>();
                _.forEach(response, (inputObj: InputModel) => {
                    inputsArray.push(new InputModel(inputObj));
                });
                return inputsArray;
            }, (err) => {
                console.log("failed to create service inputs from VF instances inputs : ", err);
                return Promise.reject(err);
            }) as any;
    };

    deleteComponentInput = (serviceId: string, inputId: string): ng.IPromise<InputModel> => {
        return this.http.delete(this.url(serviceId, 'delete', inputId, 'input')).toPromise()
            .then((response: any) => new InputModel(response),
            (err) => {
                console.log("failed to delete input from service: ", err);
                return Promise.reject(err);
            }) as any;
    };

}
