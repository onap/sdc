/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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
package org.openecomp.sdc.be.components.impl.utils;

import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.config.ConfigurationManager;

public class DirectivesUtil {

    private static Optional<String> getDirective(final String directiveValue) {
        final List<String> directives = ConfigurationManager.getConfigurationManager().getConfiguration().getDirectives();
        if (CollectionUtils.isNotEmpty(directives)) {
            return directives.stream().filter(directiveValues -> directiveValues.equalsIgnoreCase(directiveValue)).findFirst();
        }
        return Optional.empty();
    }

    public static boolean isValid(final List<String> inDirectives) {
        if (CollectionUtils.isEmpty(inDirectives)) {
            return true;
        }
        return inDirectives.stream().allMatch(directive -> getDirective(directive).isPresent());
    }
}
