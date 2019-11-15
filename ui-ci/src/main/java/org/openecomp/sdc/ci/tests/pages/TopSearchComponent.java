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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * Represents the "top-search" search component in the main upper toolbar.
 */
public class TopSearchComponent {

    public static final String CSS_CLASS = "top-search";
    public static final String SEARCH_INPUT_TEST_ID = "main-menu-input-search";

    private TopSearchComponent() {

    }

    /**
     * Gets the search component input, waiting for it's visibility.
     *
     * @return search component input with test id {@link #SEARCH_INPUT_TEST_ID}
     */
    public static WebElement getComponentInput() {
        return GeneralUIUtils.getWebElementByTestID(SEARCH_INPUT_TEST_ID);
    }

    /**
     * Replaces the current search input value by the given value. This prevents to trigger the search twice by cleaning
     * the input (triggers the search) and then pasting the value (triggers the search again).
     *
     * @param value the value to search
     */
    public static void replaceSearchValue(final String value) {
        replaceSearchValue(getComponentInput(), value);
    }

    /**
     * Replaces the current search input value by selecting it with Ctrl+A shortcut and pasting the given value. This
     * prevents to trigger the search twice by cleaning the input (triggers the search) and then pasting the value
     * (triggers the search again).
     *
     * @param searchTextInput input web element
     * @param value the value to search
     */
    public static void replaceSearchValue(final WebElement searchTextInput, final String value) {
        searchTextInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), value);
    }

}
