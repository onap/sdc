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

/**
 * Created by ob0695 on 4/18/2017.
 */

import { ArtifactGroupModel, PropertyModel, PropertiesGroup, AttributeModel, AttributesGroup, ComponentInstance,
    InputBEModel, Module, ComponentMetadata, RelationshipModel, RequirementsGroup, CapabilitiesGroup,InputFEModel} from "app/models";
import {CommonUtils} from "app/utils";
import {Serializable} from "../utils/serializable";
import {PropertyBEModel} from "../../../models/properties-inputs/property-be-model";

export class ComponentGenericResponse  implements Serializable<ComponentGenericResponse> {

    public metadata: ComponentMetadata;
    public deploymentArtifacts:ArtifactGroupModel;
    public artifacts:ArtifactGroupModel;
    public toscaArtifacts:ArtifactGroupModel;
    public componentInstancesProperties:PropertiesGroup;
    public componentInstancesAttributes:AttributesGroup;
    public componentInstancesRelations:Array<RelationshipModel>;
    public componentInstances:Array<ComponentInstance>;
    public inputs:Array<InputBEModel>;
    public capabilities:CapabilitiesGroup;
    public requirements:RequirementsGroup;
    public properties:Array<PropertyModel>;
    public attributes:Array<AttributeModel>;
    public groups:Array<Module>;
    public interfaces:any;
    public additionalInformation:any;
    public derivedList:Array<any>;

    deserialize (response): ComponentGenericResponse {

        if(response.componentInstancesProperties) {
            this.componentInstancesProperties = new PropertiesGroup(response.componentInstancesProperties);
        }
        if(response.componentInstancesAttributes) {
            this.componentInstancesAttributes = new AttributesGroup(response.componentInstancesAttributes);
        }
        if(response.componentInstances) {
            this.componentInstances = CommonUtils.initComponentInstances(response.componentInstances);
        }
        if(response.componentInstancesRelations) {
            this.componentInstancesRelations = CommonUtils.initComponentInstanceRelations(response.componentInstancesRelations);
        }
        if(response.deploymentArtifacts) {
            this.deploymentArtifacts = new ArtifactGroupModel(response.deploymentArtifacts);
        }
        if(response.inputs) {
            this.inputs = CommonUtils.initInputs(response.inputs);
        }
        if(response.attributes) {
            this.attributes = CommonUtils.initAttributes(response.attributes);
        }
        if(response.artifacts) {
            this.artifacts = new ArtifactGroupModel(response.artifacts);
        }
        if(response.properties) {
            this.properties = CommonUtils.initProperties(response.properties);
        }
        if(response.capabilities) {
            this.capabilities = new CapabilitiesGroup(response.capabilities);
        }
        if(response.requirements) {
            this.requirements = new RequirementsGroup(response.requirements);
        }
        if(response.toscaArtifacts) {
            this.toscaArtifacts = new ArtifactGroupModel(response.toscaArtifacts);
        }
        if(response.metadata) {
            this.metadata = new ComponentMetadata().deserialize(response.metadata);
        }
        if(response.groups) {
            this.groups = CommonUtils.initModules(response.groups);
        }
        return this;
    }
}
