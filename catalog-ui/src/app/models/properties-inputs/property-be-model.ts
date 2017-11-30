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

import { PropertyInputDetail, SchemaPropertyGroupModel, SchemaProperty } from "app/models";
import { PROPERTY_DATA, PROPERTY_TYPES } from 'app/utils';
export enum DerivedPropertyType {
    SIMPLE,
    LIST,
    MAP,
    COMPLEX
}

export class PropertyBEModel {

    defaultValue: string;
    definition: boolean;
    description: string;
    fromDerived: boolean;
    getInputValues: Array<PropertyInputDetail>
    name: string;
    parentUniqueId: string;
    password: boolean;
    required: boolean;
    schema: SchemaPropertyGroupModel;
    type: string;
    uniqueId: string;
    value: string;

    constructor(property?: PropertyBEModel) {
        if (property) {
            this.defaultValue = property.defaultValue;
            this.description = property.description;
            this.fromDerived = property.fromDerived;
            this.name = property.name;
            this.parentUniqueId = property.parentUniqueId;
            this.password = property.password;
            this.required = property.required;
            this.schema = property.schema;
            this.type = property.type;
            this.uniqueId = property.uniqueId;
            this.value = property.value;
            this.definition = property.definition;
            this.getInputValues = property.getInputValues;
        }

        if (!this.schema || !this.schema.property) {
            this.schema = new SchemaPropertyGroupModel(new SchemaProperty());
        } else { //forcing creating new object, so editing different one than the object in the table
            this.schema = new SchemaPropertyGroupModel(new SchemaProperty(this.schema.property));
        }
    }



    public toJSON = (): any => {
        let temp = angular.copy(this);
        temp.value = temp.value === "{}" || temp.value === "[]" ? undefined : temp.value;
        temp.defaultValue = temp.defaultValue === "{}" || temp.defaultValue === "[]" ? undefined : temp.defaultValue;
        return temp;
    };

    public getDerivedPropertyType = () => {
        if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1) {
            return DerivedPropertyType.SIMPLE;
        } else if (this.type == PROPERTY_TYPES.LIST) {
            return DerivedPropertyType.LIST;
        } else if (this.type == PROPERTY_TYPES.MAP) {
            return DerivedPropertyType.MAP;
        } else {
            return DerivedPropertyType.COMPLEX;
        }
    }

}


// EXTRAS FROM CONSTRUCTOR:
//         this.source = property.source;
//         this.valueUniqueUid = property.valueUniqueUid;
//         this.path = property.path;
//         this.rules = property.rules;
//         this.resourceInstanceUniqueId = property.resourceInstanceUniqueId;
//         this.readonly = property.readonly;
//         this.simpleType = property.simpleType;
//         this.componentInstanceId = property.componentInstanceId;
//         this.parentValue = property.parentValue;
//NEW PROPERTIES MAY NEED:
// export class PropertyFEModel extends PropertyBEModel {
//     componentInstanceId: string;
//     isAlreadySelected: boolean;
//     filterTerm: string;
// }
//FOR INPUTS, BE ALSO INCLUDES:
//export class InputFEModel extends PropertyBEModel {
//     hidden: boolean;
//     label: string;
//     immutable: boolean;
// }
