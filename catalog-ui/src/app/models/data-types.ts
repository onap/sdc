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
'use strict';
import {PropertyBEModel} from "./properties-inputs/property-be-model";
import {AttributeBEModel} from "./attributes-outputs/attribute-be-model";
import {Model} from "./model";
import {PROPERTY_DATA} from "../utils/constants";

export class DataTypeModel {

    name: string;
    uniqueId: string;
    derivedFromName: string;
    derivedFrom: DataTypeModel;
    description: string;
    creationTime: string;
    modificationTime: string;
    properties: Array<PropertyBEModel>;
    attributes: Array<AttributeBEModel>;
    model: Model;

    constructor(dataType?: DataTypeModel) {
        if (!dataType) {
            return;
        }

        this.uniqueId = dataType.uniqueId;
        this.name = dataType.name;
        this.description = dataType.description;
        this.derivedFromName = dataType.derivedFromName;
        if (dataType.derivedFrom) {
            this.derivedFrom = new DataTypeModel(dataType.derivedFrom);
        }
        this.creationTime = dataType.creationTime;
        this.modificationTime = dataType.modificationTime;
        if (dataType.properties) {
            this.properties = [];
            dataType.properties.forEach(property => {
                this.properties.push(new PropertyBEModel(property));
            });
        }
        this.attributes = dataType.attributes;
        this.model = dataType.model;
    }

    public toJSON = ():any => {

        return this;
    };

    /**
     * Parses the default value to JSON.
     */
    public parseDefaultValueToJson(): any {
        if (PROPERTY_DATA.TYPES.indexOf(this.name) > -1) {
            return undefined;
        }
        const defaultValue = {};
        if (this.properties) {
            this.properties.forEach(property => {
                const propertyDefaultValue = property.parseDefaultValueToJson();
                if (propertyDefaultValue != undefined) {
                    defaultValue[property.name] = propertyDefaultValue;
                }
            });
        }

        return defaultValue === {} ? undefined : defaultValue;
    }
}

