import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import {Response, URLSearchParams} from '@angular/http';
import { Component, PropertyBEModel, InstancePropertiesAPIMap, FilterPropertiesAssignmentData} from "app/models";
import {downgradeInjectable} from '@angular/upgrade/static';
import {COMPONENT_FIELDS} from "app/utils";
import {ComponentGenericResponse} from "../responses/component-generic-response";
import {sdc2Config} from "../../../../main";
import {InstanceBePropertiesMap} from "../../../models/properties-inputs/property-fe-map";
import {API_QUERY_PARAMS} from "app/utils";
import {ComponentType, ServerTypeUrl} from "../../../utils/constants";
import {InterceptorService} from "ng2-interceptors/index";

declare var angular:angular.IAngularStatic;

@Injectable()
export class ComponentServiceNg2 {

    protected baseUrl;

    constructor(private http:InterceptorService) {
        this.baseUrl = sdc2Config.api.root + sdc2Config.api.component_api_root;
    }

    private getComponentDataByFieldsName(componentType:string, componentId: string, fields:Array<string>):Observable<ComponentGenericResponse> {

        let params:URLSearchParams = new URLSearchParams();
        _.forEach(fields, (field:string):void => {
            params.append(API_QUERY_PARAMS.INCLUDE, field);
        });

        return this.http.get(this.baseUrl + this.getServerTypeUrl(componentType) + componentId + '/filteredDataByParams', {search: params})
            .map((res:Response) => {
                return new ComponentGenericResponse().deserialize(res.json());
            }).do(error => console.log('server data:', error));
    }

    private getServerTypeUrl = (componentType:string):string => {
        switch (componentType) {
            case ComponentType.PRODUCT:
                return ServerTypeUrl.PRODUCTS;
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

    getComponentAttributes(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_ATTRIBUTES]);
    }

    getComponentInstancesAndRelation(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_RELATION, COMPONENT_FIELDS.COMPONENT_INSTANCES]);
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

    getCapabilitiesAndRequirements(componentType: string, componentId:string):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(componentType, componentId, [COMPONENT_FIELDS.COMPONENT_REQUIREMENTS, COMPONENT_FIELDS.COMPONENT_CAPABILITIES]);
    }

    getDeploymentGraphData(component:Component):Observable<ComponentGenericResponse> {
        return this.getComponentDataByFieldsName(component.componentType, component.uniqueId, [COMPONENT_FIELDS.COMPONENT_INSTANCES_RELATION, COMPONENT_FIELDS.COMPONENT_INSTANCES, COMPONENT_FIELDS.COMPONENT_GROUPS]);
    }

    createInput(component:Component, inputsToCreate:InstancePropertiesAPIMap):Observable<any> {
        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/create/inputs', inputsToCreate)
            .map(res => {
                return res.json();
            })
    }

    deleteInput(component:Component, input:PropertyBEModel):Observable<PropertyBEModel> {

        return this.http.delete(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/delete/' + input.uniqueId + '/input')
            .map((res:Response) => {
                return new PropertyBEModel(res.json());
            })
    }

    updateComponentInput(component:Component, input:PropertyBEModel):Observable<PropertyBEModel> {

        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/update/inputs', input)
            .map((res:Response) => {
                return new PropertyBEModel(res.json())
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

        // return {'ExtVL 0':[{definition: false,name:"network_assignments",password:false,required:true,type:"org.openecomp.datatypes.network.NetworkAssignments",uniqueId:"623cca1c-d605-4c9c-9f2b-935ec85ebcf8.network_assignments"},
        //     {definition: false,name: "exVL_naming",password: false,required: true,type: "org.openecomp.datatypes.Naming",uniqueId: "623cca1c-d605-4c9c-9f2b-935ec85ebcf8.exVL_naming"},
        //     {definition: false,name: "network_flows",password: false,required: false,type: "org.openecomp.datatypes.network.NetworkFlows",uniqueId: "623cca1c-d605-4c9c-9f2b-935ec85ebcf8.network_flows"},
        //     {definition: false,name: "provider_network",password: false,required: true,type: "org.openecomp.datatypes.network.ProviderNetwork",uniqueId: "623cca1c-d605-4c9c-9f2b-935ec85ebcf8.provider_network"},
        //     {definition: false,name: "network_homing",password: false,required: true,type: "org.openecomp.datatypes.EcompHoming",uniqueId: "623cca1c-d605-4c9c-9f2b-935ec85ebcf8.network_homing"}],
        //     'NetworkCP 0':[{definition: false,description: "identifies MAC address assignments to the CP",name: "mac_requirements",password: false,required: false,type: "org.openecomp.datatypes.network.MacRequirements",uniqueId: "26ec2bfd-b904-46c7-87ed-b32775120f2c.mac_requirements"}],
        //     'NetworkCP 1':[{definition: false,description: "identifies MAC address assignments to the CP",name: "mac_requirements",password: false,required: false,type: "org.openecomp.datatypes.network.MacRequirements",uniqueId: "26ec2bfd-b904-46c7-87ed-b32775120f2c.mac_requirements"}]};


    }
}

angular.module('Sdc.Services').factory('ComponentServiceNg2', downgradeInjectable(ComponentServiceNg2)); // This is in order to use the service in angular1 till we finish remove all angular1 code
