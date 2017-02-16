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

    export class AttributesGroup {
        constructor(attributesObj?:Models.AttributesGroup) {
            _.forEach(attributesObj, (attributes:Array<Models.AttributeModel>, instance) => {
                this[instance] = [];
                _.forEach(attributes, (attribute:Models.AttributeModel):void => {
                    attribute.resourceInstanceUniqueId = instance;
                    attribute.readonly = true;
                    this[instance].push(new Models.AttributeModel(attribute));
                });
            });
        }
    }

    export interface IAttributeModel {

        //server data
        uniqueId:string;
        name:string;
        defaultValue:string;
        description:string;
        type:string;
        schema:Models.SchemaAttributeGroupModel;
        status:string;
        value:string;
        hidden:boolean;
        parentUniqueId:string;
        //custom data
        resourceInstanceUniqueId:string;
        readonly:boolean;
        valueUniqueUid:string;
    }

    export class AttributeModel implements IAttributeModel {

        //server data
        uniqueId:string;
        name:string;
        defaultValue:string;
        description:string;
        type:string;
        schema:Models.SchemaAttributeGroupModel;
        status:string;
        value:string;
        hidden:boolean;
        parentUniqueId:string;
        //custom data
        resourceInstanceUniqueId:string;
        readonly:boolean;
        valueUniqueUid:string;

        constructor(attribute?:Models.AttributeModel) {
            if (attribute) {
                this.uniqueId = attribute.uniqueId;
                this.name = attribute.name;
                this.defaultValue = attribute.defaultValue;
                this.description = attribute.description;
                this.type = attribute.type;
                this.status = attribute.status;
                this.schema = attribute.schema;
                this.value = attribute.value;
                this.hidden = attribute.hidden;
                this.parentUniqueId = attribute.parentUniqueId;
                this.resourceInstanceUniqueId = attribute.resourceInstanceUniqueId;
                this.readonly = attribute.readonly;
                this.valueUniqueUid = attribute.valueUniqueUid;
            }

            if (!this.schema || !this.schema.property) {
                this.schema = new Models.SchemaPropertyGroupModel(new Models.SchemaProperty());
            } else {
                //forcing creating new object, so editing different one than the object in the table
                this.schema = new Models.SchemaAttributeGroupModel(new Models.SchemaAttribute(this.schema.property));
            }

            this.convertValueToView();
        }

        public convertToServerObject:Function = ():string => {
            if (this.defaultValue && this.type === 'map') {
                this.defaultValue = '{' + this.defaultValue + '}';
            }
            if (this.defaultValue && this.type === 'list') {
                this.defaultValue = '[' + this.defaultValue + ']';
            }
            this.defaultValue = this.defaultValue != "" && this.defaultValue != "[]" && this.defaultValue != "{}" ? this.defaultValue : null;

            return JSON.stringify(this);
        };


        public convertValueToView() {
            //unwrapping value {} or [] if type is complex
            if (this.defaultValue && (this.type === 'map' || this.type === 'list') &&
                ['[', '{'].indexOf(this.defaultValue.charAt(0)) > -1 &&
                [']', '}'].indexOf(this.defaultValue.slice(-1)) > -1) {
                this.defaultValue = this.defaultValue.slice(1, -1);
            }

            //also for value - for the modal in canvas
            if (this.value && (this.type === 'map' || this.type === 'list') &&
                ['[', '{'].indexOf(this.value.charAt(0)) > -1 &&
                [']', '}'].indexOf(this.value.slice(-1)) > -1) {
                this.value = this.value.slice(1, -1);
            }
        }

        public toJSON = ():any => {
            if (!this.resourceInstanceUniqueId) {
                this.value = undefined;
            }
            this.readonly = undefined;
            this.resourceInstanceUniqueId = undefined;
            return this;
        };
    }
}
