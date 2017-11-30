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

import {SchemaPropertyGroupModel, SchemaProperty} from '../aschema-property';
import { PROPERTY_DATA, PROPERTY_TYPES } from 'app/utils';
import { FilterPropertiesAssignmentData, PropertyBEModel, DerivedPropertyType, DerivedFEPropertyMap, DerivedFEProperty } from 'app/models';


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
    derivedDataType: DerivedPropertyType;

    constructor(property: PropertyBEModel){
        super(property);
        this.value = property.value ? property.value : property.defaultValue;//In FE if a property doesn't have value - display the default value
        this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
        this.setNonDeclared();
        this.derivedDataType = this.getDerivedPropertyType();
        this.flattenedChildren = [];
        this.propertiesName = this.name;
    }


    public getJSONValue = (): string => {
        //If type is JSON, need to try parsing it before we stringify it so that it appears property in TOSCA - change per Bracha due to AMDOCS
        //TODO: handle this.derivedDataType == DerivedPropertyType.MAP
        if (this.derivedDataType == DerivedPropertyType.LIST && this.schema.property.type == PROPERTY_TYPES.JSON) {
            try {
                return JSON.stringify(this.valueObj.map(item => (typeof item == 'string')? JSON.parse(item) : item));
            } catch (e){}
        }

        return (this.derivedDataType == DerivedPropertyType.SIMPLE) ? this.valueObj : JSON.stringify(this.valueObj);
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
            _.set(this.valueObj, parentNames.join('.'), childProp.valueObj);
        }
    };

    /* Returns array of individual parents for given prop path, with list/map UUIDs replaced with index/mapkey */
    public getParentNamesArray = (parentPropName: string, parentNames?: Array<string>): Array<string> => {
        if (parentPropName.indexOf("#") == -1) { return parentNames; } //finished recursing parents. return

        let parentProp: DerivedFEProperty = this.flattenedChildren.find(prop => prop.propertiesName === parentPropName);
        let nameToInsert: string = parentProp.name;

        if (parentProp.isChildOfListOrMap) {
            if (parentProp.derivedDataType == DerivedPropertyType.MAP) {
                nameToInsert = parentProp.mapKey;
            } else { //LIST
                let siblingProps = this.flattenedChildren.filter(prop => prop.parentName == parentProp.parentName).map(prop => prop.propertiesName);
                nameToInsert = siblingProps.indexOf(parentProp.propertiesName).toString();
            }
        }

        parentNames.splice(0, 0, nameToInsert); //add prop name to array
        return this.getParentNamesArray(parentProp.parentName, parentNames); //continue recursing
    }


}
