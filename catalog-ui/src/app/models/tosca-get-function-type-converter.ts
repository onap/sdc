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

import {ToscaGetFunctionType} from './tosca-get-function-type';
import {ToscaFunctionType} from "./tosca-function-type.enum";

export class ToscaGetFunctionTypeConverter {

    static convertFromString(toscaGetFunction: string): ToscaGetFunctionType {
        if (!toscaGetFunction) {
            return;
        }

        if (ToscaGetFunctionType.GET_INPUT === toscaGetFunction.toUpperCase()) {
            return ToscaGetFunctionType.GET_INPUT;
        }

        if (ToscaGetFunctionType.GET_PROPERTY === toscaGetFunction.toUpperCase()) {
            return ToscaGetFunctionType.GET_PROPERTY;
        }

        if (ToscaGetFunctionType.GET_ATTRIBUTE === toscaGetFunction.toUpperCase()) {
            return ToscaGetFunctionType.GET_ATTRIBUTE;
        }

        return undefined;

    }

    /**
     * Converts a ToscaGetFunctionType to a ToscaFunctionType
     * @param toscaGetFunctionType
     */
    static convertToToscaFunctionType(toscaGetFunctionType: ToscaGetFunctionType): ToscaFunctionType {
        switch (toscaGetFunctionType) {
            case ToscaGetFunctionType.GET_INPUT:
                return ToscaFunctionType.GET_INPUT;
            case ToscaGetFunctionType.GET_ATTRIBUTE:
                return ToscaFunctionType.GET_ATTRIBUTE;
            case ToscaGetFunctionType.GET_PROPERTY:
                return ToscaFunctionType.GET_PROPERTY;
            default:
                return undefined;
        }

    }

}
