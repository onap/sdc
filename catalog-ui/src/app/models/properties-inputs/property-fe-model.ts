/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2020 Nokia
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
import {DerivedFEProperty, DerivedPropertyType, PropertyBEModel} from 'app/models';
import * as jsYaml from 'js-yaml';


export class PropertyFEModel extends PropertyBEModel {

    expandedChildPropertyId: string;
    flattenedChildren:  Array<DerivedFEProperty>;
    isDeclared: boolean;
    isDisabled: boolean;
    isSelected: boolean;
    isSimpleType: boolean; //for convenience only - we can really just check if derivedDataType == derivedPropertyTypes.SIMPLE to know if the prop is simple
    propertiesName: string;
    uniqueId: string;
    valueObj: any; //this is the only value we relate to in the html templates
    valueObjValidation: any;
    valueObjIsValid: boolean;
    valueObjOrig: any; //this is valueObj representation as saved in server
    valueObjIsChanged: boolean;
    derivedDataType: DerivedPropertyType;
    origName: string;

    constructor(property: PropertyBEModel){
        super(property);
        this.value = property.value ? property.value : property.defaultValue;//In FE if a property doesn't have value - display the default value
        this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
        this.setNonDeclared();
        this.derivedDataType = this.getDerivedPropertyType();
        this.flattenedChildren = [];
        this.propertiesName = this.name;
        this.valueObj = null;
        this.updateValueObjOrig();
        this.resetValueObjValidation();
        this.origName = this.name;
    }


    public updateValueObj(valueObj:any, isValid:boolean) {
        this.valueObj = PropertyFEModel.cleanValueObj(valueObj);
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
        if (this.derivedDataType === DerivedPropertyType.SIMPLE) {
            this.valueObjValidation = null;
        } else if (this.derivedDataType === DerivedPropertyType.LIST) {
            this.valueObjValidation = [];
        } else {
            this.valueObjValidation = {};
        }
        this.valueObjIsValid = true;
    }

    public getJSONValue = (): string => {
        return PropertyFEModel.stringifyValueObj(this.valueObj, this.schema.property.type, this.derivedDataType);
    }

    public getValueObj = (): any => {
        return PropertyFEModel.parseValueObj(this.value, this.type, this.derivedDataType, this.isToscaFunction(), this.defaultValue);
    }

    public setNonDeclared = (childPath?: string): void => {
        if (!childPath) { //un-declaring a child prop
            this.isDeclared = false;
        } else {
            let childProp: DerivedFEProperty = this.flattenedChildren.find(child => child.propertiesName == childPath);
            childProp.isDeclared = false;
        }
    }

    public setAsDeclared = (childNameToDeclare?:string): void => {
        if (!childNameToDeclare) { //declaring a child prop
            this.isSelected = false;
            this.isDeclared = true;
        } else {
            let childProp: DerivedFEProperty = this.flattenedChildren.find(child => child.propertiesName == childNameToDeclare);
            if (!childProp) { console.log("ERROR: Unabled to find child: " + childNameToDeclare, this); return; }
            childProp.isSelected = false;
            childProp.isDeclared = true;
        }
    }

    //For expand-collapse functionality - used within HTML template
    public updateExpandedChildPropertyId = (childPropertyId: string): void => {
        if (childPropertyId.lastIndexOf('#') > -1) {
            this.expandedChildPropertyId = (this.expandedChildPropertyId == childPropertyId) ? (childPropertyId.substring(0, childPropertyId.lastIndexOf('#'))) : childPropertyId;
        } else {
            this.expandedChildPropertyId = this.name;
        }
    }

    public getIndexOfChild = (childPropName: string): number => {
        return this.flattenedChildren.findIndex(prop => prop.propertiesName.indexOf(childPropName) === 0);
    }

    public getCountOfChildren = (childPropName: string):number => {
        let matchingChildren:Array<DerivedFEProperty> = this.flattenedChildren.filter(prop => prop.propertiesName.indexOf(childPropName) === 0) || [];
        return matchingChildren.length;
    }

    // public getListIndexOfChild = (childPropName: string): number => { //gets list of siblings and then the index within that list
    //     this.flattenedChildren.filter(prop => prop.parentName == item.parentName).map(prop => prop.propertiesName).indexOf(item.propertiesName)
    // }

    /* Updates parent valueObj when a child prop's value has changed */
    public childPropUpdated = (childProp: DerivedFEProperty): void => {
        let parentNames = this.getParentNamesArray(childProp.propertiesName, []);
        if (parentNames.length) {
            const childPropName = parentNames.join('.');
            // unset value only if is null and valid, and not in a list
            if (childProp.valueObj === null && childProp.valueObjIsValid) {
                const parentChildProp = this.flattenedChildren.find((ch) => ch.propertiesName === childProp.parentName) || this;
                if (parentChildProp.derivedDataType !== DerivedPropertyType.LIST) {
                    _.unset(this.valueObj, childPropName);
                    this.valueObj = PropertyFEModel.cleanValueObj(this.valueObj);
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

    /* Returns array of individual parents for given prop path, with list/map UUIDs replaced with index/mapkey */
    public getParentNamesArray = (parentPropName: string, parentNames?: Array<string>, noHashKeys:boolean = false): Array<string> => {
        parentNames = parentNames || [];
        if (parentPropName.indexOf("#") == -1) { return parentNames; } //finished recursing parents. return

        let parentProp: DerivedFEProperty = this.flattenedChildren.find(prop => prop.propertiesName === parentPropName);
        let nameToInsert: string = parentProp.name;

        if (parentProp.isChildOfListOrMap) {
            if (!noHashKeys && parentProp.derivedDataType == DerivedPropertyType.MAP && !parentProp.mapInlist) {
                nameToInsert = parentProp.getActualMapKey();
            } else { //LIST
                let siblingProps = this.flattenedChildren.filter(prop => prop.parentName == parentProp.parentName).map(prop => prop.propertiesName);
                nameToInsert = siblingProps.indexOf(parentProp.propertiesName).toString();
            }
        }

        parentNames.splice(0, 0, nameToInsert); //add prop name to array
        return this.getParentNamesArray(parentProp.parentName, parentNames, noHashKeys); //continue recursing
    }

    public hasValueObjChanged() {
        return !_.isEqual(this.valueObj, this.valueObjOrig);
    }

    static stringifyValueObj(valueObj: any, propertyType: PROPERTY_TYPES, propertyDerivedType: DerivedPropertyType): string {
        // if valueObj is null, return null
        if (valueObj === null || valueObj === undefined) {
            return null;
        }

        //If type is JSON, need to try parsing it before we stringify it so that it appears property in TOSCA - change per Bracha due to AMDOCS
        //TODO: handle this.derivedDataType == DerivedPropertyType.MAP
        if (propertyDerivedType == DerivedPropertyType.LIST && propertyType == PROPERTY_TYPES.JSON) {
            try {
                return JSON.stringify(valueObj.map(item => (typeof item == 'string') ? JSON.parse(item) : item));
            } catch (e){}
        }

        // if type is anything but string, then stringify valueObj
        if ((typeof valueObj) !== 'string') {
            return JSON.stringify(valueObj);
        }

        // return trimmed string value
        return valueObj.trim();
    }

    static parseValueObj(value: string, propertyType: PROPERTY_TYPES, propertyDerivedType: DerivedPropertyType, isToscaFunction: boolean,
                         defaultValue?: string): any {
        if (isToscaFunction) {
            return jsYaml.load(value);
        }
        if (propertyDerivedType === DerivedPropertyType.SIMPLE) {
            const valueObj = value || defaultValue || null;  // use null for empty value object
            if (valueObj &&
                propertyType !== PROPERTY_TYPES.STRING &&
                propertyType !== PROPERTY_TYPES.TIMESTAMP &&
                propertyType !== PROPERTY_TYPES.JSON &&
                PROPERTY_DATA.SCALAR_TYPES.indexOf(<string>propertyType) == -1) {
                return JSON.parse(value);  // the value object contains the real value ans not the value as string
            }
            return valueObj;
        }
        if (propertyDerivedType == DerivedPropertyType.LIST) {
            return _.merge([], JSON.parse(defaultValue || '[]'), JSON.parse(value || '[]'));  // value object should be merged value and default value. Value takes higher precedence. Set value object to empty obj if undefined.
        }

        return _.merge({}, JSON.parse(defaultValue || '{}'), JSON.parse(value || '{}'));  // value object should be merged value and default value. Value takes higher precedence. Set value object to empty obj if undefined.
    };

    static cleanValueObj(valueObj: any, unsetEmpty?: boolean): any {
        // By default - unsetEmpty undefined - will make valueObj cleaned (no null or empty objects, but array will keep null or empty objects).
        if (valueObj === undefined || valueObj === null || valueObj === '') {
            return null;
        }
        if (valueObj instanceof Array) {
            const cleanArr = valueObj.map((v) => PropertyFEModel.cleanValueObj(v)).filter((v) => v !== null);
            valueObj.splice(0, valueObj.length, ...cleanArr)
        } else if (valueObj instanceof Object) {
            Object.keys(valueObj).forEach((k) => {
                // clean each item in the valueObj (by default, unset empty objects)
                valueObj[k] = PropertyFEModel.cleanValueObj(valueObj[k], unsetEmpty !== undefined ? unsetEmpty : true);
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
