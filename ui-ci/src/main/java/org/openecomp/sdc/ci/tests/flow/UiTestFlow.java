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

package org.openecomp.sdc.ci.tests.flow;

import org.openecomp.sdc.ci.tests.pages.PageObject;

import java.util.Optional;

/**
 * Represents a UI test flow
 */
@FunctionalInterface
public interface UiTestFlow {

    /**
     * Runs the flow
     * @param pageObjects any required page object for the flow
     * @return an optional page object representing the page that the flow has ended
     */
    Optional<PageObject> run(final PageObject... pageObjects);

}
