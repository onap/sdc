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

import { PROPERTY_DATA, PROPERTY_TYPES } from 'app/utils/constants';
import { SchemaProperty, SchemaPropertyGroupModel } from '../aschema-property';
import { ToscaPresentationData } from '../tosca-presentation';
import { PropertyInputDetail } from './property-input-detail';

export enum DerivedPropertyType {
    SIMPLE,
    LIST,
    MAP,
    COMPLEX
}
export class PropertyPolicyDetail {
    policyId: string;
    propertyName: string;
    constructor(propertyPolicy?: PropertyPolicyDetail) {
        if(propertyPolicy) {
            this.policyId = propertyPolicy.policyId;
            this.propertyName = propertyPolicy.propertyName;
        }
    }
}

export class PropertyBEModel {

    constraints: any[];
    defaultValue: string;
    definition: boolean;
    description: string;
    fromDerived: boolean;
    getInputValues: PropertyInputDetail[];
    getPolicyValues: PropertyPolicyDetail[];
    name: string;
    origName: string;
    parentUniqueId: string;
    password: boolean;
    required: boolean;
    schema: SchemaPropertyGroupModel;
    schemaType: string;
    type: string;
    uniqueId: string;
    value: string;
    parentPropertyType: string;
    subPropertyInputPath: string;
    inputPath: string;
    toscaPresentation: ToscaPresentationData;

    constructor(property?: PropertyBEModel) {
        if (property) {
            this.constraints = property.constraints;
            this.defaultValue = property.defaultValue;
            this.description = property.description;
            this.fromDerived = property.fromDerived;
            this.name = property.name;
            this.origName = property.origName;
            this.parentUniqueId = property.parentUniqueId;
            this.password = property.password;
            this.required = property.required;
            this.schema = property.schema;
            this.schemaType = property.schemaType;
            this.type = property.type;
            this.uniqueId = property.uniqueId;
            this.value = property.value;
            this.definition = property.definition;
            this.getInputValues = property.getInputValues;
            this.parentPropertyType = property.parentPropertyType;
            this.subPropertyInputPath = property.subPropertyInputPath;
            this.toscaPresentation = property.toscaPresentation;
            this.getPolicyValues = property.getPolicyValues;
            this.inputPath = property.inputPath;
        }

        if (!this.schema || !this.schema.property) {
            this.schema = new SchemaPropertyGroupModel(new SchemaProperty());
        } else { // forcing creating new object, so editing different one than the object in the table
            this.schema = new SchemaPropertyGroupModel(new SchemaProperty(this.schema.property));
        }
    }

    public toJSON = (): any => {
        const temp = angular.copy(this);
        temp.value = temp.value === '{}' || temp.value === '[]' ? undefined : temp.value;
        temp.defaultValue = temp.defaultValue === '{}' || temp.defaultValue === '[]' ? undefined : temp.defaultValue;
        return temp;
    }

    public getDerivedPropertyType = () => {
        if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1) {
            return DerivedPropertyType.SIMPLE;
        } else if (this.type === PROPERTY_TYPES.LIST) {
            return DerivedPropertyType.LIST;
        } else if (this.type === PROPERTY_TYPES.MAP) {
            return DerivedPropertyType.MAP;
        } else {
            return DerivedPropertyType.COMPLEX;
        }
    }
}

