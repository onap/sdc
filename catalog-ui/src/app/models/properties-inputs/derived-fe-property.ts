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
import { SchemaPropertyGroupModel, SchemaProperty } from '../schema-property';
import { DerivedPropertyType, PropertyBEModel, PropertyFEModel } from '../../models';
import {SubPropertyToscaFunction} from "../sub-property-tosca-function";
import {ToscaFunction} from "../tosca-function";
import { PROPERTY_TYPES } from 'app/utils';
import { UUID } from "angular2-uuid";


export class DerivedFEProperty extends PropertyBEModel {
    valueObj: any;
    valueObjIsValid: boolean;
    valueObjOrig: any;
    valueObjIsChanged: boolean;
    value: any
    parentName: string;
    propertiesName: string; //"network_assignments#ipv4_subnet#use_ipv4 =  parentPath + name
    derivedDataType: DerivedPropertyType;
    toscaFunction: ToscaFunction;
    isDeclared: boolean;
    isSelected: boolean;
    isDisabled: boolean;
    hidden: boolean;
    isChildOfListOrMap: boolean;
    canBeDeclared: boolean;
    mapKey: string;
    mapKeyError: string;
    mapInlist: boolean
    inputName: string;
    parentMapKey: string;
    toscaPath: string[];

    constructor(property: PropertyBEModel, parentName?: string, createChildOfListOrMap?: boolean, key?:string, value?:any) {
        if (!createChildOfListOrMap) { //creating a standard derived prop
            super(property);
            this.toscaPath = [];
            this.parentName = parentName ? parentName : null;
            this.propertiesName = (parentName) ? parentName + '#' + property.name : property.name;
            this.canBeDeclared = true; //defaults to true
            if (property instanceof DerivedFEProperty) {
                this.toscaPath = property.toscaPath != null ? property.toscaPath : [];
            } else {
                this.toscaPath = property.parentToscaPath != null ? property.parentToscaPath : property.parentToscaPath;
            }
            if (this.toscaPath.length == 0 && parentName != null && parentName.indexOf('#') != -1) {
                let lastparent = parentName.split('#');
                this.toscaPath.push(lastparent[lastparent.length - 1]);
            }
            this.toscaPath.push(property.name);
        } else { //creating a direct child of list or map (ie. Item that can be deleted, with UUID instead of name)
            super(null);
            let toscaPathCopy = null;
            if (property instanceof DerivedFEProperty) {
                toscaPathCopy = property.toscaPath != null ? property.toscaPath .toString() : null;
            } else {
                toscaPathCopy = property.parentToscaPath != null ? property.parentToscaPath.toString() : null;
            }
            this.toscaPath = toscaPathCopy != null ? toscaPathCopy.split(",") : [];
            this.isChildOfListOrMap = true;
            this.canBeDeclared = false;
            this.name = UUID.UUID();
            this.parentName = parentName;
            this.propertiesName = parentName + '#' + this.name;
            
            if (property.type == PROPERTY_TYPES.LIST) {
                let parentKey : string = null;
                if (property instanceof DerivedFEProperty) {
                    if (property.valueObj != '') {
                        if (key != '') {
                            this.toscaPath.push(key);
                        } else {
                            let toscaIndex = Object.keys(property.valueObj).sort().reverse()[0];
                            this.toscaPath.push((Number(toscaIndex) + 1).toString());
                        }
                    } else {
                        this.toscaPath.push("0");
                    }
                } else {
                    if (property instanceof PropertyFEModel && property.valueObj != '') {
                        if (key != '') {
                            parentKey = key;
                        }else{
                            let toscaIndex = Object.keys(property.valueObj).sort().reverse()[0];
                            parentKey = (Number(toscaIndex) + 1).toString();
                        }
                    } else {
                        parentKey = "0";
                    }
                    this.toscaPath.push(parentKey);
                }
                if (property.schemaType != PROPERTY_TYPES.MAP) {
                    this.mapKey = parentKey;
                }
                this.mapKeyError = null;
                this.type = property.schema.property.type;
                if (this.type == PROPERTY_TYPES.MAP){
                    this.mapInlist = true;
                    this.parentMapKey = parentKey;
                }
                this.schema = new SchemaPropertyGroupModel(new SchemaProperty(property.schema.property));
            } else { //map
                if (key) {
                    this.mapKey = key;
                    this.mapKeyError = null;
                } else {
                    this.mapKey = '';
                    this.mapKeyError = 'Key cannot be empty.';
                }
                this.type = property.type;
                if (property.schema.property.type == PROPERTY_TYPES.MAP){
                    const schProp = new SchemaProperty();
                    schProp.isSimpleType = true;
                    schProp.isDataType = false;
                    schProp.simpleType = PROPERTY_TYPES.STRING;
                    this.schema = new SchemaPropertyGroupModel(schProp);
                    this.schemaType = PROPERTY_TYPES.STRING;
                    if (property instanceof DerivedFEProperty) {
                        this.parentMapKey = property.parentMapKey;
                        if (value != null && typeof value == 'object') {
                            this.toscaFunction = property.toscaFunction;
                        }
                    }
                } else {
                    this.schema = new SchemaPropertyGroupModel(new SchemaProperty(property.schema.property));
                }
                if (this.toscaPath != null) {
                    let lastIndex = this.toscaPath[this.toscaPath.length - 1];
                    if(this.mapKey != lastIndex){
                        this.toscaPath.push(this.mapKey);
                    }
                } else {
                    this.toscaPath.push(this.mapKey);
                }
            }
            this.valueObj = (this.type == PROPERTY_TYPES.JSON && typeof value == 'object') ? JSON.stringify(value) : value;
            if (value != null) {
                this.value = typeof value == 'object' ? JSON.stringify(value) : value;
            }
            this.updateValueObjOrig();
        }
        this.parentToscaPath = this.toscaPath;
        if(property.subPropertyToscaFunctions != null){
            property.subPropertyToscaFunctions.forEach((item : SubPropertyToscaFunction) => {
                if(item.subPropertyPath.toString() === this.toscaPath.toString() && this.uniqueId == null){
                    this.toscaFunction = item.toscaFunction;
                }
            });
        }
        this.valueObjIsValid = true;
        this.derivedDataType = this.getDerivedPropertyType();
        this.inputName = property.inputName;
    }

    public getActualMapKey() {
        return (this.mapKeyError) ? this.name : this.mapKey;
    }

    public updateValueObj(valueObj:any, isValid:boolean) {
        this.valueObj = PropertyFEModel.cleanValueObj(valueObj);
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
export class DerivedFEPropertyMap {
    [parentPath: string]: Array<DerivedFEProperty>;
}

