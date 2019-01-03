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

import * as _ from "lodash";
import {DerivedFEProperty, PropertyFEModel} from "../../models";
import {PROPERTY_DATA} from "../../utils/constants";
import {InputBEModel} from "./input-be-model";
import {DerivedPropertyType} from "./property-be-model";

export class InputFEModel extends InputBEModel {
  expandedChildPropertyId: string;
  isSimpleType: boolean;
  relatedPropertyValue: any;
  relatedPropertyName: string;
  defaultValueObj: any;
  defaultValueObjIsValid: boolean;
  defaultValueObjOrig: any;
  defaultValueObjIsChanged: boolean;
  derivedDataType: DerivedPropertyType;

  flattenedChildren: Array<DerivedFEProperty>;

  constructor(input?: InputBEModel) {
    super(input);
    if (input) {
      this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
      let relatedProperty = input.properties && input.properties[0] || input.inputs && input.inputs[0];
      if (relatedProperty) {
        this.relatedPropertyValue = relatedProperty.value;
        this.relatedPropertyName = relatedProperty.name;
      }
      this.derivedDataType = this.getDerivedPropertyType();
      this.resetDefaultValueObjValidation();
      this.updateDefaultValueObjOrig();
    }
    this.flattenedChildren = [];
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

  /* Returns array of individual parents for given prop path, with list/map UUIDs replaced with index/mapkey */
  public getParentNamesArray = (parentPropName: string, parentNames?: Array<string>, noHashKeys: boolean = false): Array<string> => {
    parentNames = parentNames || [];
    if (parentPropName.indexOf("#") == -1) {
      return parentNames;
    } //finished recursing parents. return

    let parentProp: DerivedFEProperty = this.flattenedChildren.find(prop => prop.propertiesName === parentPropName);
    let nameToInsert: string = parentProp.name;

    if (parentProp.isChildOfListOrMap) {
      if (!noHashKeys && parentProp.derivedDataType == DerivedPropertyType.MAP) {
        nameToInsert = parentProp.getActualMapKey();
      } else { //LIST
        let siblingProps = this.flattenedChildren.filter(prop => prop.parentName == parentProp.parentName).map(prop => prop.propertiesName);
        nameToInsert = siblingProps.indexOf(parentProp.propertiesName).toString();
      }
    }

    parentNames.splice(0, 0, nameToInsert); //add prop name to array
    return this.getParentNamesArray(parentProp.parentName, parentNames, noHashKeys); //continue recursing
  }

  public getIndexOfChild = (childPropName: string): number => {
    return this.flattenedChildren.findIndex(prop => prop.propertiesName.indexOf(childPropName) === 0);
  }

  public getCountOfChildren = (childPropName: string): number => {
    let matchingChildren: Array<DerivedFEProperty> = this.flattenedChildren.filter(prop => prop.propertiesName.indexOf(childPropName) === 0) || [];
    return matchingChildren.length;
  }

  /* Updates parent valueObj when a child prop's value has changed */
  public childPropUpdated = (childProp: DerivedFEProperty): void => {
    let parentNames = this.getParentNamesArray(childProp.propertiesName, []);
    if (parentNames.length) {
      const childPropName = parentNames.join('.');
      // unset value only if is null and valid, and not in a list
      if (childProp.valueObj === null && childProp.valueObjIsValid) {
        const parentChildProp = this.flattenedChildren.find((ch) => ch.propertiesName === childProp.parentName) || this;
        if (parentChildProp.derivedDataType !== DerivedPropertyType.LIST) {
          _.unset(this.defaultValueObj, childPropName);
          this.defaultValueObj = InputFEModel.cleanValueObj(this.defaultValueObj);
        } else {
          _.set(this.defaultValueObj, childPropName, null);
        }
      } else {
        _.set(this.defaultValueObj, childPropName, childProp.valueObj);
      }
      if (childProp.valueObjIsChanged) {
        this.defaultValueObjIsValid = childProp.valueObjIsValid;
        this.defaultValueObjIsChanged = true;
      } else {
        this.defaultValueObjIsValid = true;
        this.defaultValueObjIsChanged = this.hasDefaultValueChanged();
      }
    }
  };

  static cleanValueObj(valueObj: any, unsetEmpty?: boolean): any {
    // By default - unsetEmpty undefined - will make valueObj cleaned (no null or empty objects, but array will keep null or empty objects).
    if (valueObj === undefined || valueObj === null || valueObj === '') {
      return null;
    }
    if (valueObj instanceof Array) {
      const cleanArr = valueObj.map((v) => InputFEModel.cleanValueObj(v)).filter((v) => v !== null);
      valueObj.splice(0, valueObj.length, ...cleanArr)
    } else if (valueObj instanceof Object) {
      Object.keys(valueObj).forEach((k) => {
        // clean each item in the valueObj (by default, unset empty objects)
        valueObj[k] = InputFEModel.cleanValueObj(valueObj[k], unsetEmpty !== undefined ? unsetEmpty : true);
        if (valueObj[k] === null) {
          delete valueObj[k];
        }
      });
      // if unsetEmpty flag is true and valueObj is empty
      if (unsetEmpty && !Object.keys(valueObj).length) {
        return null;
      }
    }
    return valueObj;
  }

  childPropMapKeyUpdated = (childProp: DerivedFEProperty, newMapKey: string, forceValidate: boolean = false) => {
    if (!childProp.isChildOfListOrMap || childProp.derivedDataType !== DerivedPropertyType.MAP) {
      return;
    }

    const childParentNames = this.getParentNamesArray(childProp.parentName);
    const oldActualMapKey = childProp.getActualMapKey();

    childProp.mapKey = newMapKey;
    if (childProp.mapKey === null) {  // null -> remove map key
      childProp.mapKeyError = null;
    } else if (!childProp.mapKey) {
      childProp.mapKeyError = 'Key cannot be empty.';
    } else if (this.flattenedChildren
    .filter((fch) => fch !== childProp && fch.parentName === childProp.parentName)  // filter sibling child props
    .map((fch) => fch.mapKey)
    .indexOf(childProp.mapKey) !== -1) {
      childProp.mapKeyError = 'This key already exists.';
    } else {
      childProp.mapKeyError = null;
    }
    const newActualMapKey = childProp.getActualMapKey();
    const newMapKeyIsValid = !childProp.mapKeyError;

    // if mapKey was changed, then replace the old key with the new one
    if (newActualMapKey !== oldActualMapKey) {
      const oldChildPropNames = childParentNames.concat([oldActualMapKey]);
      const newChildPropNames = (newActualMapKey) ? childParentNames.concat([newActualMapKey]) : null;

      // add map key to valueObj and valueObjValidation
      if (newChildPropNames) {
        const newChildVal = _.get(this.defaultValueObj, oldChildPropNames);
        if (newChildVal !== undefined) {
          _.set(this.defaultValueObj, newChildPropNames, newChildVal);
        }
      }

      // remove map key from valueObj and valueObjValidation
      _.unset(this.defaultValueObj, oldChildPropNames);

      // force validate after map key change
      forceValidate = true;
    }

    if (forceValidate) {
      this.defaultValueObjIsValid = newMapKeyIsValid;
      this.defaultValueObjIsChanged = this.hasDefaultValueChanged();
    }
  };

  //For expand-collapse functionality - used within HTML template
  public updateExpandedChildPropertyId = (childPropertyId: string): void => {
    if (childPropertyId.lastIndexOf('#') > -1) {
      this.expandedChildPropertyId = (this.expandedChildPropertyId == childPropertyId) ? (childPropertyId.substring(0, childPropertyId.lastIndexOf('#'))) : childPropertyId;
    } else {
      this.expandedChildPropertyId = this.name;
    }
  }

  hasDefaultValueChanged(): boolean {
    return !_.isEqual(this.defaultValueObj, this.defaultValueObjOrig);
  }

}