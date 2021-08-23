/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.openecomp.core.utilities.file.FileContentHandler;

/**
 * Validates the contents of the CSAR package uploaded in SDC.
 */
public interface Validator {

    /**
     * Validates the structure and content of a CSAR.
     *
     * @param csarContent the CSAR content
     * @return the result of the validation
     */
    ValidationResult validate(final FileContentHandler csarContent);

    /**
     * Checks if the validator applies to the given model.
     *
     * @param model the model to check
     * @return {@code true} if the validator applies to the given model, {@code false} otherwise
     */
    boolean appliesTo(final String model);

    /**
     * Should return the execution order that the validator is intended to run in relation to other validators that applies to the same model ({@link
     * #appliesTo(String)}). The lower the value, the higher the priority. If a validator happens to have the same order of others, the system will
     * randomly decides the execution order.
     *
     * @return the execution order of the validator.
     */
    int getOrder();
}
