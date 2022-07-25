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

package org.openecomp.sdc.be.model.validation;

import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.Component;

/**
 * Represents a validator for a property that has a TOSCA function value
 */
@org.springframework.stereotype.Component
public interface ToscaFunctionValidator {

    /**
     * Validates the given property that has a TOSCA function value. Should throw a RuntimeException in case of failure.
     *
     * @param property           the property with a tosca function to validate
     * @param containerComponent the component that is the container for the instance that has the property
     * @param <T>                a property data definition
     */
    <T extends PropertyDataDefinition> void validate(T property, Component containerComponent);
}
