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
 * Created by osonsino on 16/05/2016.
 */
/// <reference path="../references"/>
module Sdc.Models {
    'use strict';

    export class SchemaPropertyGroupModel{
        property: SchemaProperty;

        constructor(schemaProperty?:Models.SchemaProperty) {
            this.property = schemaProperty;
        }
    }

    export class SchemaProperty {

        type: string;
        required: boolean;
        definition: boolean;
        description: string;
        password: boolean;
        //custom properties
        simpleType: string;

        constructor(schemaProperty?:SchemaProperty) {
            if(schemaProperty) {
                this.type = schemaProperty.type;
                this.required = schemaProperty.required;
                this.definition = schemaProperty.definition;
                this.description = schemaProperty.description;
                this.password = schemaProperty.password;
                this.simpleType = schemaProperty.simpleType;
            }
        }

        public toJSON = ():any => {
            this.simpleType = undefined;
            return this;
        };
    }
}


