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

package org.openecomp.sdc.logging.servlet;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.junit.Test;

/**
 * Unit testing getting any of possible headers.
 *
 * @author evitaliy
 * @since 31 Jul 2018
 */
public class HttpHeaderTest {

    private static final Function<String, String> NULL_WHEN_KEY_NOT_B = k -> "B".equals(k) ? "Value" : null;

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenInputArrayNull() {
        new HttpHeader((String[])null);
    }

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenInputListNull() {
        new HttpHeader((List<String>)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenInputArrayEmpty() {
        new HttpHeader(new String[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenInputListEmpty() {
        new HttpHeader(Collections.emptyList());
    }

    @Test
    public void valueNotFoundWhenKeyNotRequestedInArray() {
        HttpHeader header = new HttpHeader(new String[] {"A"});
        assertFalse(header.getAny(NULL_WHEN_KEY_NOT_B).isPresent());
    }

    @Test
    public void valueNotFoundWhenKeyNotRequestedInList() {
        HttpHeader header = new HttpHeader(Collections.singletonList("A"));
        assertFalse(header.getAny(NULL_WHEN_KEY_NOT_B).isPresent());
    }

    @Test
    public void valueFoundWhenKeyRequestedInArray() {
        HttpHeader header = new HttpHeader(new String[] {"A", "B"});
        assertTrue(header.getAny(NULL_WHEN_KEY_NOT_B).isPresent());
    }

    @Test
    public void valueFoundWhenKeyRequestedInList() {
        HttpHeader header = new HttpHeader(Arrays.asList("A", "B"));
        assertTrue(header.getAny(NULL_WHEN_KEY_NOT_B).isPresent());
    }
}