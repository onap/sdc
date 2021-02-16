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

import * as _ from "lodash";
import {SchemaAttribute, SchemaAttributeGroupModel} from '../../models';
import {PROPERTY_TYPES} from 'app/utils';
import {UUID} from "angular2-uuid";
import {AttributeBEModel, DerivedAttributeType} from "./attribute-be-model";
import {AttributeFEModel} from "./attribute-fe-model";

export class DerivedFEAttribute extends AttributeBEModel {
  valueObj: any;
  valueObjIsValid: boolean;
  valueObjOrig: any;
  valueObjIsChanged: boolean;
  parentName: string;
  attributesName: string;
  derivedDataType: DerivedAttributeType;
  isDeclared: boolean;
  isSelected: boolean;
  isDisabled: boolean;
  hidden: boolean;
  isChildOfListOrMap: boolean;
  canBeDeclared: boolean;
  mapKey: string;
  mapKeyError: string;

  constructor(attribute: AttributeBEModel, parentName?: string, createChildOfListOrMap?: boolean, key?: string, value?: any) {
    if (createChildOfListOrMap) { //creating a direct child of list or map (ie. Item that can be deleted, with UUID instead of name)
      super(null);
      this.isChildOfListOrMap = true;
      this.canBeDeclared = false;
      this.name = UUID.UUID();
      this.parentName = parentName;
      this.attributesName = parentName + '#' + this.name;

      if (attribute.type == PROPERTY_TYPES.LIST) {
        this.mapKey = attribute.schema.property.type.split('.').pop();
        this.mapKeyError = null;
        this.type = attribute.schema.property.type;
      } else { //map
        if (key) {
          this.mapKey = key;
          this.mapKeyError = null;
        } else {
          this.mapKey = '';
          this.mapKeyError = 'Key cannot be empty.';
        }
        this.type = attribute.type;
      }
      this.valueObj = (this.type == PROPERTY_TYPES.JSON && typeof value == 'object') ? JSON.stringify(value) : value;
      this.schema = new SchemaAttributeGroupModel(new SchemaAttribute(attribute.schema.property));
      this.updateValueObjOrig();
    } else { //creating a standard derived prop
      super(attribute);
      this.parentName = parentName ? parentName : null;
      this.attributesName = (parentName) ? parentName + '#' + attribute.name : attribute.name;
      this.canBeDeclared = true; //defaults to true
    }
    this.valueObjIsValid = true;
    this.derivedDataType = this.getDerivedAttributeType();
  }

  public getActualMapKey() {
    return (this.mapKeyError) ? this.name : this.mapKey;
  }

  public updateValueObj(valueObj: any, isValid: boolean) {
    this.valueObj = AttributeFEModel.cleanValueObj(valueObj);
    this.valueObjIsValid = isValid;
    this.valueObjIsChanged = this.hasValueObjChanged();
  }

  public updateValueObjOrig() {
    this.valueObjOrig = _.cloneDeep(this.valueObj);
    this.valueObjIsChanged = false;
  }

  public hasValueObjChanged() {
    return !_.isEqual(this.valueObj, this.valueObjOrig);
  }
}
