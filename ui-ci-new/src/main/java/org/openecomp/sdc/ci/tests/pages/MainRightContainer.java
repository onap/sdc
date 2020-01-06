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

package org.openecomp.sdc.ci.tests.pages;

import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;

/**
 * Represents the "w-sdc-main-right-container" panel that shows the list of resources/components.
 */
public class MainRightContainer {

    public static final String CSS_CLASS = "w-sdc-main-right-container";

    private MainRightContainer() {

    }

    /**
     * Checks if the resource is visible in the panel/container.
     *
     * @param resourceName the resource name to search in the panel/container
     * @return {@code true} if the resource is visible, {@code false} otherwise.
     */
    public static boolean isResultVisible(final String resourceName) {
        return GeneralUIUtils.isElementVisibleByTestId(resourceName);
    }

    /**
     * Checks if the panel/container is showing no resources.
     *
     * @return {@code true} if the panel/container is empty, {@code false} otherwise.
     */
    public static boolean isEmptyResult() {
        return GeneralUIUtils.isElementInvisibleByTestId("dashboard-Elements");
    }

}
