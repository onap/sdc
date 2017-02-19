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
/// <reference path="../references"/>
module Sdc.Models {
    'use strict';

    export class InputModel {

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

        //costom properties
        isNew: boolean;
        properties:Array<Models.PropertyModel>;
        inputs:Array<Models.InputModel>;
        isAlreadySelected: boolean;
        filterTerm: string;

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
                this.filterTerm = this.name + ' ' + this.description + ' ' + this.type + ' ' + this.componentInstanceName;
            }
        }

        public toJSON = ():any => {
            this.isNew = undefined;
            this.properties = undefined;
            this.inputs = undefined;
            this.isAlreadySelected = undefined;
            this.filterTerm = undefined;
            return this;
        };
    }
}
