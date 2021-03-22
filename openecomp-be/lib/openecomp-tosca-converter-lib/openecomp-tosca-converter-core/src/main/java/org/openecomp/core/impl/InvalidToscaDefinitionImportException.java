/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.core.impl;

import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

/**
 * Runtime exception for errors in import statements inside a TOSCA definition yaml file.
 */
public class InvalidToscaDefinitionImportException extends RuntimeException {

    private final String message;

    /**
     * Builds the exception message based on the provided validation error list.
     *
     * @param validationErrorList The error list
     */
    public InvalidToscaDefinitionImportException(final List<ErrorMessage> validationErrorList) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("The provided package is invalid as it contains descriptors import errors:\n");
        if (CollectionUtils.isNotEmpty(validationErrorList)) {
            final StringJoiner joiner = new StringJoiner(";\n");
            validationErrorList.forEach(errorMessage -> joiner.add(String.format("%s: %s", errorMessage.getLevel(), errorMessage.getMessage())));
            message = stringBuilder.append(joiner.toString()).toString();
        } else {
            message = stringBuilder.toString();
        }
    }

    @Override
    public String getMessage() {
        return message;
    }
}
