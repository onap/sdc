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

/**
 * Created by obarda on 8/24/2016.
 */
'use strict';
import {PropertyModel} from "./properties";
import {InputPropertyBase} from "./input-property-base";
import {SchemaPropertyGroupModel} from "./aschema-property";

export class InputsGroup {
    constructor(inputsObj?:InputsGroup) {
        _.forEach(inputsObj, (inputs:Array<InputModel>, instance) => {
            this[instance] = [];
            _.forEach(inputs, (input:InputModel):void => {
                this[instance].push(new InputModel(input));
            });
        });
    }
}

export interface IInputModel extends InputPropertyBase {
    //server data
    definition:boolean;
    value:string;
    componentInstanceName:string;
    //costom properties
    isNew:boolean;
    properties:Array<PropertyModel>;
    inputs:Array<InputModel>;
    filterTerm:string;

}
export class InputModel implements IInputModel {

    //server data
    uniqueId:string;
    name:string;
    type:string;
    password:boolean;
    required:boolean;
    definition:boolean;
    parentUniqueId:string;
    description:string;
    componentInstanceName:string;
    componentInstanceId:string;
    schema:SchemaPropertyGroupModel;
    defaultValue:string;
    value:string;

    //costom properties
    isNew:boolean;
    isDeleteDisabled:boolean;
    properties:Array<PropertyModel>;
    inputs:Array<InputModel>;
    isAlreadySelected:boolean;
    filterTerm:string;

    constructor(input:InputModel) {

        if (input) {
            this.uniqueId = input.uniqueId;
            this.name = input.name;
            this.type = input.type;
            this.description = input.description;
            this.password = input.password;
            this.required = input.required;
            this.definition = input.definition;
            this.parentUniqueId = input.parentUniqueId;
            this.description = input.description;
            this.componentInstanceName = input.componentInstanceName;
            this.componentInstanceId = input.componentInstanceId;
            this.schema = input.schema;
            this.defaultValue = input.defaultValue;
            this.value = input.value;
            this.filterTerm = this.name + ' ' + this.description + ' ' + this.type + ' ' + this.componentInstanceName;
            this.inputs = input.inputs;
            this.properties = input.properties;
        }
    }

    public toJSON = ():any => {
        let input = angular.copy(this);
        input.isNew = undefined;
        input.isDeleteDisabled = undefined;
        input.properties = undefined;
        input.inputs = undefined;
        input.isAlreadySelected = undefined;
        input.filterTerm = undefined;
        return input;
    };
}

