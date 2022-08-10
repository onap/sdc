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
            this.parameters = Array.from(toscaConcatFunction.parameters);
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