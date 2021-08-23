/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

public class CsarValidationResult implements ValidationResult {

    private final List<ErrorMessage> errorMessages = new ArrayList<>();

    @Override
    public boolean isValid() {
        return errorMessages.stream().noneMatch(errorMessage -> errorMessage.getLevel().equals(ErrorLevel.ERROR));
    }

    @Override
    public List<ErrorMessage> getErrors() {
        return errorMessages;
    }

    @Override
    public List<ErrorMessage> getErrors(final ErrorLevel errorLevel) {
        return errorMessages.stream().filter(errorMessage -> errorMessage.getLevel().equals(errorLevel)).collect(Collectors.toList());
    }

    public void addError(final ErrorMessage errorMessage) {
        errorMessages.add(errorMessage);
    }
}