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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.Test;
import org.openecomp.sdc.logging.servlet.HttpHeader;

/**
 * Unit tests multiple-option headers.
 *
 * @author evitaliy
 * @since 25 Mar 2018
 */
public class HttpHeaderTest {

    private static final String KEY_FIRST = "First";
    private static final String KEY_SECOND = "Second";
    private static final Supplier<? extends Throwable> VALUE_EXPECTED = () -> new AssertionError("Value expected");

    @Test
    public void valueReturnedWhenSinglePossibleHeader() {

        final String key = "Head";
        final String value = "1234";

        Function<String, String> reader = createReader(key, value);
        HttpHeader header = new HttpHeader(new String[]{ key });
        assertEquals(value, header.getAny(reader));
    }

    @Test
    public void nullReturnedWhenSingleNoMatchingHeader() {

        final String key = "Head";

        Function<String, String> reader = createReader(key, null);
        HttpHeader header = new HttpHeader(new String[]{ key });
        assertNull(header.getAny(reader));
    }

    @Test
    public void nullReturnedWhenNoneHeaderMatches() {
        Function<String, String> reader = createReader("None", "Value");
        HttpHeader header = new HttpHeader(new String[]{ "A", "B", "C" });
        assertNull(header.getAny(reader));
    }

    @Test
    public void valueReturnedWhenLastHeaderMatches() throws Throwable {

        final String lastKey = "Last";
        final String value = "1234";

        Function<String, String> reader = createReader(lastKey, value);
        HttpHeader header = new HttpHeader(new String[]{ KEY_FIRST, KEY_SECOND, lastKey });
        assertEquals(value, header.getAny(reader).orElseThrow(VALUE_EXPECTED));
    }

    @Test
    public void valueReturnedWhenFirstHeaderMatches() throws Throwable {

        final String value = "1234";
        Function<String, String> reader = createReader(KEY_FIRST, value);
        HttpHeader header = new HttpHeader(new String[]{ KEY_FIRST, KEY_SECOND, "Third" });
        assertEquals(value, header.getAny(reader).orElseThrow(VALUE_EXPECTED));
    }

    @Test
    public void valueReturnedWhenMiddleHeaderMatches() throws Throwable {

        final String value = "1234";
        Function<String, String> reader = createReader(KEY_SECOND, value);
        HttpHeader header = new HttpHeader(new String[]{ KEY_FIRST, KEY_SECOND, "Third" });
        assertEquals(value, header.getAny(reader).orElseThrow(VALUE_EXPECTED));
    }

    private Function<String, String> createReader(String key, String value) {
        return  h -> h.equals(key) ? value : null;
    }
}