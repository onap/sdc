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
import {PROPERTY_DATA, PROPERTY_TYPES} from 'app/utils';
import {
  AttributeBEModel,
  DerivedAttributeType
} from "app/models/attributes-outputs/attribute-be-model";
import {DerivedFEAttribute} from "app/models/attributes-outputs/derived-fe-attribute";

export class AttributeFEModel extends AttributeBEModel {

  expandedChildAttributeId: string;
  flattenedChildren: Array<DerivedFEAttribute>;
  isDeclared: boolean;
  isDisabled: boolean;
  isSelected: boolean;
  isSimpleType: boolean; //for convenience only - we can really just check if derivedDataType == derivedPropertyTypes.SIMPLE to know if the attrib is simple
  attributesName: string;
  uniqueId: string;
  valueObj: any; //this is the only value we relate to in the html templates
  valueObjValidation: any;
  valueObjIsValid: boolean;
  valueObjOrig: any; //this is valueObj representation as saved in server
  valueObjIsChanged: boolean;
  derivedDataType: DerivedAttributeType;
  origName: string;

  constructor(attribute: AttributeBEModel) {
    super(attribute);
    this.value = attribute.value ? attribute.value : attribute.defaultValue;//In FE if a attribute doesn't have value - display the default value
    this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
    this.setNonDeclared();
    this.derivedDataType = this.getDerivedAttributeType();
    this.flattenedChildren = [];
    this.attributesName = this.name;
    this.valueObj = null;
    this.updateValueObjOrig();
    this.resetValueObjValidation();
    this.origName = this.name;
  }


  public updateValueObj(valueObj: any, isValid: boolean) {
    this.valueObj = AttributeFEModel.cleanValueObj(valueObj);
    this.valueObjValidation = this.valueObjIsValid = isValid;
    this.valueObjIsChanged = this.hasValueObjChanged();
  }

  public updateValueObjOrig() {
    this.valueObjOrig = _.cloneDeep(this.valueObj);
    this.valueObjIsChanged = false;
  }

  public calculateValueObjIsValid(valueObjValidation?: any) {
    valueObjValidation = (valueObjValidation !== undefined) ? valueObjValidation : this.valueObjValidation;
    if (valueObjValidation instanceof Array) {
      return valueObjValidation.every((v) => this.calculateValueObjIsValid(v));
    } else if (valueObjValidation instanceof Object) {
      return Object.keys(valueObjValidation).every((k) => this.calculateValueObjIsValid(valueObjValidation[k]));
    }
    return Boolean(valueObjValidation);
  }

  public resetValueObjValidation() {
    if (this.derivedDataType === DerivedAttributeType.SIMPLE) {
      this.valueObjValidation = null;
    } else if (this.derivedDataType === DerivedAttributeType.LIST) {
      this.valueObjValidation = [];
    } else {
      this.valueObjValidation = {};
    }
    this.valueObjIsValid = true;
  }

  public getJSONValue = (): string => {
    return AttributeFEModel.stringifyValueObj(this.valueObj, this.schema.property.type, this.derivedDataType);
  }

  public getValueObj = (): any => {
    return AttributeFEModel.parseValueObj(this.value, this.type, this.derivedDataType, this.defaultValue);
  }

  public setNonDeclared = (childPath?: string): void => {
    if (!childPath) { //un-declaring a child attrib
      this.isDeclared = false;
    } else {
      let childProp: DerivedFEAttribute = this.flattenedChildren.find(child => child.attributesName == childPath);
      childProp.isDeclared = false;
    }
  }

  public setAsDeclared = (childNameToDeclare?: string): void => {
    if (!childNameToDeclare) { //declaring a child attrib
      this.isSelected = false;
      this.isDeclared = true;
    } else {
      let childProp: DerivedFEAttribute = this.flattenedChildren.find(child => child.attributesName == childNameToDeclare);
      if (!childProp) {
        console.log("ERROR: Unabled to find child: " + childNameToDeclare, this);
        return;
      }
      childProp.isSelected = false;
      childProp.isDeclared = true;
    }
  }

  //For expand-collapse functionality - used within HTML template
  public updateExpandedChildAttributeId = (childAttributeId: string): void => {
    if (childAttributeId.lastIndexOf('#') > -1) {
      this.expandedChildAttributeId = (this.expandedChildAttributeId == childAttributeId) ? (childAttributeId.substring(0, childAttributeId.lastIndexOf('#'))) : childAttributeId;
    } else {
      this.expandedChildAttributeId = this.name;
    }
  }

  public getIndexOfChild = (childPropName: string): number => {
    return this.flattenedChildren.findIndex(attrib => attrib.attributesName.indexOf(childPropName) === 0);
  }

  public getCountOfChildren = (childPropName: string): number => {
    let matchingChildren: Array<DerivedFEAttribute> = this.flattenedChildren.filter(attrib => attrib.attributesName.indexOf(childPropName) === 0) || [];
    return matchingChildren.length;
  }


  /* Updates parent valueObj when a child attrib's value has changed */
  public childPropUpdated = (childProp: DerivedFEAttribute): void => {
    let parentNames = this.getParentNamesArray(childProp.attributesName, []);
    if (parentNames.length) {
      const childPropName = parentNames.join('.');
      // unset value only if is null and valid, and not in a list
      if (childProp.valueObj === null && childProp.valueObjIsValid) {
        const parentChildProp = this.flattenedChildren.find((ch) => ch.attributesName === childProp.parentName) || this;
        if (parentChildProp.derivedDataType !== DerivedAttributeType.LIST) {
          _.unset(this.valueObj, childPropName);
          this.valueObj = AttributeFEModel.cleanValueObj(this.valueObj);
        } else {
          _.set(this.valueObj, childPropName, null);
        }
      } else {
        _.set(this.valueObj, childPropName, childProp.valueObj);
      }
      if (childProp.valueObjIsChanged) {
        _.set(this.valueObjValidation, childPropName, childProp.valueObjIsValid);
        this.valueObjIsValid = childProp.valueObjIsValid && this.calculateValueObjIsValid();
        this.valueObjIsChanged = true;
      } else {
        _.unset(this.valueObjValidation, childPropName);
        this.valueObjIsValid = this.calculateValueObjIsValid();
        this.valueObjIsChanged = this.hasValueObjChanged();
      }
    }
  };

  childPropMapKeyUpdated = (childProp: DerivedFEAttribute, newMapKey: string, forceValidate: boolean = false) => {
    if (!childProp.isChildOfListOrMap || childProp.derivedDataType !== DerivedAttributeType.MAP) {
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
        const newChildVal = _.get(this.valueObj, oldChildPropNames);
        if (newChildVal !== undefined) {
          _.set(this.valueObj, newChildPropNames, newChildVal);
          _.set(this.valueObjValidation, newChildPropNames, _.get(this.valueObjValidation, oldChildPropNames, childProp.valueObjIsValid));
        }
      }

      // remove map key from valueObj and valueObjValidation
      _.unset(this.valueObj, oldChildPropNames);
      _.unset(this.valueObjValidation, oldChildPropNames);

      // force validate after map key change
      forceValidate = true;
    }

    if (forceValidate) {
      // add custom entry for map key validation:
      const childMapKeyNames = childParentNames.concat(`%%KEY:${childProp.name}%%`);
      if (newActualMapKey) {
        _.set(this.valueObjValidation, childMapKeyNames, newMapKeyIsValid);
      } else {
        _.unset(this.valueObjValidation, childMapKeyNames);
      }

      this.valueObjIsValid = newMapKeyIsValid && this.calculateValueObjIsValid();
      this.valueObjIsChanged = this.hasValueObjChanged();
    }
  };

  /* Returns array of individual parents for given attrib path, with list/map UUIDs replaced with index/mapkey */
  public getParentNamesArray = (parentPropName: string, parentNames?: Array<string>, noHashKeys: boolean = false): Array<string> => {
    parentNames = parentNames || [];
    if (parentPropName.indexOf("#") == -1) {
      return parentNames;
    } //finished recursing parents. return

    let parentAttrib: DerivedFEAttribute = this.flattenedChildren.find(attrib => attrib.attributesName === parentPropName);
    let nameToInsert: string = parentAttrib.name;

    if (parentAttrib.isChildOfListOrMap) {
      if (!noHashKeys && parentAttrib.derivedDataType == DerivedAttributeType.MAP) {
        nameToInsert = parentAttrib.getActualMapKey();
      } else { //LIST
        let siblingProps = this.flattenedChildren.filter(attrib => attrib.parentName == parentAttrib.parentName).map(attrib => attrib.attributesName);
        nameToInsert = siblingProps.indexOf(parentAttrib.attributesName).toString();
      }
    }

    parentNames.splice(0, 0, nameToInsert); //add attrib name to array
    return this.getParentNamesArray(parentAttrib.parentName, parentNames, noHashKeys); //continue recursing
  }

  public hasValueObjChanged() {
    return !_.isEqual(this.valueObj, this.valueObjOrig);
  }

  static stringifyValueObj(valueObj: any, attributeType: PROPERTY_TYPES, derivedAttributeType: DerivedAttributeType): string {
    // if valueObj is null, return null
    if (valueObj === null || valueObj === undefined) {
      return null;
    }

    //If type is JSON, need to try parsing it before we stringify it so that it appears property in TOSCA - change per Bracha due to AMDOCS
    //TODO: handle this.derivedDataType == DerivedAttributeType.MAP
    if (derivedAttributeType == DerivedAttributeType.LIST && attributeType == PROPERTY_TYPES.JSON) {
      try {
        return JSON.stringify(valueObj.map(item => (typeof item == 'string') ? JSON.parse(item) : item));
      } catch (e) {
      }
    }

    // if type is anything but string, then stringify valueObj
    if ((typeof valueObj) !== 'string') {
      return JSON.stringify(valueObj);
    }

    // return string value as is
    return valueObj;
  }

  static parseValueObj(value: string, attributeType: PROPERTY_TYPES, derivedAttributeType: DerivedAttributeType, defaultValue?: string): any {
    let valueObj;
    if (derivedAttributeType === DerivedAttributeType.SIMPLE) {
      valueObj = value || defaultValue || null;  // use null for empty value object
      if (valueObj &&
          attributeType !== PROPERTY_TYPES.STRING &&
          attributeType !== PROPERTY_TYPES.JSON &&
          PROPERTY_DATA.SCALAR_TYPES.indexOf(<string>attributeType) == -1) {
        valueObj = JSON.parse(value);  // the value object contains the real value ans not the value as string
      }
    } else if (derivedAttributeType == DerivedAttributeType.LIST) {
      valueObj = _.merge([], JSON.parse(defaultValue || '[]'), JSON.parse(value || '[]'));  // value object should be merged value and default value. Value takes higher precedence. Set value object to empty obj if undefined.
    } else {
      valueObj = _.merge({}, JSON.parse(defaultValue || '{}'), JSON.parse(value || '{}'));  // value object should be merged value and default value. Value takes higher precedence. Set value object to empty obj if undefined.
    }
    return valueObj;
  }

  static cleanValueObj(valueObj: any, unsetEmpty?: boolean): any {
    // By default - unsetEmpty undefined - will make valueObj cleaned (no null or empty objects, but array will keep null or empty objects).
    if (valueObj === undefined || valueObj === null || valueObj === '') {
      return null;
    }
    if (valueObj instanceof Array) {
      const cleanArr = valueObj.map((v) => AttributeFEModel.cleanValueObj(v)).filter((v) => v !== null);
      valueObj.splice(0, valueObj.length, ...cleanArr)
    } else if (valueObj instanceof Object) {
      Object.keys(valueObj).forEach((k) => {
        // clean each item in the valueObj (by default, unset empty objects)
        valueObj[k] = AttributeFEModel.cleanValueObj(valueObj[k], unsetEmpty !== undefined ? unsetEmpty : true);
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
}
