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

import { SchemaPropertyGroupModel, SchemaProperty } from '../aschema-property';
import { DerivedPropertyType, PropertyBEModel } from '../../models';
import { PROPERTY_TYPES } from 'app/utils';
import { UUID } from "angular2-uuid";


export class DerivedFEProperty extends PropertyBEModel {
    valueObj: any; 
    parentName: string;
    propertiesName: string; //"network_assignments#ipv4_subnet#use_ipv4 =  parentPath + name
    derivedDataType: DerivedPropertyType;
    isDeclared: boolean;
    isSelected: boolean;
    isDisabled: boolean;
    hidden: boolean;
    isChildOfListOrMap: boolean;
    canBeDeclared: boolean;
    mapKey: string;

    constructor(property: PropertyBEModel, parentName?: string, createChildOfListOrMap?: boolean, key?:string, value?:any) {
        if (!createChildOfListOrMap) { //creating a standard derived prop
            super(property);
            this.parentName = parentName ? parentName : null;
            this.propertiesName = (parentName) ? parentName + '#' + property.name : property.name;
            this.canBeDeclared = true; //defaults to true
        } else { //creating a direct child of list or map (ie. Item that can be deleted, with UUID instead of name)
            super(null);
            this.isChildOfListOrMap = true;
            this.canBeDeclared = false;
            this.name = UUID.UUID();
            this.parentName = parentName;
            this.propertiesName = parentName + '#' + this.name;

            
            if (property.type == PROPERTY_TYPES.LIST) {
                this.mapKey = property.schema.property.type.split('.').pop();
                this.type = property.schema.property.type;
            } else { //map
                this.mapKey = key || "";
                this.type = property.type;
            }
            this.valueObj = (this.type == PROPERTY_TYPES.JSON && typeof value == 'object') ? JSON.stringify(value) : value;
            this.schema = new SchemaPropertyGroupModel(new SchemaProperty(property.schema.property));
        }
        this.derivedDataType = this.getDerivedPropertyType();
    }
    
}
export class DerivedFEPropertyMap {
    [parentPath: string]: Array<DerivedFEProperty>;
}

