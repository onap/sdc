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

import {FilterConstraint} from "../models/filter-constraint";
import {ToscaFunctionHelper} from "./tosca-function-helper";

export class FilterConstraintHelper {

    public static buildFilterConstraintLabel(constraint: FilterConstraint): string {
        let value;
        if (ToscaFunctionHelper.isValueToscaFunction(constraint.value)) {
            const toscaFunction = ToscaFunctionHelper.convertObjectToToscaFunction(constraint.value);
            if (toscaFunction) {
                value = toscaFunction.buildValueString();
            } else {
                value = JSON.stringify(constraint.value, null, 4);
            }
        } else {
            value = JSON.stringify(constraint.value, null, 4);
        }
        if (constraint.capabilityName) {
            return `${constraint.capabilityName}: ${constraint.servicePropertyName} ${this.convertToSymbol(constraint.constraintOperator)} ${value}`;
        }

        return `${constraint.servicePropertyName} ${this.convertToSymbol(constraint.constraintOperator)} ${value}`;
    }

    public static convertToSymbol(constraintOperator: string) {
        switch (constraintOperator) {
            case ConstraintOperatorType.LESS_THAN: return '<';
            case ConstraintOperatorType.EQUAL: return '=';
            case ConstraintOperatorType.GREATER_THAN: return '>';
            case ConstraintOperatorType.GREATER_OR_EQUAL: return '>=';
            case ConstraintOperatorType.LESS_OR_EQUAL: return '<=';
        }
    }

}

export enum ConstraintOperatorType {
    EQUAL = 'equal',
    GREATER_THAN = 'greater_than',
    LESS_THAN = 'less_than',
    GREATER_OR_EQUAL = 'greater_or_equal',
    LESS_OR_EQUAL = 'less_or_equal'
}

