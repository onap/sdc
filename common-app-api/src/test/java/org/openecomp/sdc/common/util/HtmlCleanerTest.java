/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.common.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HtmlCleanerTest {

    @Test
    public void validateStripHtmlReturnsStringWithNoHtml() {
        final String testInput = "  <div>testing <p>element</p> &value</div> ";
        String result = HtmlCleaner.stripHtml(testInput);
        assertEquals(result, "  testing element &value ");
    }

    @Test
    public void validateStripHtmlReturnsInputIfIsEmpty() {
        final String testInput = "";
        String result = HtmlCleaner.stripHtml(testInput);
        assertEquals(result, testInput);
    }

    @Test
    public void validateStripHtmlReturnsInputIfTagsAreEmpty() {
        final String testInput = "<>emptyTags<>";
        String result = HtmlCleaner.stripHtml(testInput);
        assertEquals(result, testInput);
    }

    @Test
    public void validateStripHtmlReturnsStripedHtmlWithEscapeTrue() {
        final String testInput = "  <div>testing <p>element</p> &value</div> ";
        String result = HtmlCleaner.stripHtml(testInput, true);
        assertEquals(result, "  testing element &amp;value ");
    }
}
