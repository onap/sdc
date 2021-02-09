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

import static org.openecomp.sdc.common.errors.Messages.MANIFEST_VALIDATION_HELM_IS_BASE_MISSING;
import static org.openecomp.sdc.common.errors.Messages.MANIFEST_VALIDATION_HELM_IS_BASE_NOT_SET;
import static org.openecomp.sdc.common.errors.Messages.MANIFEST_VALIDATION_HELM_IS_BASE_NOT_UNIQUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;

public class CnfPackageValidator {

    public List<String> validateHelmPackage(List<FileData> modules) {
        List<String> messages = Collections.emptyList();

        if (modules != null && !modules.isEmpty()) {
            Stats stats = calculateStats(modules);
            messages = createErrorMessages(stats);
        }

        return messages;
    }

    private Stats calculateStats(List<FileData> modules) {
        Stats stats = new Stats();
        for (FileData mod : modules) {
            if (mod.getBase() == null) {
                stats.without++;
            } else if (mod.getBase()) {
                stats.base++;
            }
        }
        return stats;
    }

    private List<String> createErrorMessages(Stats stats) {
        List<String> messages = new ArrayList<>();

        if (stats.without > 0) {
            messages.add(MANIFEST_VALIDATION_HELM_IS_BASE_MISSING.formatMessage(stats.without));
        }

        if (stats.base == 0) {
            messages.add(MANIFEST_VALIDATION_HELM_IS_BASE_NOT_SET.getErrorMessage());
        } else if (stats.base > 1) {
            messages.add(MANIFEST_VALIDATION_HELM_IS_BASE_NOT_UNIQUE.getErrorMessage());
        }

        return messages;
    }

    private static class Stats {

        private int base = 0;
        private int without = 0;
    }
}
