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

export class FilterConstraint {
    capabilityName: string;
    servicePropertyName: string;
    constraintOperator: string;
    sourceType: string;
    sourceName: string;
    value: any;
    originalType: string;

    constructor(input?: any) {
        if (input) {
            this.capabilityName = input.capabilityName;
            this.servicePropertyName = input.servicePropertyName;
            this.constraintOperator = input.constraintOperator;
            this.sourceType = input.sourceType;
            this.sourceName = input.sourceName;
            this.value = input.value;
            this.originalType = input.originalType;
        }
    }
}
