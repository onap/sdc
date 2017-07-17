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

import { SchemaPropertyGroupModel, SchemaProperty } from "../aschema-property";
import { PropertyBEModel } from "../../models";
import {PROPERTY_DATA} from "../../utils/constants";
import {InputBEModel} from "./input-be-model";

export class InputFEModel extends InputBEModel {
    isSimpleType: boolean;
    relatedPropertyValue: any;
    relatedPropertyName: string;

    constructor(input?: InputBEModel) {
        super(input);
        if (input) {
            this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
            let relatedProperty = input.properties && input.properties[0] || input.inputs && input.inputs[0];
            if (relatedProperty) {
                this.relatedPropertyValue = relatedProperty.value;
                this.relatedPropertyName = relatedProperty.name;
            }
        }
    }

}