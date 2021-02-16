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

'use strict';
import * as _ from "lodash";
import {SchemaAttribute, SchemaAttributeGroupModel} from "./schema-attribute";
import {AttributeOutputDetail} from "app/models/attributes-outputs/attribute-output-detail";
import {AttributeBEModel} from "app/models/attributes-outputs/attribute-be-model";

export class AttributesGroup {
  constructor(attributesObj?: AttributesGroup) {
    _.forEach(attributesObj, (attributes: Array<AttributeModel>, instance) => {
      this[instance] = [];
      _.forEach(attributes, (attribute: AttributeModel): void => {
        attribute.resourceInstanceUniqueId = instance;
        attribute.readonly = true;
        this[instance].push(new AttributeModel(attribute));
      });
    });
  }
}

export interface IAttributeModel {

  //server data
  uniqueId: string;
  name: string;
  _default: string;
  description: string;
  type: string;
  schema: SchemaAttributeGroupModel;
  status: string;
  value: string;
  parentUniqueId: string;
  //custom data
  resourceInstanceUniqueId: string;
  readonly: boolean;
  valueUniqueUid: string;
}

export class AttributeModel extends AttributeBEModel implements IAttributeModel {

  //server data
  uniqueId: string;
  name: string;
  _default: string;
  description: string;
  type: string;
  schema: SchemaAttributeGroupModel;
  status: string;
  value: string;
  parentUniqueId: string;
  //custom data
  resourceInstanceUniqueId: string;
  readonly: boolean;
  valueUniqueUid: string;

  getOutputValues: AttributeOutputDetail[];
  subAttributeOutputPath: string;
  outputPath: string;

  constructor(attribute?: AttributeModel) {
    super(attribute);
    if (attribute) {
      this.uniqueId = attribute.uniqueId;
      this.name = attribute.name;
      this._default = attribute._default;
      this.description = attribute.description;
      this.type = attribute.type;
      this.status = attribute.status;
      this.schema = attribute.schema;
      this.value = attribute.value;
      this.parentUniqueId = attribute.parentUniqueId;
      this.resourceInstanceUniqueId = attribute.resourceInstanceUniqueId;
      this.readonly = attribute.readonly;
      this.valueUniqueUid = attribute.valueUniqueUid;

      this.getOutputValues = attribute.getOutputValues;
      this.subAttributeOutputPath = attribute.subAttributeOutputPath;
      this.outputPath = attribute.outputPath;
    } else {
      this._default = '';
    }

    if (!this.schema || !this.schema.property) {
      this.schema = new SchemaAttributeGroupModel(new SchemaAttribute());
    } else {
      //forcing creating new object, so editing different one than the object in the table
      this.schema = new SchemaAttributeGroupModel(new SchemaAttribute(this.schema.property));
    }

    this.convertValueToView();
  }

  public convertToServerObject(): string {
    if (this._default && this.type === 'map') {
      this._default = '{' + this._default + '}';
    }
    if (this._default && this.type === 'list') {
      this._default = '[' + this._default + ']';
    }
    this._default = this._default != "" && this._default != "[]" && this._default != "{}" ? this._default : null;

    return JSON.stringify(this);
  }


  public convertValueToView() {
    //unwrapping value {} or [] if type is complex
    if (this._default && (this.type === 'map' || this.type === 'list') &&
        ['[', '{'].indexOf(this._default.charAt(0)) > -1 &&
        [']', '}'].indexOf(this._default.slice(-1)) > -1) {
      this._default = this._default.slice(1, -1);
    }

    //also for value - for the modal in canvas
    if (this.value && (this.type === 'map' || this.type === 'list') &&
        ['[', '{'].indexOf(this.value.charAt(0)) > -1 &&
        [']', '}'].indexOf(this.value.slice(-1)) > -1) {
      this.value = this.value.slice(1, -1);
    }
  }

  public toJSON = (): any => {
    if (!this.resourceInstanceUniqueId) {
      this.value = undefined;
    }
    this.readonly = undefined;
    this.resourceInstanceUniqueId = undefined;
    return this;
  };
}
