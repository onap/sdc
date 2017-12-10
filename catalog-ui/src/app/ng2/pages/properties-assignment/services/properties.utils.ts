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

import { Injectable } from '@angular/core';
import { DataTypeModel, PropertyFEModel, PropertyBEModel, InstanceBePropertiesMap, InstanceFePropertiesMap, DerivedFEProperty,  DerivedPropertyType, InputFEModel} from "app/models";
import { DataTypeService } from "app/ng2/services/data-type.service";
import { PropertiesService } from "app/ng2/services/properties.service";
import { PROPERTY_TYPES, PROPERTY_DATA } from "app/utils";

@Injectable()
export class PropertiesUtils {

    constructor(private dataTypeService:DataTypeService, private propertiesService: PropertiesService) {}

    /**
     * Entry point when getting properties from server
     * For each instance, loop through each property, and:
     * 1. Create flattened children
     * 2. Check against inputs to see if any props are declared and disable them
     * 3. Initialize valueObj (which also creates any new list/map flattened children as needed)
     * Returns InstanceFePropertiesMap
     */
    public convertPropertiesMapToFEAndCreateChildren = (instancePropertiesMap:InstanceBePropertiesMap, isVF:boolean, inputs?:Array<InputFEModel>): InstanceFePropertiesMap => {
        let instanceFePropertiesMap:InstanceFePropertiesMap = new InstanceFePropertiesMap();
        angular.forEach(instancePropertiesMap, (properties:Array<PropertyBEModel>, instanceId:string) => {
            let propertyFeArray: Array<PropertyFEModel> = [];
            _.forEach(properties, (property: PropertyBEModel) => {

                if (this.dataTypeService.getDataTypeByTypeName(property.type)) { // if type not exist in data types remove property from list

                    let newFEProp: PropertyFEModel = new PropertyFEModel(property); //Convert property to FE

                    if (newFEProp.derivedDataType == DerivedPropertyType.COMPLEX) { //Create children if prop is not simple, list, or map.
                        newFEProp.flattenedChildren = this.createFlattenedChildren(newFEProp.type, newFEProp.name);
                    }
                    if (newFEProp.getInputValues && newFEProp.getInputValues.length) { //if this prop (or any children) are declared, set isDeclared and disable checkbox on parents/children
                        newFEProp.getInputValues.forEach(propInputDetail => {
                            let inputPath = propInputDetail.inputPath;
                            if (!inputPath) { //TODO: this is a workaround until Marina adds inputPath
                                let input = inputs.find(input => input.uniqueId == propInputDetail.inputId);
                                if (!input) { console.log("CANNOT FIND INPUT FOR " + propInputDetail.inputId); return; }
                                else inputPath = input.inputPath;
                            }
                            if (inputPath == newFEProp.name) inputPath = undefined; // if not complex we need to remove the inputPath from FEModel so we not look for a child
                            newFEProp.setAsDeclared(inputPath); //if a path is sent, its a child prop. this param is optional
                            this.propertiesService.disableRelatedProperties(newFEProp, inputPath);
                        });
                    }
                    this.initValueObjectRef(newFEProp); //initialize valueObj.
                    propertyFeArray.push(newFEProp);
                    newFEProp.updateExpandedChildPropertyId(newFEProp.name); //display only the first level of children
                    this.dataTypeService.checkForCustomBehavior(newFEProp);
                }
            });
            instanceFePropertiesMap[instanceId] = propertyFeArray;

        });
        return instanceFePropertiesMap;
    }

    public createListOrMapChildren = (property:PropertyFEModel | DerivedFEProperty, key: string, valueObj: any): Array<DerivedFEProperty> => {
        let newProps: Array<DerivedFEProperty> = [];
        let parentProp = new DerivedFEProperty(property, property.propertiesName, true, key, valueObj);
        newProps.push(parentProp);

        if (!property.schema.property.isSimpleType) {
            let additionalChildren:Array<DerivedFEProperty> = this.createFlattenedChildren(property.schema.property.type, parentProp.propertiesName);
            this.assignFlattenedChildrenValues(parentProp.valueObj, additionalChildren, parentProp.propertiesName);
            additionalChildren.forEach(prop => prop.canBeDeclared = false);
            newProps.push(...additionalChildren);
        }
        return newProps;
    }

    /**
     * Creates derivedFEProperties of a specified type and returns them.
     */
    private createFlattenedChildren = (type: string, parentName: string):Array<DerivedFEProperty> => {
        let tempProps: Array<DerivedFEProperty> = [];
        let dataTypeObj: DataTypeModel = this.dataTypeService.getDataTypeByTypeName(type);
        this.dataTypeService.getDerivedDataTypeProperties(dataTypeObj, tempProps, parentName);
        return tempProps;
    }

    /* Sets the valueObj of parent property and its children.
    * Note: This logic is different than assignflattenedchildrenvalues - here we merge values, there we pick either the parents value, props value, or default value - without merging.
    */
    public initValueObjectRef = (property: PropertyFEModel): void => {
        if (property.derivedDataType == DerivedPropertyType.SIMPLE || property.isDeclared) { //if property is declared, it gets a simple input instead. List and map values and pseudo-children will be handled in property component
            property.valueObj = property.value || property.defaultValue;
            if (property.isDeclared) {
                if(typeof property.valueObj == 'object'){
                    property.valueObj = JSON.stringify(property.valueObj);
                }
            }else if(property.valueObj &&
                property.type !== PROPERTY_TYPES.STRING &&
                property.type !== PROPERTY_TYPES.JSON &&
                PROPERTY_DATA.SCALAR_TYPES.indexOf(property.type) == -1){
                    property.valueObj = JSON.parse(property.valueObj);//The valueObj contains the real value ans not the value as string
            }
        } else {
            if (property.derivedDataType == DerivedPropertyType.LIST) {
                property.valueObj = _.merge([], JSON.parse(property.defaultValue || '[]'), JSON.parse(property.value || '[]')); //value object should be merged value and default value. Value takes higher precendence. Set valueObj to empty obj if undefined.
            } else {
                property.valueObj = _.merge({}, JSON.parse(property.defaultValue || '{}'), JSON.parse(property.value || '{}')); //value object should be merged value and default value. Value takes higher precendence. Set valueObj to empty obj if undefined.
            }
            if ((property.derivedDataType == DerivedPropertyType.LIST || property.derivedDataType == DerivedPropertyType.MAP) && Object.keys(property.valueObj).length) {
                Object.keys(property.valueObj).forEach((key) => {
                    property.flattenedChildren.push(...this.createListOrMapChildren(property, key, property.valueObj[key]))
                });
            } else {
                this.assignFlattenedChildrenValues(property.valueObj, property.flattenedChildren, property.name);
            }
        }
    }

    /*
    * Loops through flattened properties array and to assign values
    * Then, convert any neccessary strings to objects, and vis-versa
    * For list or map property, creates new children props if valueObj has values
    */
    public assignFlattenedChildrenValues = (parentValueJSON: any, derivedPropArray: Array<DerivedFEProperty>, parentName: string) => {
        if (!derivedPropArray || !parentName) return;
        let propsToPushMap: Map<number, Array<DerivedFEProperty>> = new Map<number, Array<DerivedFEProperty>>();
        derivedPropArray.forEach((prop, index) => {

            let propNameInObj = prop.propertiesName.substring(prop.propertiesName.indexOf(parentName) + parentName.length + 1).split('#').join('.'); //extract everything after parent name
            prop.valueObj = _.get(parentValueJSON, propNameInObj, prop.value || prop.defaultValue); //assign value -first value of parent if exists. If not, prop.value if not, prop.defaultvalue

            if ( prop.isDeclared && typeof prop.valueObj == 'object') { //Stringify objects of items that are declared
                prop.valueObj = JSON.stringify(prop.valueObj);
            } else if(typeof prop.valueObj == PROPERTY_TYPES.STRING
                && (prop.type == PROPERTY_TYPES.INTEGER || prop.type == PROPERTY_TYPES.FLOAT || prop.type == PROPERTY_TYPES.BOOLEAN)){ //parse ints and non-string simple types
                prop.valueObj = JSON.parse(prop.valueObj);
            } else { //parse strings that should be objects
                if (prop.derivedDataType == DerivedPropertyType.COMPLEX && typeof prop.valueObj != 'object') {
                    prop.valueObj = JSON.parse(prop.valueObj || '{}');
                } else if (prop.derivedDataType == DerivedPropertyType.LIST && typeof prop.valueObj != 'object') {
                    prop.valueObj = JSON.parse(prop.valueObj || '[]');
                } else if (prop.derivedDataType == DerivedPropertyType.MAP && typeof prop.valueObj != 'object' && (!prop.isChildOfListOrMap || !prop.schema.property.isSimpleType)) { //dont parse values for children of map of simple
                    prop.valueObj = JSON.parse(prop.valueObj || '{}');
                }
                if ((prop.derivedDataType == DerivedPropertyType.LIST || prop.derivedDataType == DerivedPropertyType.MAP) && typeof prop.valueObj == 'object' && Object.keys(prop.valueObj).length) {
                    let newProps: Array<DerivedFEProperty> = [];
                    Object.keys(prop.valueObj).forEach((key) => {
                        newProps.push(...this.createListOrMapChildren(prop, key, prop.valueObj[key]));//create new children, assign their values, and then add to array
                    });
                    propsToPushMap[index + 1] = newProps;
                }
            }
        });

        //add props after we're done looping (otherwise our loop gets messed up). Push in reverse order, so we dont mess up indexes.
        Object.keys(propsToPushMap).reverse().forEach((indexToInsert) => {
            derivedPropArray.splice(+indexToInsert, 0, ...propsToPushMap[indexToInsert]); //slacker parsing
        });
    }

    public resetPropertyValue = (property: PropertyFEModel, newValue: string, nestedPath?: string): void => {
        property.value = newValue;
        if (nestedPath) {
            let newProp = property.flattenedChildren.find(prop => prop.propertiesName == nestedPath);
            newProp && this.assignFlattenedChildrenValues(JSON.parse(newValue), [newProp], property.name);
        } else {
            this.initValueObjectRef(property);
        }
    }



}
