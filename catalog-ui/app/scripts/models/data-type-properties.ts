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
 * Created by rcohen on 9/25/2016.
 */
/// <reference path="../references"/>
module Sdc.Models {
    'use strict';

    export class DataTypePropertyModel {

        //server data
        uniqueId:string;
        type:string;
        required:boolean;
        definition:boolean;
        description:string;
        password:boolean;
        name:string;
        parentUniqueId:string;
        defaultValue:string;
        constraints:Array<any>;
        //custom
        simpleType:string;

        constructor(dataTypeProperty:DataTypePropertyModel) {
            if (dataTypeProperty) {
                this.uniqueId = dataTypeProperty.uniqueId;
                this.type = dataTypeProperty.type;
                this.required = dataTypeProperty.required;
                this.definition = dataTypeProperty.definition;
                this.description = dataTypeProperty.description;
                this.password = dataTypeProperty.password;
                this.name = dataTypeProperty.name;
                this.parentUniqueId = dataTypeProperty.parentUniqueId;
                this.defaultValue = dataTypeProperty.defaultValue;
                this.constraints = dataTypeProperty.constraints;
                this.simpleType = dataTypeProperty.simpleType;
            }
        }

        public toJSON = ():any => {
            this.simpleType = undefined;
            return this;
        };
    }

}
