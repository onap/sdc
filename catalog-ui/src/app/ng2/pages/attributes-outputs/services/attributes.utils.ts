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
import { Injectable } from '@angular/core';
import { DataTypeService } from "app/ng2/services/data-type.service";
import { PROPERTY_TYPES } from "app/utils";
import { AttributesService } from "app/ng2/services/attributes.service";
import { InstanceBeAttributesMap, InstanceFeAttributesMap } from "app/models/attributes-outputs/attribute-fe-map";
import {OutputFEModel} from "../../../../models/attributes-outputs/output-fe-model";
import { AttributeBEModel, DerivedAttributeType } from "app/models/attributes-outputs/attribute-be-model";
import { AttributeFEModel } from "app/models/attributes-outputs/attribute-fe-model";
import { DerivedFEAttribute } from "app/models/attributes-outputs/derived-fe-attribute";
import { DataTypeModel } from "app/models";

@Injectable()
export class AttributesUtils {

    constructor(private dataTypeService:DataTypeService, private attributesService: AttributesService) {}

    /**
     * Entry point when getting attributes from server
     * For each instance, loop through each property, and:
     * 1. Create flattened children
     * 2. Check against outputs to see if any props are declared and disable them
     * 3. Initialize valueObj (which also creates any new list/map flattened children as needed)
     * Returns InstanceFeAttributesMap
     */
    public convertAttributesMapToFEAndCreateChildren = (instanceAttributesMap:InstanceBeAttributesMap, isVF:boolean, outputs?:Array<OutputFEModel>): InstanceFeAttributesMap => {
        let instanceFeAttributesMap:InstanceFeAttributesMap = new InstanceFeAttributesMap();
        angular.forEach(instanceAttributesMap, (attributes:Array<AttributeBEModel>, instanceId:string) => {
            let propertyFeArray: Array<AttributeFEModel> = [];
            _.forEach(attributes, (property: AttributeBEModel) => {

                if (this.dataTypeService.getDataTypeByTypeName(property.type)) { // if type not exist in data types remove property from list

                    let newFEAttrib: AttributeFEModel = new AttributeFEModel(property); //Convert property to FE

                    this.initValueObjectRef(newFEAttrib); //initialize valueObj AND creates flattened children
                    propertyFeArray.push(newFEAttrib);
                    newFEAttrib.updateExpandedChildAttributeId(newFEAttrib.name); //display only the first level of children

                    //if this prop (or any children) are declared, set isDeclared and disable checkbox on parents/children
                    if (newFEAttrib.getOutputValues && newFEAttrib.getOutputValues.length) {
                        newFEAttrib.getOutputValues.forEach(propOutputDetail => {
                            let outputPath = propOutputDetail.outputPath;
                            if (!outputPath) { //TODO: this is a workaround until Marina adds outputPath
                                let output = outputs.find(output => output.uniqueId == propOutputDetail.outputId);
                                if (!output) { console.log("CANNOT FIND INPUT FOR " + propOutputDetail.outputId); return; }
                                else outputPath = output.outputPath;
                            }
                            if (outputPath == newFEAttrib.name) outputPath = undefined; // if not complex we need to remove the outputPath from FEModel so we not look for a child
                            newFEAttrib.setAsDeclared(outputPath); //if a path is sent, its a child prop. this param is optional
                            this.attributesService.disableRelatedAttributes(newFEAttrib, outputPath);
                        });
                    }
                }
            });
            instanceFeAttributesMap[instanceId] = propertyFeArray;

        });
        return instanceFeAttributesMap;
    }

    public convertAddAttributeBEToAttributeFE = (property: AttributeBEModel): AttributeFEModel => {
        const newFEProp: AttributeFEModel = new AttributeFEModel(property); //Convert property to FE
        this.initValueObjectRef(newFEProp);
        newFEProp.updateExpandedChildAttributeId(newFEProp.name); //display only the first level of children
        return newFEProp;
    }

    public createListOrMapChildren = (property:AttributeFEModel | DerivedFEAttribute, key: string, valueObj: any): Array<DerivedFEAttribute> => {
        let newProps: Array<DerivedFEAttribute> = [];
        let parentProp = new DerivedFEAttribute(property, property.attributesName, true, key, valueObj);
        newProps.push(parentProp);

        if (!property.schema.property.isSimpleType) {
            let additionalChildren:Array<DerivedFEAttribute> = this.createFlattenedChildren(property.schema.property.type, parentProp.attributesName);
            this.assignFlattenedChildrenValues(parentProp.valueObj, additionalChildren, parentProp.attributesName);
            additionalChildren.forEach(prop => prop.canBeDeclared = false);
            newProps.push(...additionalChildren);
        }
        return newProps;
    }

    /**
     * Creates derivedFEAttributes of a specified type and returns them.
     */
    private createFlattenedChildren = (type: string, parentName: string):Array<DerivedFEAttribute> => {
        let tempProps: Array<DerivedFEAttribute> = [];
        let dataTypeObj: DataTypeModel = this.dataTypeService.getDataTypeByTypeName(type);
        this.dataTypeService.getDerivedDataTypeAttributes(dataTypeObj, tempProps, parentName);
        return _.sortBy(tempProps, ['propertiesName']);
    }

    /* Sets the valueObj of parent property and its children.
    * Note: This logic is different than assignflattenedchildrenvalues - here we merge values, there we pick either the parents value, props value, or default value - without merging.
    */
    public initValueObjectRef = (attribute: AttributeFEModel): void => {
        attribute.resetValueObjValidation();
        if (attribute.isDeclared) { //if attribute is declared, it gets a simple output instead. List and map values and pseudo-children will be handled in attribute component
            attribute.valueObj = attribute.value || attribute.defaultValue || null;  // use null for empty value object
            if (attribute.valueObj && typeof attribute.valueObj == 'object') {
                attribute.valueObj = JSON.stringify(attribute.valueObj);
            }
        } else {
            attribute.valueObj = attribute.getValueObj();
            if (attribute.derivedDataType == DerivedAttributeType.LIST || attribute.derivedDataType == DerivedAttributeType.MAP) {
                attribute.flattenedChildren = [];
                Object.keys(attribute.valueObj).forEach((key) => {
                    attribute.flattenedChildren.push(...this.createListOrMapChildren(attribute, key, attribute.valueObj[key]))
                });
            } else if (attribute.derivedDataType === DerivedAttributeType.COMPLEX) {
                attribute.flattenedChildren = this.createFlattenedChildren(attribute.type, attribute.name);
                this.assignFlattenedChildrenValues(attribute.valueObj, attribute.flattenedChildren, attribute.name);
                attribute.flattenedChildren.forEach((childProp) => {
                    attribute.childPropUpdated(childProp);
                });
            }
        }
        attribute.updateValueObjOrig();
    };

    /*
    * Loops through flattened attributes array and to assign values
    * Then, convert any neccessary strings to objects, and vis-versa
    * For list or map property, creates new children props if valueObj has values
    */
    public assignFlattenedChildrenValues = (parentValueJSON: any, derivedPropArray: Array<DerivedFEAttribute>, parentName: string) => {
        if (!derivedPropArray || !parentName) return;
        let propsToPushMap: Map<number, Array<DerivedFEAttribute>> = new Map<number, Array<DerivedFEAttribute>>();
        derivedPropArray.forEach((prop, index) => {

            let propNameInObj = prop.attributesName.substring(prop.attributesName.indexOf(parentName) + parentName.length + 1).split('#').join('.'); //extract everything after parent name
            prop.valueObj = _.get(parentValueJSON, propNameInObj, prop.value || prop.defaultValue || null); //assign value -first value of parent if exists. If not, prop.value if not, prop.defaultvalue
            prop.value = (prop.valueObj !== null && (typeof prop.valueObj) != 'string') ? JSON.stringify(prop.valueObj) : prop.valueObj;

            if ((prop.isDeclared || prop.type == PROPERTY_TYPES.STRING || prop.type == PROPERTY_TYPES.JSON)) { //Stringify objects of items that are declared or from type string/json
                prop.valueObj = (prop.valueObj !== null && typeof prop.valueObj == 'object') ? JSON.stringify(prop.valueObj) : prop.valueObj;
            } else if(prop.type == PROPERTY_TYPES.INTEGER || prop.type == PROPERTY_TYPES.FLOAT || prop.type == PROPERTY_TYPES.BOOLEAN){ //parse ints and non-string simple types
                prop.valueObj = (prop.valueObj !== null && typeof prop.valueObj == PROPERTY_TYPES.STRING) ? JSON.parse(prop.valueObj) : prop.valueObj;
            } else { //parse strings that should be objects
                if (prop.derivedDataType == DerivedAttributeType.COMPLEX) {
                    prop.valueObj = (prop.valueObj === null || typeof prop.valueObj != 'object') ? JSON.parse(prop.valueObj || '{}') : prop.valueObj;
                } else if (prop.derivedDataType == DerivedAttributeType.LIST) {
                    prop.valueObj = (prop.valueObj === null || typeof prop.valueObj != 'object') ? JSON.parse(prop.valueObj || '[]') : prop.valueObj;
                } else if (prop.derivedDataType == DerivedAttributeType.MAP) {
                    if (!prop.isChildOfListOrMap || !prop.schema.property.isSimpleType) {
                        prop.valueObj = (prop.valueObj === null || typeof prop.valueObj != 'object') ? JSON.parse(prop.valueObj || '{}') : prop.valueObj;
                    }
                }
                if ((prop.derivedDataType == DerivedAttributeType.LIST || prop.derivedDataType == DerivedAttributeType.MAP) && typeof prop.valueObj == 'object' && prop.valueObj !== null && Object.keys(prop.valueObj).length) {
                    let newProps: Array<DerivedFEAttribute> = [];
                    Object.keys(prop.valueObj).forEach((key) => {
                        newProps.push(...this.createListOrMapChildren(prop, key, prop.valueObj[key]));//create new children, assign their values, and then add to array
                    });
                    propsToPushMap[index + 1] = newProps;
                }
            }

            prop.valueObj = AttributeFEModel.cleanValueObj(prop.valueObj);
        });

        //add props after we're done looping (otherwise our loop gets messed up). Push in reverse order, so we dont mess up indexes.
        Object.keys(propsToPushMap).reverse().forEach((indexToInsert) => {
            derivedPropArray.splice(+indexToInsert, 0, ...propsToPushMap[indexToInsert]); //slacker parsing
        });
    }

    public resetAttributeValue = (attribute: AttributeFEModel, newValue: string, nestedPath?: string): void => {
        attribute.value = newValue;
        if (nestedPath) {
            let newAttrib = attribute.flattenedChildren.find(attrib => attrib.attributesName == nestedPath);
            newAttrib && this.assignFlattenedChildrenValues(JSON.parse(newValue), [newAttrib], attribute.name);
            attribute.updateValueObjOrig();
        } else {
            this.initValueObjectRef(attribute);
        }
    }

}
