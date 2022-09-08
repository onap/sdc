/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {ToscaFunctionType} from "../models/tosca-function-type.enum";
import {ToscaConcatFunction} from "../models/tosca-concat-function";
import {ToscaGetFunction} from "../models/tosca-get-function";
import {YamlFunction} from "../models/yaml-function";
import {ToscaFunction} from "../models/tosca-function";

export class ToscaFunctionHelper {

    public static convertObjectToToscaFunction(value: any): ToscaFunction {
        if (!value || !this.isValueToscaFunction(value)) {
            return undefined;
        }

        switch (value.type) {
            case ToscaFunctionType.CONCAT:
                return new ToscaConcatFunction(value);
            case ToscaFunctionType.GET_PROPERTY:
            case ToscaFunctionType.GET_INPUT:
            case ToscaFunctionType.GET_ATTRIBUTE:
                return new ToscaGetFunction(value);
            case ToscaFunctionType.YAML:
                return new YamlFunction(value);
            case ToscaFunctionType.STRING:
                return <ToscaFunction> {
                    type: ToscaFunctionType.STRING,
                    value: value.value,
                    buildValueString(): string {
                        return this.value;
                    },
                    buildValueObject(): Object {
                        return this.value;
                    }
                };
            default:
                return undefined;
        }
    }

    public static isValueToscaFunction(value: any): boolean {
        return value instanceof Object && 'type' in value && (<any>Object).values(ToscaFunctionType).includes(value.type);
    }

}
