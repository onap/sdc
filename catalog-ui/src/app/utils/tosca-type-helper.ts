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

import {DerivedPropertyType} from "../models/properties-inputs/property-be-model";
import {PROPERTY_DATA, PROPERTY_TYPES} from "./constants";

export class ToscaTypeHelper {

    private ToscaTypeHelper() {
        //not designed to be instantiated
    }

    public static getType(typeName: string): DerivedPropertyType {
        if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(typeName) > -1) {
            return DerivedPropertyType.SIMPLE;
        } else if (typeName === PROPERTY_TYPES.LIST) {
            return DerivedPropertyType.LIST;
        } else if (typeName === PROPERTY_TYPES.MAP) {
            return DerivedPropertyType.MAP;
        } else {
            return DerivedPropertyType.COMPLEX;
        }
    }

    public static isTypeSimple(typeName: string): boolean {
        return this.getType(typeName) == DerivedPropertyType.SIMPLE;
    }

    public static isTypeList(typeName: string): boolean {
        return this.getType(typeName) == DerivedPropertyType.LIST;
    }

    public static isTypeMap(typeName: string): boolean {
        return this.getType(typeName) == DerivedPropertyType.MAP;
    }

    public static isTypeComplex(typeName: string): boolean {
        return !this.isTypeSimple(typeName) && !this.isTypeList(typeName) && !this.isTypeMap(typeName);
    }

    public static isTypeNumber(typeName: string): boolean {
        return typeName === PROPERTY_TYPES.INTEGER || typeName === PROPERTY_TYPES.FLOAT;
    }

    public static isTypeBoolean(typeName: string): boolean {
        return typeName === PROPERTY_TYPES.BOOLEAN;
    }

    public static isTypeLiteral(typeName: string): boolean {
        return !this.isTypeNumber(typeName) && !this.isTypeBoolean(typeName) && !this.isTypeList(typeName) && !this.isTypeMap(typeName)
            && !this.isTypeComplex(typeName);
    }

    public static isTypeRange(typeName: string): boolean {
        return typeName === PROPERTY_TYPES.RANGE;
    }

}
