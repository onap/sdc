/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia
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
package org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.validation;

import java.util.ArrayList;
import java.util.List;

public final class CnfValidatorResult {

    private final List<String> errorMessages;
    private final List<String> warningMessages;
    private boolean isDeployable;

    public CnfValidatorResult() {
        this.errorMessages = new ArrayList<>();
        this.warningMessages = new ArrayList<>();
        this.isDeployable = true;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public boolean isValid() {
        return errorMessages.isEmpty() && isDeployable;
    }

    public List<String> getWarningMessages() {
        return warningMessages;
    }

    public void addWarning(String helmWarning) {
        warningMessages.add(helmWarning);
    }

    public void addErrorMessages(List<String> errorMessages) {
        this.errorMessages.addAll(errorMessages);
    }

    public void addWarningMessages(List<String> warningMessages) {
        this.warningMessages.addAll(warningMessages);
    }

    void setDeployable(boolean deployable) {
        isDeployable = deployable;
    }
}
