/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 Nordix Foundation. All rights reserved.
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

import {PROPERTY_DATA, PROPERTY_TYPES} from 'app/utils/constants';
import {ToscaPresentationData} from '../tosca-presentation';
import {AttributeOutputDetail} from "app/models/attributes-outputs/attribute-output-detail";
import {SchemaAttribute, SchemaAttributeGroupModel} from "../schema-attribute";

export enum DerivedAttributeType {
  SIMPLE,
  LIST,
  MAP,
  COMPLEX
}

export class AttributeBEModel {

  constraints: any[];
  defaultValue: string;
  definition: boolean;
  description: string;
  fromDerived: boolean;
  getOutputValues: AttributeOutputDetail[];
  name: string;
  origName: string;
  parentUniqueId: string;
  password: boolean;
  required: boolean;
  schema: SchemaAttributeGroupModel;
  schemaType: string;
  type: string;
  uniqueId: string;
  value: string;
  parentAttributeType: string;
  subAttributeOutputPath: string;
  outputPath: string;
  toscaPresentation: ToscaPresentationData;

  constructor(attribute?: AttributeBEModel) {
    if (attribute) {
      this.constraints = attribute.constraints;
      this.defaultValue = attribute.defaultValue;
      this.description = attribute.description;
      this.fromDerived = attribute.fromDerived;
      this.name = attribute.name;
      this.origName = attribute.origName;
      this.parentUniqueId = attribute.parentUniqueId;
      this.password = attribute.password;
      this.required = attribute.required;
      this.schema = attribute.schema;
      this.schemaType = attribute.schemaType;
      this.type = attribute.type;
      this.uniqueId = attribute.uniqueId;
      this.value = attribute.value;
      this.definition = attribute.definition;
      this.getOutputValues = attribute.getOutputValues;
      this.parentAttributeType = attribute.parentAttributeType;
      this.subAttributeOutputPath = attribute.subAttributeOutputPath;
      this.toscaPresentation = attribute.toscaPresentation;
      this.outputPath = attribute.outputPath;
    }

    if (!this.schema || !this.schema.property) {
      this.schema = new SchemaAttributeGroupModel(new SchemaAttribute());
    } else { // forcing creating new object, so editing different one than the object in the table
      this.schema = new SchemaAttributeGroupModel(new SchemaAttribute(this.schema.property));
    }
  }

  public toJSON = (): any => {
    const temp = angular.copy(this);
    temp.value = temp.value === '{}' || temp.value === '[]' ? undefined : temp.value;
    temp.defaultValue = temp.defaultValue === '{}' || temp.defaultValue === '[]' ? undefined : temp.defaultValue;
    return temp;
  }

  public getDerivedAttributeType = () => {
    if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1) {
      return DerivedAttributeType.SIMPLE;
    } else if (this.type === PROPERTY_TYPES.LIST) {
      return DerivedAttributeType.LIST;
    } else if (this.type === PROPERTY_TYPES.MAP) {
      return DerivedAttributeType.MAP;
    } else {
      return DerivedAttributeType.COMPLEX;
    }
  }
}
