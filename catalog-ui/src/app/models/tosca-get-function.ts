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

import {PropertySource} from "./property-source";
import {ToscaGetFunctionType} from "./tosca-get-function-type";
import {ToscaFunction} from "./tosca-function";
import {ToscaFunctionType} from "./tosca-function-type.enum";

export class ToscaGetFunction implements ToscaFunction {
    type: ToscaFunctionType;
    propertyUniqueId: string;
    propertyName: string;
    propertySource: PropertySource;
    sourceUniqueId: string;
    sourceName: string;
    functionType: ToscaGetFunctionType;
    propertyPathFromSource: Array<string>;
    value: any

    constructor(toscaGetFunction?: ToscaGetFunction) {
        if (!toscaGetFunction) {
            return;
        }
        this.type = toscaGetFunction.type;
        this.value = toscaGetFunction.value;
        this.propertyUniqueId = toscaGetFunction.propertyUniqueId;
        this.propertyName = toscaGetFunction.propertyName;
        this.propertySource = toscaGetFunction.propertySource;
        this.sourceUniqueId = toscaGetFunction.sourceUniqueId;
        this.sourceName = toscaGetFunction.sourceName;
        this.functionType = toscaGetFunction.functionType;
        if (toscaGetFunction.propertyPathFromSource) {
            this.propertyPathFromSource = [...toscaGetFunction.propertyPathFromSource];
        }
    }

    public buildValueString(): string {
        return JSON.stringify(this.buildValueObject());
    }

    public buildValueObject(): Object {
        if (this.functionType == ToscaGetFunctionType.GET_PROPERTY || this.functionType == ToscaGetFunctionType.GET_ATTRIBUTE) {
            return this.buildFunctionValueWithPropertySource();
        }
        if (this.functionType == ToscaGetFunctionType.GET_INPUT) {
            return this.buildGetInputFunctionValue();
        }
        return undefined;
    }

    private buildGetInputFunctionValue(): Object {
        if (this.propertyPathFromSource.length === 1) {
            return {[this.functionType.toLowerCase()]: this.propertyPathFromSource[0]};
        }
        return {[this.functionType.toLowerCase()]: this.propertyPathFromSource};
    }

    private buildFunctionValueWithPropertySource(): Object {
        if (this.propertySource == PropertySource.SELF) {
            return {
                [this.functionType.toLowerCase()]: [PropertySource.SELF, ...this.propertyPathFromSource]
            };
        }
        if (this.propertySource == PropertySource.INSTANCE) {
            return {
                [this.functionType.toLowerCase()]: [this.sourceName, ...this.propertyPathFromSource]
            };
        }
    }

}