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
import java.util.function.Supplier;
import org.junit.Test;

/**
 * Unit tests multiple-option headers.
 *
 * @author evitaliy
 * @since 31 Jul 2018
 */
public class HttpHeaderTest {

    private static final Supplier<? extends Throwable> VALUE_EXPECTED = () -> new AssertionError("Value expected");
    private static final Function<String, String> NULL_WHEN_NAME_NOT_B = k -> "B".equals(k) ? "Value" : null;

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenInputArrayNull() {
        new HttpHeader((String[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenInputListNull() {
        new HttpHeader((List<String>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenInputArrayEmpty() {
        new HttpHeader();
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenInputListEmpty() {
        new HttpHeader(Collections.emptyList());
    }

    @Test
    public void valueNotReturnedWhenNameInArrayNotRequested() {
        HttpHeader header = new HttpHeader("A");
        assertFalse(header.getAny(NULL_WHEN_NAME_NOT_B).isPresent());
    }

    @Test
    public void valueNotReturnedWhenNameInListNotRequested() {
        HttpHeader header = new HttpHeader(Collections.singletonList("A"));
        assertFalse(header.getAny(NULL_WHEN_NAME_NOT_B).isPresent());
    }

    @Test
    public void valueReturnedWhenSinglePossibleHeaderInArrayMatches() {
        HttpHeader header = new HttpHeader("B");
        assertTrue(header.getAny(NULL_WHEN_NAME_NOT_B).isPresent());
    }

    @Test
    public void valueReturnedWhenSinglePossibleHeaderInListMatches() {
        HttpHeader header = new HttpHeader(Collections.singletonList("B"));
        assertTrue(header.getAny(NULL_WHEN_NAME_NOT_B).isPresent());
    }

    @Test
    public void valueReturnedWhenLastHeaderInArrayMatches() throws Throwable {
        HttpHeader header = new HttpHeader("A", "B");
        header.getAny(NULL_WHEN_NAME_NOT_B).orElseThrow(VALUE_EXPECTED);
    }

    @Test
    public void valueReturnedWhenLastHeaderInListMatches() throws Throwable {
        HttpHeader header = new HttpHeader(Arrays.asList("A", "B"));
        header.getAny(NULL_WHEN_NAME_NOT_B).orElseThrow(VALUE_EXPECTED);
    }

    @Test
    public void valueReturnedWhenFirstHeaderInArrayMatches() throws Throwable {
        HttpHeader header = new HttpHeader("B", "A");
        header.getAny(NULL_WHEN_NAME_NOT_B).orElseThrow(VALUE_EXPECTED);
    }

    @Test
    public void valueReturnedWhenFirstHeaderInListMatches() throws Throwable {
        HttpHeader header = new HttpHeader(Arrays.asList("B", "A"));
        header.getAny(NULL_WHEN_NAME_NOT_B).orElseThrow(VALUE_EXPECTED);
    }

    @Test
    public void valueReturnedWhenMiddleHeaderInArrayMatches() throws Throwable {
        HttpHeader header = new HttpHeader("A", "B", "C");
        header.getAny(NULL_WHEN_NAME_NOT_B).orElseThrow(VALUE_EXPECTED);
    }

    @Test
    public void valueReturnedWhenMiddleHeaderInListMatches() throws Throwable {
        HttpHeader header = new HttpHeader(Arrays.asList("A", "B", "C"));
        header.getAny(NULL_WHEN_NAME_NOT_B).orElseThrow(VALUE_EXPECTED);
    }
}