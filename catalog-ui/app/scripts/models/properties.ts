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
/// <reference path="../references"/>
module Sdc.Models {
    'use strict';

    export class PropertiesGroup {
        constructor(propertiesObj?:Models.PropertiesGroup){
            _.forEach(propertiesObj, (properties:Array<Models.PropertyModel>, instance) => {
                this[instance] = [];
                _.forEach(properties, (property:Models.PropertyModel):void => {
                    property.resourceInstanceUniqueId = instance;
                    property.readonly = true;
                    this[instance].push(new Models.PropertyModel(property));
                });
            });
        }
    }

    export interface IPropertyModel {

        //server data
        uniqueId: string;
        name: string;
        constraints: Array<Object>;
        defaultValue: string;
        description: string;
        password: boolean;
        required: boolean;
        type: string;
        source: string;
        parentUniqueId: string;
        schema: Models.SchemaPropertyGroupModel;

        //instance properties
        value:string;
        valueUniqueUid:string;
        path:Array<string>;
        rules:Array<Object>;

        //custom properties
        resourceInstanceUniqueId: string;
        readonly: boolean;
        simpleType: string;
    }

    export class PropertyModel implements IPropertyModel{

        //server data
        uniqueId:string;
        name:string;
        constraints:Array<Object>;
        defaultValue:string;
        description:string;
        password:boolean;
        required:boolean;
        type:string;
        source:string;
        parentUniqueId:string;
        schema: Models.SchemaPropertyGroupModel;

        //instance properties
        value:string;
        valueUniqueUid:string;
        path:Array<string>;
        rules:Array<Object>;

        //custom properties
        resourceInstanceUniqueId:string;
        readonly:boolean;
        simpleType: string;
        filterTerm: string;
        isAlreadySelected: boolean;

        constructor(property?:Models.PropertyModel) {
            if (property) {
                this.uniqueId = property.uniqueId;
                this.name = property.name;
                this.constraints = property.constraints;
                this.defaultValue = property.defaultValue;
                this.description = property.description;
                this.password = property.password;
                this.required = property.required;
                this.type = property.type;
                this.source = property.source;
                this.parentUniqueId = property.parentUniqueId;
                this.schema = property.schema;
                this.value = property.value?property.value:property.defaultValue;
                this.valueUniqueUid = property.valueUniqueUid;
                this.path = property.path;
                this.rules = property.rules;
                this.resourceInstanceUniqueId = property.resourceInstanceUniqueId;
                this.readonly = property.readonly;
                this.simpleType = property.simpleType;


            }

            if(!this.schema || !this.schema.property) {
                this.schema = new Models.SchemaPropertyGroupModel(new Models.SchemaProperty());
            } else {
                //forcing creating new object, so editing different one than the object in the table
                this.schema = new Models.SchemaPropertyGroupModel(new Models.SchemaProperty(this.schema.property));
            }
            if(property) {
                this.filterTerm = this.name + " " + (this.description||"") +" " + this.type;
                if(this.schema.property && this.schema.property.type) {
                    this.filterTerm += " " +this.schema.property.type;
                }
            }
        }

        public convertToServerObject:Function = ():string => {
            let serverObject = {};
            let mapData = {
                "type": this.type,
                "required": this.required || false,
                "defaultValue": this.defaultValue != "" && this.defaultValue != "[]" && this.defaultValue != "{}" ? this.defaultValue :null,
                "description": this.description,
                "constraints": this.constraints,
                "isPassword": this.password || false,
                "schema": this.schema,
                "name": this.name
            };
            serverObject[this.name] = mapData;

            return JSON.stringify(serverObject);
        };


        // public convertValueToView () {
        //     //unwrapping value {} or [] if type is complex
        //     if (this.defaultValue && (this.type === 'map' || this.type === 'list') &&
        //         ['[','{'].indexOf(this.defaultValue.charAt(0)) > -1 &&
        //         [']','}'].indexOf(this.defaultValue.slice(-1)) > -1) {
        //         this.defaultValue = this.defaultValue.slice(1, -1);
        //     }
        //
        //     //also for value - for the modal in canvas
        //     if (this.value && (this.type === 'map' || this.type === 'list') &&
        //         ['[','{'].indexOf(this.value.charAt(0)) > -1 &&
        //         [']','}'].indexOf(this.value.slice(-1)) > -1) {
        //         this.value = this.value.slice(1, -1);
        //     }
        // }

        public toJSON = ():any => {
            if(!this.resourceInstanceUniqueId){
                this.value = undefined;
            }
            this.readonly = undefined;
            this.resourceInstanceUniqueId = undefined;
            this.simpleType = undefined;
            this.value = this.value === "{}" || this.value === "[]" ? undefined: this.value;
            this.defaultValue = this.defaultValue === "{}" || this.defaultValue === "[]" ? undefined: this.defaultValue;
            return this;
        };
    }
}
