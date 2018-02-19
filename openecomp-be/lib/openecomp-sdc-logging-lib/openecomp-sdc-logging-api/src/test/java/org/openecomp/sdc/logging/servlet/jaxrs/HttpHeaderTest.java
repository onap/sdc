/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.logging.servlet.jaxrs;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.function.Function;
import org.openecomp.sdc.logging.servlet.HttpHeader;
import org.testng.annotations.Test;

/**
 * Unit tests mutliple-option headers.
 *
 * @author evitaliy
 * @since 25 Mar 2018
 */
public class HttpHeaderTest {

    @Test
    public void valueReturnedWhenSinglePossibleHeader() {

        final String key = "Head";
        final String value = "1234";

        Function<String, String> reader = createReader(key, value);
        HttpHeader header = new HttpHeader(key);
        assertEquals(header.getAny(reader), value);
    }

    @Test
    public void nullReturnedWhenSingleNoMatchingHeader() {

        final String key = "Head";

        Function<String, String> reader = createReader(key, null);
        HttpHeader header = new HttpHeader(key);
        assertNull(header.getAny(reader));
    }

    @Test
    public void nullReturnedWhenNoneHeaderMatches() {
        Function<String, String> reader = createReader("None", "Value");
        HttpHeader header = new HttpHeader("A", "B", "C");
        assertNull(header.getAny(reader));
    }

    @Test
    public void valueReturnedWhenLastHeaderMatches() {

        final String lastKey = "Last";
        final String value = "1234";

        Function<String, String> reader = createReader(lastKey, value);
        HttpHeader header = new HttpHeader("First", "Second", lastKey);
        assertEquals(header.getAny(reader), value);
    }

    @Test
    public void valueReturnedWhenFirstHeaderMatches() {

        final String firstKey = "First";
        final String value = "1234";

        Function<String, String> reader = createReader(firstKey, value);
        HttpHeader header = new HttpHeader(firstKey, "Second", "Third");
        assertEquals(header.getAny(reader), value);
    }

    @Test
    public void valueReturnedWhenMiddleHeaderMatches() {

        final String middleKey = "Second";
        final String value = "1234";

        Function<String, String> reader = createReader(middleKey, value);
        HttpHeader header = new HttpHeader("First", middleKey, "Third");
        assertEquals(header.getAny(reader), value);
    }

    private Function<String, String> createReader(String key, String value) {
        return  h -> h.equals(key) ? value : null;
    }
}