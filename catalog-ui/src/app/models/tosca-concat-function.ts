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

import {ToscaFunction} from "./tosca-function";
import {ToscaFunctionType} from "./tosca-function-type.enum";
import {ToscaFunctionParameter} from "./tosca-function-parameter";
import {ToscaGetFunction} from "./tosca-get-function";
import {YamlFunction} from "./yaml-function";
import {ToscaStringParameter} from "./tosca-string-parameter";

export class ToscaConcatFunction implements ToscaFunction, ToscaFunctionParameter {
    type = ToscaFunctionType.CONCAT;
    value: any;
    parameters: Array<ToscaFunctionParameter> = [];

    constructor(toscaConcatFunction?: ToscaConcatFunction) {
        if (!toscaConcatFunction) {
            return;
        }
        this.value = toscaConcatFunction.value;
        if (toscaConcatFunction.parameters) {
            toscaConcatFunction.parameters.forEach(parameter => {
                switch (parameter.type) {
                    case ToscaFunctionType.GET_INPUT:
                    case ToscaFunctionType.GET_ATTRIBUTE:
                    case ToscaFunctionType.GET_PROPERTY:
                        this.parameters.push(new ToscaGetFunction(<ToscaGetFunction>parameter));
                        break;
                    case ToscaFunctionType.CONCAT:
                        this.parameters.push(new ToscaConcatFunction(<ToscaConcatFunction>parameter));
                        break;
                    case ToscaFunctionType.YAML:
                        this.parameters.push(new YamlFunction(<YamlFunction>parameter));
                        break;
                    case ToscaFunctionType.STRING:
                        this.parameters.push(new ToscaStringParameter(<ToscaStringParameter>parameter));
                        break;
                    default:
                        console.error(`Unsupported parameter type "${parameter.type}"`);
                        this.parameters.push(parameter);
                }
            });
        }
    }

    public buildValueString(): string {
        return JSON.stringify(this.buildValueObject());
    }

    public buildValueObject(): Object {
        return {
            [this.type.toLowerCase()]: this.parameters.map(parameter => parameter.buildValueObject())
        }
    }

}