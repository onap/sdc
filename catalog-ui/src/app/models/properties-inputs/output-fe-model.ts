/*
* ============LICENSE_START=======================================================
*  Copyright (C) 2020 Nordix Foundation. All rights reserved.
*  ================================================================================
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  SPDX-License-Identifier: Apache-2.0
*  ============LICENSE_END=========================================================
*/

import * as _ from "lodash";
import {PropertyFEModel} from "../../models";
import {PROPERTY_DATA} from "../../utils/constants";
import {DerivedPropertyType} from "./property-be-model";
import {OutputBEModel} from "./output-be-model";

export class OutputFeModel extends OutputBEModel {
  isSimpleType: boolean;
  relatedPropertyValue: any;
  relatedPropertyName: string;
  defaultValueObj: any;
  defaultValueObjIsValid: boolean;
  defaultValueObjOrig: any;
  defaultValueObjIsChanged: boolean;
  derivedDataType: DerivedPropertyType;
  requiredOrig: boolean;

  constructor(output?: OutputBEModel) {
    super(output);
    if (output) {
      this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
      let relatedProperty = output.properties && output.properties[0] || output.outputs && output.outputs[0];
      if (relatedProperty) {
        this.relatedPropertyValue = relatedProperty.value;
        this.relatedPropertyName = relatedProperty.name;
      }
      this.derivedDataType = this.getDerivedPropertyType();
      this.resetDefaultValueObjValidation();
      this.updateDefaultValueObjOrig();

      this.requiredOrig = this.required;
    }
  }

  public updateDefaultValueObj(defaultValueObj: any, isValid: boolean) {
    this.defaultValueObj = PropertyFEModel.cleanValueObj(defaultValueObj);
    this.defaultValueObjIsValid = isValid;
    this.defaultValueObjIsChanged = this.hasDefaultValueChanged();
  }

  public updateDefaultValueObjOrig() {
    this.defaultValueObjOrig = _.cloneDeep(this.defaultValueObj);
    this.defaultValueObjIsChanged = false;
  }

  public getJSONDefaultValue(): string {
    return PropertyFEModel.stringifyValueObj(this.defaultValueObj, this.schema.property.type, this.derivedDataType);
  }

  public getDefaultValueObj(): any {
    return PropertyFEModel.parseValueObj(this.defaultValue, this.type, this.derivedDataType);
  }

  public resetDefaultValueObjValidation() {
    this.defaultValueObjIsValid = true;
  }

  hasDefaultValueChanged(): boolean {
    return !_.isEqual(this.defaultValueObj, this.defaultValueObjOrig);
  }

  hasRequiredChanged(): boolean {
    return this.required !== this.requiredOrig;
  }

  hasChanged(): boolean {
    return this.hasDefaultValueChanged() || this.hasRequiredChanged();
  }
}
