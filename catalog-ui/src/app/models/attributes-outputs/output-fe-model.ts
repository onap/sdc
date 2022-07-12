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

import * as _ from 'lodash';
import { PROPERTY_DATA } from '../../utils/constants';
import { DerivedAttributeType } from './attribute-be-model';
import { AttributeFEModel } from './attribute-fe-model';
import { OutputBEModel } from './output-be-model';

export class OutputFEModel extends OutputBEModel {
  isSimpleType: boolean;
  relatedAttributeValue: any;
  relatedAttributeName: string;
  defaultValueObj: any;
  defaultValueObjIsValid: boolean;
  defaultValueObjOrig: any;
  defaultValueObjIsChanged: boolean;
  derivedDataType: DerivedAttributeType;

  constructor(output?: OutputBEModel) {
    super(output);
    if (output) {
      this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
      const relatedAttribute = output.attribute;
      if (relatedAttribute) {
        this.relatedAttributeValue = relatedAttribute.value;
        this.relatedAttributeName = relatedAttribute.name;
      }
      this.derivedDataType = this.getDerivedAttributeType();
      this.resetDefaultValueObjValidation();
      this.updateDefaultValueObjOrig();
    }
  }

  public updateDefaultValueObjOrig() {
    this.defaultValueObjOrig = _.cloneDeep(this.defaultValueObj);
    this.defaultValueObjIsChanged = false;
  }

  public getJSONDefaultValue(): string {
    return AttributeFEModel.stringifyValueObj(this.defaultValueObj, this.schema.property.type, this.derivedDataType);
  }

  public getDefaultValueObj(): any {
    return AttributeFEModel.parseValueObj(this.defaultValueObj, this.type, this.derivedDataType);
  }

  public resetDefaultValueObjValidation() {
    this.defaultValueObjIsValid = true;
  }

  hasDefaultValueChanged(): boolean {
    return !_.isEqual(this.defaultValueObj, this.defaultValueObjOrig);
  }

  hasChanged(): boolean {
    return this.hasDefaultValueChanged();
  }
}
